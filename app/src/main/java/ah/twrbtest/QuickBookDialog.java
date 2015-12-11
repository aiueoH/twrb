package ah.twrbtest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.helpers.IDCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;

public class QuickBookDialog extends Dialog {
    @Bind(R.id.editText_id)
    EditText id_editText;
    @Bind(R.id.spinner_qtu)
    Spinner qtu_spinner;
    @Bind(R.id.button_book)
    Button book_button;
    @Bind(R.id.button_save)
    Button save_button;

    private Context context;
    private BookingInfo bookingInfo;
    private SimpleAdapter qtuAdapter;

    public QuickBookDialog(Context context, BookingInfo bookingInfo) {
        super(context);
        this.context = context;
        this.bookingInfo = bookingInfo;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.quickbook);
        ButterKnife.bind(this);
        buildQtuAdapter();
        SharedPreferences sp = this.context.getSharedPreferences("twrbtest", Activity.MODE_PRIVATE);
        String id = sp.getString("personId", "");
        int qtu = sp.getInt("qtu", 1);
        this.id_editText.setText(id);
        this.qtu_spinner.setAdapter(this.qtuAdapter);
        this.qtu_spinner.setSelection(qtu + 1);
    }

    @OnItemSelected(R.id.spinner_qtu)
    public void onQtuSpinnerItemSelected(int position) {
        SharedPreferences sp = this.context.getSharedPreferences("twrbtest", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("qtu", position - 1);
        editor.commit();
    }

    @OnTextChanged(R.id.editText_id)
    public void onIdEditTextChang(CharSequence s) {
        SharedPreferences sp = this.context.getSharedPreferences("twrbtest", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("personId", s.toString());
        editor.commit();
    }

    @OnClick(R.id.textView_id)
    public void onIdTextViewClick() {
        this.id_editText.setText(IDCreator.create());
    }

    @OnClick(R.id.button_save)
    public void onSaveButtonClick() {
        if (setBookingInfo()) return;
        dismiss();
        EventBus.getDefault().post(new OnSavingEvent(this.bookingInfo));
    }

    @OnClick(R.id.button_book)
    public void onBookButtonClick() {
        if (setBookingInfo()) return;
        dismiss();
        EventBus.getDefault().post(new OnBookingEvent(this.bookingInfo));
    }

    private boolean setBookingInfo() {
        String id = this.id_editText.getText().toString();
        if (!IDCreator.check(id)) {
            Toast.makeText(this.context, "你不要用假的身分證字號好不好", Toast.LENGTH_SHORT).show();
            return true;
        }
        this.bookingInfo.PERSON_ID = id;
        this.bookingInfo.ORDER_QTU_STR = String.valueOf(this.qtu_spinner.getSelectedItemPosition() + 1);
        return false;
    }

    private void buildQtuAdapter() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("qtu", i);
            items.add(item);
        }
        this.qtuAdapter = new SimpleAdapter(this.context, items, R.layout.item_qtu, new String[]{"qtu"}, new int[]{R.id.textView});
    }

    private static class OnFinishEvent {
        private BookingInfo bookingInfo;

        public OnFinishEvent(BookingInfo bookingInfo) {
            this.bookingInfo = bookingInfo;
        }

        public BookingInfo getBookingInfo() {
            return bookingInfo;
        }
    }

    public static class OnSavingEvent extends OnFinishEvent {
        public OnSavingEvent(BookingInfo bookingInfo) {
            super(bookingInfo);
        }
    }

    public static class OnBookingEvent extends OnFinishEvent {
        public OnBookingEvent(BookingInfo bookingInfo) {
            super(bookingInfo);
        }
    }
}
