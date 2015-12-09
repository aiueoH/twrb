package ah.twrbtest;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.helpers.IDCreator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.DBObject.BookableStation;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Helper.AsyncBookHelper;
import ah.twrbtest.MyArrayAdapter.BookableStationArrayAdapter;
import ah.twrbtest.MyArrayAdapter.DateArrayAdapter;
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
    private long bookingId = 0;

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
        System.out.println("BookTicketFragment onResume");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("BookTicketFragment onPause");
        EventBus.getDefault().unregister(this);
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

    private BookingInfo getBookingInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        BookingInfo info = new BookingInfo();
        info.PERSON_ID = this.id_editText.getText().toString();
        info.TRAIN_NO = this.no_editText.getText().toString();
        info.GETIN_DATE = dateFormat.format((Date) this.date_spinner.getSelectedItem());
        info.FROM_STATION = ((BookableStation) ((Spinner) getView().findViewById(R.id.spinner_from)).getSelectedItem()).getNo();
        info.TO_STATION = ((BookableStation) ((Spinner) getView().findViewById(R.id.spinner_to)).getSelectedItem()).getNo();
        info.ORDER_QTU_STR = "" + (((Spinner) getView().findViewById(R.id.spinner_qtu)).getSelectedItemPosition() + 1);
        return info;
    }

    public void book() {
        this.mProgressDialog = ProgressDialog.show(getActivity(), "", "訂票中");
        final BookingInfo info = getBookingInfo();
        BookRecord bookRecord = saveToDB(info);
        new AsyncBookHelper(bookRecord).execute((long) 0);
        bookingId = bookRecord.getId();
    }

    public void save() {
        BookingInfo info = getBookingInfo();
        long id = saveToDB(info).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(id));
        Toast.makeText(getActivity(), "已加入待訂清單", Toast.LENGTH_SHORT).show();
    }

    public BookRecord saveToDB(BookingInfo info) {
        Realm.getDefaultInstance().beginTransaction();
        BookRecord bookRecord = new BookRecord();
        AdaptHelper.to(info, bookRecord);
        Realm.getDefaultInstance().copyToRealm(bookRecord);
        Realm.getDefaultInstance().commitTransaction();
        return bookRecord;
    }

    public void onEvent(OnBookedEvent e) {
        if (e.getBookRecordId() == this.bookingId) {
            EventBus.getDefault().post(new OnBookRecordAddedEvent(this.bookingId));
            String result = e.isSuccess() ? "訂票成功！" : "訂票失敗，已加入待訂清單";
            this.mProgressDialog.dismiss();
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }
}
