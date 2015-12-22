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

import com.twrb.core.timetable.MobileWebTimetableSearcher;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

public class SearchFragment extends Fragment {
    @Bind(R.id.spinner_date)
    Spinner date_spinner;
    @Bind(R.id.spinner_from)
    Spinner from_spinner;
    @Bind(R.id.spinner_to)
    Spinner to_spinner;
    private TimetableStationArrayAdapter timetableStationArrayAdapter;
    private DateArrayAdapter dateArrayAdapter;
    private ProgressDialog progressDialog;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildBookableStationArrayAdapter();
        buildDateArrayAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        this.from_spinner.setAdapter(this.timetableStationArrayAdapter);
        this.from_spinner.setSelection(14); // 台北
        this.to_spinner.setAdapter(this.timetableStationArrayAdapter);
        this.to_spinner.setSelection(71); // 花蓮
        this.date_spinner.setAdapter(this.dateArrayAdapter);
        return view;
    }

    @OnClick(R.id.button_search)
    public void onSearchButtonClick() {
        SearchInfo si = SearchInfo.createExpressClass();
        si.fromStation = ((TimetableStation) this.from_spinner.getSelectedItem()).getNo();
        si.fromCity = ((TimetableStation) this.from_spinner.getSelectedItem()).getCityNo();
        si.toCity = ((TimetableStation) this.to_spinner.getSelectedItem()).getCityNo();
        si.toStation = ((TimetableStation) this.to_spinner.getSelectedItem()).getNo();
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
