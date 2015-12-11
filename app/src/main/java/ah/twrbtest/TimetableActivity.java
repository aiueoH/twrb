package ah.twrbtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.DBObject.TimetableStation;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Events.OnSearchedEvent;
import ah.twrbtest.Helper.AsyncBookHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TimetableActivity extends Activity {
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    @Bind(R.id.textView_date)
    TextView date_textView;

    private SearchInfo searchInfo;
    private ProgressDialog mProgressDialog;
    private long bookingId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_timetable);
        ButterKnife.bind(this);

        OnSearchedEvent e = EventBus.getDefault().getStickyEvent(OnSearchedEvent.class);
        ArrayList<TrainInfo> trainInfos = e.getTrainInfos();
        this.searchInfo = e.getSearchInfo();

        String date = "";
        try {
            DateFormat input = new SimpleDateFormat("yyyy/MM/dd");
            DateFormat output = new SimpleDateFormat("yyyy/MM/dd E");
            date = output.format(input.parse(this.searchInfo.SEARCHDATE));
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        this.date_textView.setText(date);
        this.from_textView.setText(TimetableStation.get(this.searchInfo.FROMSTATION).getNameCh());
        this.to_textView.setText(TimetableStation.get(this.searchInfo.TOSTATION).getNameCh());

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(new TrainInfoAdapter(this, trainInfos));
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
        BookingInfo bi = new BookingInfo();
        bi.TRAIN_NO = ti.NO;
        bi.FROM_STATION = TimetableStation.get(this.searchInfo.FROMSTATION).getBookNo();
        bi.TO_STATION = TimetableStation.get(this.searchInfo.TOSTATION).getBookNo();
        bi.GETIN_DATE = this.searchInfo.SEARCHDATE;
        new QuickBookDialog(this, bi).show();
    }

    public void onEvent(QuickBookDialog.OnBookingEvent e) {
        BookRecord bookRecord = BookRecordFactory.createBookRecord(e.getBookingInfo());
        if (BookRecord.isBookable(bookRecord, Calendar.getInstance())) {
            this.mProgressDialog = ProgressDialog.show(this, "", "訂票中");
            bookingId = bookRecord.getId();
            new AsyncBookHelper(bookRecord).execute((long) 0);
        } else
            Toast.makeText(this, "還沒開放訂票，我先把他加入待訂清單哦", Toast.LENGTH_LONG).show();
    }

    public void onEvent(QuickBookDialog.OnSavingEvent e) {
        long brId = BookRecordFactory.createBookRecord(e.getBookingInfo()).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(brId));
        Toast.makeText(this, "已加入待訂清單", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(OnBookedEvent e) {
        if (e.getBookRecordId() == this.bookingId) {
            EventBus.getDefault().post(new OnBookRecordAddedEvent(this.bookingId));
            String result = e.isSuccess() ? "訂票成功！" : "訂票失敗，已加入待訂清單";
            this.mProgressDialog.dismiss();
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
