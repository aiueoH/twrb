package com.dowob.twrb.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.dowob.twrb.DBObject.City;
import com.dowob.twrb.DBObject.TimetableStation;
import com.dowob.twrb.Events.OnSearchedEvent;
import com.dowob.twrb.Model.TimetableSearcher;
import com.dowob.twrb.MyArrayAdapter.DateArrayAdapter;
import com.dowob.twrb.MyArrayAdapter.TimetableStationArrayAdapter;
import com.dowob.twrb.NetworkChecker;
import com.dowob.twrb.R;
import com.dowob.twrb.SnackbarHelper;
import com.dowob.twrb.TimetableActivity;
import com.jakewharton.rxbinding.view.RxView;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;
import linkedspinner.Item;
import linkedspinner.LinkedSpinner;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchFragment extends Fragment {
    @Bind(R.id.button_search)
    Button search_button;
    @Bind(R.id.spinner_date)
    Spinner date_spinner;
    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    @Bind(R.id.imageButton_swap)
    ImageButton swap_imageButton;
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
        Item defaultFromItem = null, defaultToItem = null;
        String defaultFromString = getLastSearchedFromStation();
        String defaultToSTring = getLastSearchedToStation();
        if (defaultFromString == null) defaultFromString = "臺北";
        if (defaultToSTring == null) defaultToSTring = "高雄";
        System.out.print(defaultFromString + "," + defaultToSTring);
        for (City c : rr) {
            Item item = new Item(c.getNo(), c.getNameCh());
            List<Item> subItems = new ArrayList<>();
            if (c.getTimetableStations().isEmpty())
                continue;
            for (TimetableStation ts : c.getTimetableStations()) {
                Item subItem = new Item(ts.getNo(), ts.getNameCh(), item);
                subItems.add(subItem);
                if (subItem.getName().equals(defaultFromString))
                    defaultFromItem = subItem;
                if (subItem.getName().equals(defaultToSTring))
                    defaultToItem = subItem;
            }
            item.setSubItems(subItems);
            items.add(item);
        }
        fromLinkedSpinner = new LinkedSpinner(getActivity(), items);
        if (defaultFromItem != null) fromLinkedSpinner.setRightSelectedItem(defaultFromItem);
        toLinkedSpinner = new LinkedSpinner(getActivity(), items);
        if (defaultToItem != null) toLinkedSpinner.setRightSelectedItem(defaultToItem);
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

    private void setLastSearchedStation(String from, String to) {
        SharedPreferences sp = getContext().getSharedPreferences(getContext().getString(R.string.sp_name), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("lastSearchedFromStation", from);
        editor.putString("lastSearchedToStation", to);
        editor.commit();
    }

    private String getLastSearchedFromStation() {
        return getLastSearchedStation("lastSearchedFromStation");
    }

    private String getLastSearchedToStation() {
        return getLastSearchedStation("lastSearchedToStation");
    }

    private String getLastSearchedStation(String key) {
        SharedPreferences sp = getContext().getSharedPreferences(getContext().getString(R.string.sp_name), Activity.MODE_PRIVATE);
        String station = sp.getString(key, "");
        return station.isEmpty() ? null : station;
    }

    private void updateCityAndStation() {
        fromCity = (String) fromLinkedSpinner.getRightSelectedItem().getSuperItem().getValue();
        toCity = (String) toLinkedSpinner.getRightSelectedItem().getSuperItem().getValue();
        fromStation = (String) fromLinkedSpinner.getRightSelectedItem().getValue();
        toStation = (String) toLinkedSpinner.getRightSelectedItem().getValue();
    }

    private void updateStationTextView() {
        String from = (String) fromLinkedSpinner.getRightSelectedItem().getName();
        String to = (String) toLinkedSpinner.getRightSelectedItem().getName();
        if (from.equals("侯硐(猴硐)"))
            from = "侯硐";
        if (to.equals("侯硐(猴硐)"))
            to = "侯硐";
        from_textView.setText(from);
        to_textView.setText(to);
        setLastSearchedStation(from, to);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        this.date_spinner.setAdapter(this.dateArrayAdapter);
        updateStationTextView();
        RxView.clicks(from_textView).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> fromLinkedSpinner.show());
        RxView.clicks(to_textView).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> toLinkedSpinner.show());
        RxView.clicks(swap_imageButton).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> onSwapButtonClick());
        RxView.clicks(search_button).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> onSearchButtonClick());
        return view;
    }

    public void onSwapButtonClick() {
        Item to = toLinkedSpinner.getRightSelectedItem();
        Item from = fromLinkedSpinner.getRightSelectedItem();
        toLinkedSpinner.setRightSelectedItem(from);
        fromLinkedSpinner.setRightSelectedItem(to);
        updateCityAndStation();
        updateStationTextView();
    }

    public void onSearchButtonClick() {
        final SearchInfo si = createSearchInfo();
        if (si == null) {
            SnackbarHelper.show(date_spinner, getString(R.string.wtf), Snackbar.LENGTH_LONG);
            return;
        }
        if (!NetworkChecker.isConnected(getContext())) {
            SnackbarHelper.show(date_spinner, getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        Observable.just(si)
                .map(searchInfo -> search(searchInfo))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> showSearchingProgressDialog())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    progressDialog.dismiss();
                    if (result.getKey().equals(TimetableSearcher.Result.OK)) {
                        if (result.getValue().isEmpty())
                            SnackbarHelper.show(date_spinner, getString(R.string.no_search_result), Snackbar.LENGTH_LONG);
                        else {
                            EventBus.getDefault().postSticky(new OnSearchedEvent(si, result.getValue()));
                            Intent intent = new Intent(getActivity(), TimetableActivity.class);
                            startActivity(intent);
                        }
                    } else
                        SnackbarHelper.show(date_spinner, getString(R.string.search_error), Snackbar.LENGTH_LONG);
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
    private AbstractMap.SimpleEntry<TimetableSearcher.Result, List<TrainInfo>> search(SearchInfo searchInfo) {
        AbstractMap.SimpleEntry<TimetableSearcher.Result, List<TrainInfo>> result;
        result = new TimetableSearcher().search(searchInfo);
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
