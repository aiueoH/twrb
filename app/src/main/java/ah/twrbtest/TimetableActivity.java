package ah.twrbtest;

import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.TextView;

import com.twrb.core.book.BookInfo;
import com.twrb.core.book.BookResult;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.DBObject.TimetableStation;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Events.OnSearchedEvent;
import ah.twrbtest.Fragments.TimetableFragment;
import ah.twrbtest.Helper.BookManager;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TimetableActivity extends AppCompatActivity {
    //    @Bind(R.id.recyclerView)
//    RecyclerView recyclerView;
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
    private ArrayList<TrainInfo> trainInfos;
    private ProgressDialog mProgressDialog;

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
        this.from_textView.setText(TimetableStation.get(this.searchInfo.fromStation).getNameCh());
        this.to_textView.setText(TimetableStation.get(this.searchInfo.toStation).getNameCh());

        ViewPagerAdapter vpa = new ViewPagerAdapter(getSupportFragmentManager());
        EventBus.getDefault().postSticky(new TimetableFragment.OnPassingTrainInfoEvent(trainInfos, searchDate));
        vpa.createAllClass();
        List<TrainInfo> expressTrainInfos = getExpressClassTrainInfos();
        EventBus.getDefault().postSticky(new TimetableFragment.OnPassingTrainInfoEvent(expressTrainInfos, searchDate));
        vpa.createExpressClass();
        viewPager.setAdapter(vpa);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        TrainInfo ti = e.getTrainInfo();
        BookInfo bi = new BookInfo();
        bi.trainNo = ti.no;
        bi.fromStation = TimetableStation.get(this.searchInfo.fromStation).getBookNo();
        bi.toStation = TimetableStation.get(this.searchInfo.toStation).getBookNo();
        bi.getinDate = this.searchInfo.searchDate;
        new QuickBookDialog(this, bi).show();
    }

    public void onEvent(QuickBookDialog.OnBookingEvent e) {
        BookRecord bookRecord = BookRecordFactory.createBookRecord(e.getBookInfo());
        if (BookRecord.isBookable(bookRecord, Calendar.getInstance())) {
            Observable.just(bookRecord.getId())
                    .map(id -> BookManager.book(id))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(() -> mProgressDialog = ProgressDialog.show(this, "", "訂票中"))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        mProgressDialog.dismiss();
                        if (result == null)
                            result = BookResult.UNKNOWN;
                        EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
                        EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result));
                        String s = result.equals(BookResult.OK) ? "訂票成功！" : "訂票失敗，已加入待訂清單";
                        Snackbar.make(viewPager, s, Snackbar.LENGTH_LONG).show();
                    });
        } else {
            EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
            Snackbar.make(viewPager, "還沒開放訂票，我就自作主張先加入待訂清單了，不用謝", Snackbar.LENGTH_LONG).show();
        }
    }

    public void onEvent(QuickBookDialog.OnSavingEvent e) {
        long brId = BookRecordFactory.createBookRecord(e.getBookInfo()).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(brId));
        Snackbar.make(viewPager, "已加入待訂清單，手續費三百大洋", Snackbar.LENGTH_SHORT).show();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final String[] titles = new String[]{
                "所有車種",
                "對號列車",
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
