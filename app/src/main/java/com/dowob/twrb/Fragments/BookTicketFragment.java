package com.dowob.twrb.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.dowob.twrb.BookRecordFactory;
import com.dowob.twrb.DBObject.BookRecord;
import com.dowob.twrb.DBObject.BookableStation;
import com.dowob.twrb.Events.OnBookRecordAddedEvent;
import com.dowob.twrb.MyArrayAdapter.BookableStationArrayAdapter;
import com.dowob.twrb.MyArrayAdapter.DateArrayAdapter;
import com.dowob.twrb.R;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.helpers.IDCreator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

public class BookTicketFragment extends Fragment {

    EditText id_editText;
    EditText no_editText;
    Spinner date_spinner;
    Spinner from_spinner;
    Spinner to_spinner;
    Spinner qtu_spinner;
    Button submit_button;
    Button save_button;

    private BookableStationArrayAdapter bookableStationArrayAdapter;
    private DateArrayAdapter dateArrayAdapter;
    private SimpleAdapter qtuAdapter;
    private ProgressDialog mProgressDialog;

    public static BookTicketFragment newInstance() {
        return new BookTicketFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildBookableStationArrayAdapter();
        buildDateArrayAdapter();
        buildQtuAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLogger.i("BookTicketFragment onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLogger.i("BookTicketFragment onPause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookticket, container, false);

        this.id_editText = ButterKnife.findById(view, R.id.editText_id);
        this.no_editText = ButterKnife.findById(view, R.id.editText_no);
        this.date_spinner = ButterKnife.findById(view, R.id.spinner_date);
        this.from_spinner = ButterKnife.findById(view, R.id.spinner_from);
        this.to_spinner = ButterKnife.findById(view, R.id.spinner_to);
        this.qtu_spinner = ButterKnife.findById(view, R.id.spinner_qtu);
        this.submit_button = ButterKnife.findById(view, R.id.button_submit);
        this.save_button = ButterKnife.findById(view, R.id.button_save);

        this.from_spinner.setAdapter(this.bookableStationArrayAdapter);
        this.to_spinner.setAdapter(this.bookableStationArrayAdapter);
        this.date_spinner.setAdapter(this.dateArrayAdapter);
        this.qtu_spinner.setAdapter(this.qtuAdapter);

        this.id_editText.setText(IDCreator.create());

        this.submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book();
            }
        });
        this.save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        return view;
    }

    private void buildQtuAdapter() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("qtu", i);
            items.add(item);
        }
        this.qtuAdapter = new SimpleAdapter(getActivity(), items, R.layout.item_qtu, new String[]{"qtu"}, new int[]{R.id.textView});
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
        RealmResults<BookableStation> rr = Realm.getDefaultInstance().where(BookableStation.class).findAll();
        List<BookableStation> bss = new ArrayList<>();
        for (BookableStation bs : rr)
            bss.add(bs);
        this.bookableStationArrayAdapter = new BookableStationArrayAdapter(getActivity(), R.layout.item_bookablestation, bss);
    }

    private BookInfo getBookingInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        BookInfo info = new BookInfo();
        info.personId = this.id_editText.getText().toString();
        info.trainNo = this.no_editText.getText().toString();
        info.getinDate = dateFormat.format((Date) this.date_spinner.getSelectedItem());
        info.fromStation = ((BookableStation) ((Spinner) getView().findViewById(R.id.spinner_from)).getSelectedItem()).getNo();
        info.toStation = ((BookableStation) ((Spinner) getView().findViewById(R.id.spinner_to)).getSelectedItem()).getNo();
        info.orderQtuStr = "" + (((Spinner) getView().findViewById(R.id.spinner_qtu)).getSelectedItemPosition() + 1);
        return info;
    }

    public void book() {
//        BookInfo info = getBookingInfo();
//        if (!info.verify()) {
//            Snackbar.make(id_editText, "檢查一下你的欄位好嗎？", Snackbar.LENGTH_LONG).show();
//            return;
//        }
//        final BookRecord bookRecord = saveToDB(info);
//        if (BookRecord.isBookable(bookRecord, Calendar.getInstance())) {
//            this.mProgressDialog = ProgressDialog.show(getActivity(), "", "訂票中");
//            AsyncBookHelper abh = new AsyncBookHelper(bookRecord);
//            abh.setOnPostExecuteListener(new NotifiableAsyncTask.OnPostExecuteListener() {
//                @Override
//                public void onPostExecute(NotifiableAsyncTask notifiableAsyncTask) {
//                    BookResult result = (BookResult) notifiableAsyncTask.getResult();
//                    if (result == null)
//                        result = BookResult.UNKNOWN;
//                    EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
//                    EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result));
//                    mProgressDialog.dismiss();
//                    String s = result.equals(BookResult.OK) ? "訂票成功！" : "訂票失敗，已加入待訂清單";
//                    Snackbar.make(id_editText, s, Snackbar.LENGTH_LONG).show();
//                }
//            });
//            abh.execute();
//        } else {
//            EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
//            Snackbar.make(id_editText, "還沒開放訂票，我先把他加入待訂清單哦", Snackbar.LENGTH_LONG).show();
//        }
    }

    public void save() {
        BookInfo info = getBookingInfo();
        if (!info.verify()) {
            Snackbar.make(id_editText, "檢查一下你的欄位好嗎？", Snackbar.LENGTH_LONG).show();
            return;
        }
        long id = saveToDB(info).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(id));
        Snackbar.make(id_editText, "已加入待訂清單，手續費三百大洋", Snackbar.LENGTH_LONG).show();
    }

    public BookRecord saveToDB(BookInfo info) {
        return BookRecordFactory.createBookRecord(info);
    }
}
