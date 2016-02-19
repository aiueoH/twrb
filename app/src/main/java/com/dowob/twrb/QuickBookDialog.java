package com.dowob.twrb;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.dowob.twrb.Helper.BookManager;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.twrb.core.book.BookInfo;
import com.twrb.core.helpers.IDCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
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
    private BookInfo bookInfo;
    private SimpleAdapter qtuAdapter;

    public QuickBookDialog(Context context, BookInfo bookInfo) {
        super(context);
        this.context = context;
        this.bookInfo = bookInfo;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.quickbook);
        ButterKnife.bind(this);
        buildQtuAdapter();
        SharedPreferences sp = this.context.getSharedPreferences(context.getString(R.string.sp_name), Activity.MODE_PRIVATE);
        String id = sp.getString("personId", "");
        int qtu = sp.getInt("qtu", -1);
        this.id_editText.setText(id);
        this.qtu_spinner.setAdapter(this.qtuAdapter);
        this.qtu_spinner.setSelection(qtu - 1);

        RxTextView.textChanges(id_editText).throttleLast(500, TimeUnit.MILLISECONDS).subscribe(s -> onIdEditTextChang(s));
        RxAdapterView.itemSelections(qtu_spinner).subscribe(i -> onQtuSpinnerItemSelected(i));
        RxView.clicks(save_button).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> onSaveButtonClick());
        RxView.clicks(book_button).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> onBookButtonClick());
    }

    public void onQtuSpinnerItemSelected(int position) {
        SharedPreferences sp = this.context.getSharedPreferences(context.getString(R.string.sp_name), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("qtu", position + 1);
        editor.commit();
    }

    public void onIdEditTextChang(CharSequence s) {
        SharedPreferences sp = this.context.getSharedPreferences(context.getString(R.string.sp_name), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("personId", s.toString());
        editor.commit();
        System.out.println("qq");
    }

//    @OnClick(R.id.textView_id)
//    public void onIdTextViewClick() {
//        this.id_editText.setText(IDCreator.create());
//    }

    public void onSaveButtonClick() {
        if (setBookingInfo()) return;
        dismiss();
        EventBus.getDefault().post(new OnSavingEvent(this.bookInfo));
    }

    public void onBookButtonClick() {
        if (!NetworkChecker.isConnected(context)) {
            SnackbarHelper.show(qtu_spinner, context.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        if (setBookingInfo())
            return;

        int remainCDTime = BookManager.getBookCDTime(context);
        if (remainCDTime > 0) {
            String s = String.format(context.getString(R.string.cold_down_msg), remainCDTime);
            SnackbarHelper.show(qtu_spinner, s, Snackbar.LENGTH_LONG);
            return;
        }
        dismiss();
        EventBus.getDefault().post(new OnBookingEvent(this.bookInfo));
    }

    private boolean setBookingInfo() {
        String id = this.id_editText.getText().toString();
        if (!IDCreator.check(id)) {
            SnackbarHelper.show(qtu_spinner, context.getString(R.string.tip_for_wrong_id), Snackbar.LENGTH_LONG);
            return true;
        }
        this.bookInfo.personId = id;
        this.bookInfo.orderQtuStr = String.valueOf(this.qtu_spinner.getSelectedItemPosition() + 1);
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
        private BookInfo bookInfo;

        public OnFinishEvent(BookInfo bookInfo) {
            this.bookInfo = bookInfo;
        }

        public BookInfo getBookInfo() {
            return bookInfo;
        }
    }

    public static class OnSavingEvent extends OnFinishEvent {
        public OnSavingEvent(BookInfo bookInfo) {
            super(bookInfo);
        }
    }

    public static class OnBookingEvent extends OnFinishEvent {
        public OnBookingEvent(BookInfo bookInfo) {
            super(bookInfo);
        }
    }
}
