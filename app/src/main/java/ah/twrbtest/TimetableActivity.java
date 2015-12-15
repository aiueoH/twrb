package ah.twrbtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

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
import ah.twrbtest.Helper.NotifiableAsyncTask;
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
            AsyncBookHelper abh = new AsyncBookHelper(bookRecord);
            abh.setOnPostExecuteListener(new OnBookedListener(bookRecord.getId()));
            abh.execute();
        } else {
            EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
            Snackbar.make(recyclerView, "還沒開放訂票，我就自作主張先加入待訂清單了，不用謝", Snackbar.LENGTH_LONG).show();
        }
    }

    public void onEvent(QuickBookDialog.OnSavingEvent e) {
        long brId = BookRecordFactory.createBookRecord(e.getBookingInfo()).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(brId));
        Snackbar.make(recyclerView, "已加入待訂清單，手續費三百大洋", Snackbar.LENGTH_SHORT).show();
    }

    class OnBookedListener implements AsyncBookHelper.OnPostExecuteListener {
        private long id;

        public OnBookedListener(long id) {
            this.id = id;
        }

        @Override
        public void onPostExecute(NotifiableAsyncTask notifiableAsyncTask) {
            Boolean result = (Boolean) notifiableAsyncTask.getResult();
            if (result == null)
                result = false;
            EventBus.getDefault().post(new OnBookRecordAddedEvent(this.id));
            EventBus.getDefault().post(new OnBookedEvent(this.id, result));
            mProgressDialog.dismiss();
            String s = result ? "訂票成功！" : "訂票失敗，已加入待訂清單";
            Snackbar.make(recyclerView, s, Snackbar.LENGTH_LONG).show();
        }
    }
}
