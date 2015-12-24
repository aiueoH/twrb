package ah.twrbtest.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.twrb.core.timetable.MobileWebTimetableSearcher;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ah.twrbtest.DBObject.City;
import ah.twrbtest.DBObject.TimetableStation;
import ah.twrbtest.Events.OnSearchedEvent;
import ah.twrbtest.MyArrayAdapter.DateArrayAdapter;
import ah.twrbtest.MyArrayAdapter.TimetableStationArrayAdapter;
import ah.twrbtest.R;
import ah.twrbtest.TimetableActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;
import linkedspinner.Item;
import linkedspinner.LinkedSpinner;

public class SearchFragment extends Fragment {
    @Bind(R.id.spinner_date)
    Spinner date_spinner;
    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    private TimetableStationArrayAdapter timetableStationArrayAdapter;
    private DateArrayAdapter dateArrayAdapter;
    private ProgressDialog progressDialog;
    private LinkedSpinner fromLinkedSpinner;
    private LinkedSpinner toLinkedSpinner;
    private String fromCity;
    private String toCity;
    private String fromStation;
    private String toStation;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildBookableStationArrayAdapter();
        buildDateArrayAdapter();
        // setup station linked spinner
        List<Item> items = new ArrayList<>();
        RealmResults<City> rr = Realm.getDefaultInstance().allObjects(City.class);
        Item defaultFrom = null, deafultTo = null;
        for (City c : rr) {
            Item item = new Item(c.getNo(), c.getNameCh());
            List<Item> subItems = new ArrayList<>();
            for (TimetableStation ts : c.getTimetableStations()) {
                Item subItem = new Item(ts.getNo(), ts.getNameCh(), item);
                subItems.add(subItem);
                if (subItem.getName().equals("臺北"))
                    defaultFrom = subItem;
                if (subItem.getName().equals("花蓮"))
                    deafultTo = subItem;
            }
            item.setSubItems(subItems);
            items.add(item);
        }
        fromLinkedSpinner = new LinkedSpinner(getActivity(), items);
        fromLinkedSpinner.setSelectedSubItem(defaultFrom);
        toLinkedSpinner = new LinkedSpinner(getActivity(), items);
        toLinkedSpinner.setSelectedSubItem(deafultTo);
        EventBus.getDefault().register(this);
        updateCityAndStation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(OnSearchedEvent e) {
        this.progressDialog.dismiss();
        if (e.getTrainInfos() == null || e.getTrainInfos().isEmpty()) {
            Snackbar.make(date_spinner, "很遺憾，你輸入的資料查不到任何班次", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), TimetableActivity.class);
        startActivity(intent);
    }

    public void onEvent(LinkedSpinner.OnSelectedEvent e) {
        updateCityAndStation();
        updateStationTextView();
    }

    private void updateCityAndStation() {
        fromCity = (String) fromLinkedSpinner.getSelectedSubItem().getSuperItem().getValue();
        toCity = (String) toLinkedSpinner.getSelectedSubItem().getSuperItem().getValue();
        fromStation = (String) fromLinkedSpinner.getSelectedSubItem().getValue();
        toStation = (String) toLinkedSpinner.getSelectedSubItem().getValue();
    }

    private void updateStationTextView() {
        to_textView.setText((String) toLinkedSpinner.getSelectedSubItem().getName());
        from_textView.setText((String) fromLinkedSpinner.getSelectedSubItem().getName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        this.date_spinner.setAdapter(this.dateArrayAdapter);
        updateStationTextView();
        return view;
    }

    @OnClick(R.id.textView_from)
    public void onFromClick() {
        fromLinkedSpinner.show();
    }

    @OnClick(R.id.textView_to)
    public void onToClick() {
        toLinkedSpinner.show();
    }

    @OnClick(R.id.button_search)
    public void onSearchButtonClick() {
        SearchInfo si = SearchInfo.createExpressClass();
        si.fromStation = fromStation;
        si.fromCity = fromCity;
        si.toStation = toStation;
        si.toCity = toCity;
        si.setDateTime((Date) (this.date_spinner.getSelectedItem()));
        if (si.fromStation.equals(si.toStation)) {
            Snackbar.make(date_spinner, "三小？", Snackbar.LENGTH_SHORT)
                    .setAction("我知道錯了", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    })
                    .show();
            return;
        }
        this.progressDialog = ProgressDialog.show(getActivity(), "", "正在幫您查查");
        this.progressDialog.show();
        new AsyncSearcher(si).execute();
    }

    private void buildDateArrayAdapter() {
        int selectableDateCount = 70;
        List<Date> dates = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        dates.add(c.getTime());
        for (int i = 0; i < selectableDateCount - 1; i++) {
            c.add(Calendar.DATE, 1);
            dates.add(c.getTime());
        }
        this.dateArrayAdapter = new DateArrayAdapter(getActivity(), R.layout.item_date, dates);
    }

    private void buildBookableStationArrayAdapter() {
        RealmResults<TimetableStation> rr = Realm.getDefaultInstance().where(TimetableStation.class).equalTo("isBookable", true).findAll();
        List<TimetableStation> tss = new ArrayList<>();
        for (TimetableStation ts : rr)
            tss.add(ts);
        this.timetableStationArrayAdapter = new TimetableStationArrayAdapter(getActivity(), R.layout.item_bookablestation, tss);
    }

    class AsyncSearcher extends AsyncTask<Integer, Integer, Boolean> {
        private SearchInfo searchInfo;
        private ArrayList<TrainInfo> trainInfos;

        public AsyncSearcher(SearchInfo searchInfo) {
            this.searchInfo = searchInfo;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
//                this.trainInfos = TimetableSearcher.search(this.searchInfo);
                this.trainInfos = MobileWebTimetableSearcher.search(this.searchInfo);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            EventBus.getDefault().postSticky(new OnSearchedEvent(this.searchInfo, this.trainInfos));
        }
    }
}
