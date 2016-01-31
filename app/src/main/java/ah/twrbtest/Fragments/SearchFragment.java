package ah.twrbtest.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import ah.twrbtest.SnackbarHelper;
import ah.twrbtest.TimetableActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;
import linkedspinner.Item;
import linkedspinner.LinkedSpinner;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
            if (c.getTimetableStations().isEmpty())
                continue;
            for (TimetableStation ts : c.getTimetableStations()) {
                Item subItem = new Item(ts.getNo(), ts.getNameCh(), item);
                subItems.add(subItem);
                if (subItem.getName().equals("臺北"))
                    defaultFrom = subItem;
                if (subItem.getName().equals("高雄"))
                    deafultTo = subItem;
            }
            item.setSubItems(subItems);
            items.add(item);
        }
        fromLinkedSpinner = new LinkedSpinner(getActivity(), items);
        fromLinkedSpinner.setRightSelectedItem(defaultFrom);
        toLinkedSpinner = new LinkedSpinner(getActivity(), items);
        toLinkedSpinner.setRightSelectedItem(deafultTo);
        EventBus.getDefault().register(this);
        updateCityAndStation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(LinkedSpinner.OnSelectedEvent e) {
        updateCityAndStation();
        updateStationTextView();
    }

    private void updateCityAndStation() {
        fromCity = (String) fromLinkedSpinner.getRightSelectedItem().getSuperItem().getValue();
        toCity = (String) toLinkedSpinner.getRightSelectedItem().getSuperItem().getValue();
        fromStation = (String) fromLinkedSpinner.getRightSelectedItem().getValue();
        toStation = (String) toLinkedSpinner.getRightSelectedItem().getValue();
    }

    private void updateStationTextView() {
        to_textView.setText((String) toLinkedSpinner.getRightSelectedItem().getName());
        from_textView.setText((String) fromLinkedSpinner.getRightSelectedItem().getName());
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
        final SearchInfo si = createSearchInfo();
        if (si == null) {
            SnackbarHelper.show(date_spinner, getString(R.string.wtf), Snackbar.LENGTH_SHORT);
            return;
        }
        Observable.just(si)
                .map(searchInfo -> search(searchInfo))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> showSearchingProgressDialog())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trainInfos -> {
                    progressDialog.dismiss();
                    if (trainInfos == null || trainInfos.isEmpty()) {
                        SnackbarHelper.show(date_spinner, getString(R.string.no_search_result), Snackbar.LENGTH_SHORT);
                        return;
                    }
                    EventBus.getDefault().postSticky(new OnSearchedEvent(si, trainInfos));
                    Intent intent = new Intent(getActivity(), TimetableActivity.class);
                    startActivity(intent);
                });
    }

    @NonNull
    private SearchInfo createSearchInfo() {
        final SearchInfo si = SearchInfo.createAllClass();
        si.fromStation = fromStation;
        si.fromCity = fromCity;
        si.toStation = toStation;
        si.toCity = toCity;
        si.setDateTime((Date) (this.date_spinner.getSelectedItem()));
        if (si.fromStation.equals(si.toStation))
            return null;
        return si;
    }

    @Nullable
    private ArrayList<TrainInfo> search(SearchInfo searchInfo) {
        ArrayList<TrainInfo> result = null;
        try {
            result = MobileWebTimetableSearcher.search(searchInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void showSearchingProgressDialog() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.is_searching));
        progressDialog.show();
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
}
