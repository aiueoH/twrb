package com.dowob.twrb.features.timetable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.TimetableStation;
import com.dowob.twrb.events.OnSearchedEvent;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.features.tickets.BookFlowController;
import com.dowob.twrb.features.tickets.BookRecordActivity;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
public class TimetableActivity extends AppCompatActivity {
    @Bind(R.id.tabs)
    TabLayout tabLayout;
    @Bind(R.id.container)
    ViewPager viewPager;
    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    @Bind(R.id.textView_date)
    TextView date_textView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    private Calendar searchDate;
    private SearchInfo searchInfo;
    private List<TrainInfo> trainInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        ButterKnife.bind(this);
        setupToolbar();
        OnSearchedEvent e = EventBus.getDefault().getStickyEvent(OnSearchedEvent.class);
        this.trainInfos = e.getTrainInfos();
        this.searchInfo = e.getSearchInfo();
        this.searchDate = parseSearchDate();
        this.date_textView.setText(formatSearchDate());
        this.from_textView.setText(TimetableStation.getByNo(this.searchInfo.fromStation).getNameCh());
        this.to_textView.setText(TimetableStation.getByNo(this.searchInfo.toStation).getNameCh());

        ViewPagerAdapter vpa = new ViewPagerAdapter(getSupportFragmentManager());
        EventBus.getDefault().postSticky(new TimetableFragment.OnPassingTrainInfoEvent(trainInfos, searchDate, searchInfo));
        vpa.createAllClass();
        List<TrainInfo> expressTrainInfos = getExpressClassTrainInfos();
        EventBus.getDefault().postSticky(new TimetableFragment.OnPassingTrainInfoEvent(expressTrainInfos, searchDate, searchInfo));
        vpa.createExpressClass();
        viewPager.setAdapter(vpa);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private Calendar parseSearchDate() {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(this.searchInfo.searchDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return c;
    }

    private String formatSearchDate() {
        return new SimpleDateFormat("yyyy/MM/dd E").format(searchDate.getTime());
    }

    @NonNull
    private List<TrainInfo> getExpressClassTrainInfos() {
        List<TrainInfo> expressTrainInfos = new ArrayList<>();
        for (TrainInfo trainInfo : trainInfos) {
            if (trainInfo.type.equals("自強") ||
                    trainInfo.type.equals("莒光") ||
                    trainInfo.type.equals("普悠瑪") ||
                    trainInfo.type.equals("太魯閣"))
                expressTrainInfos.add(trainInfo);
        }
        return expressTrainInfos;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(TrainInfoAdapter.OnItemClickEvent e) {
        new BookFlowController(this, toolbar, bookResult -> {
            String msg = bookResult.getStatusMsg(TimetableActivity.this);
            if (bookResult.isOK())
                SnackbarHelper.show(tabLayout, msg, Snackbar.LENGTH_LONG, getString(R.string.goto_book_record_detail), v -> {
                    Intent intent = new Intent(TimetableActivity.this, BookRecordActivity.class);
                    EventBus.getDefault().postSticky(new BookRecordActivity.Data(bookResult.getBookRecordId()));
                    TimetableActivity.this.startActivity(intent);
                });
            else
                SnackbarHelper.show(tabLayout, msg, Snackbar.LENGTH_LONG);
        }).book(TimetableStation.getByNo(this.searchInfo.fromStation).getBookNo(),
                TimetableStation.getByNo(this.searchInfo.toStation).getBookNo(),
                searchDate,
                e.getTrainInfo(),
                false);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final String[] titles = new String[]{
                getString(R.string.tab_name_all_class),
                getString(R.string.tab_name_exp_class),
        };
        private final Fragment[] fragments = new Fragment[2];

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void createAllClass() {
            fragments[0] = TimetableFragment.newInstance();
        }

        public void createExpressClass() {
            fragments[1] = TimetableFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
