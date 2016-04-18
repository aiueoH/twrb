package com.dowob.twrb.features.tickets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dowob.twrb.BuildConfig;
import com.dowob.twrb.R;
import com.dowob.twrb.features.shared.NetworkChecker;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.features.tickets.book.BookResult;
import com.dowob.twrb.utils.Config;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.twrb.core.book.BookInfo;
import com.twrb.core.helpers.IDCreator;
import com.twrb.core.timetable.TrainInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookFlowController implements BookRecordModel.BookListener {
    private Activity activity;
    private View parentView;
    private Listener listener;
    private ProgressDialog progressDialog;

    public BookFlowController(Activity activity, View parentView, Listener listener) {
        this.activity = activity;
        this.parentView = parentView;
        this.listener = listener;
    }

    /***
     * Called by Timetable activity. Will auto create a book record when book successfully.
     *
     * @param from
     * @param to
     * @param getInDate
     * @param trainInfo
     * @param isBackground
     */
    public void book(String from, String to, Calendar getInDate, TrainInfo trainInfo, boolean isBackground) {
        if (!NetworkChecker.isConnected(activity)) {
            SnackbarHelper.show(parentView, activity.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        final View view = createPersonIdAndQtyFormView();
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(activity.getString(R.string.book_button),
                        (dialog, which) -> onBookButtonClick(view, from, to, getInDate, trainInfo, isBackground))
                .setNegativeButton(activity.getString(R.string.save_to_my_tickets_button),
                        ((dialog1, which1) -> onSaveButtonClick(view, from, to, getInDate, trainInfo)))
                .show();
    }

    private String getPersonId(View view) {
        return ((EditText) view.findViewById(R.id.editText_personId)).getText().toString();
    }

    private int getQty(View view) {
        return ((Spinner) view.findViewById(R.id.spinner_qty)).getSelectedItemPosition() + 1;
    }

    private void onBookButtonClick(View view, String from, String to, Calendar getInDate, TrainInfo trainInfo, boolean isBackground) {
        book(from, to, getInDate, getQty(view), getPersonId(view), trainInfo, isBackground);
    }

    private void onSaveButtonClick(View view, String from, String to, Calendar getInDate, TrainInfo trainInfo) {
        BookInfo bookInfo = new BookInfo();
        bookInfo.trainNo = trainInfo.no;
        bookInfo.fromStation = from;
        bookInfo.toStation = to;
        bookInfo.getinDate = new SimpleDateFormat("yyyy/MM/dd").format(getInDate.getTime());
        bookInfo.personId = getPersonId(view);
        bookInfo.orderQtuStr = String.valueOf(getQty(view));
        BookRecordModel.getInstance().save(bookInfo, trainInfo);
        Snackbar.make(parentView, activity.getString(R.string.saved_to_book_record), Snackbar.LENGTH_LONG).show();
    }

    private View createPersonIdAndQtyFormView() {
        View view = LayoutInflater.from(activity).inflate(R.layout.form_personid_qty, null);
        EditText personId_editText = (EditText) view.findViewById(R.id.editText_personId);
        Spinner qty_spinner = (Spinner) view.findViewById(R.id.spinner_qty);
        SharedPreferences sp = activity.getSharedPreferences(Config.SHARE_PREFERENCE, Activity.MODE_PRIVATE);
        String defaultPersonId = sp.getString(Config.PREFERENCE_PERSONID, "");
        int defaultQty = sp.getInt(Config.PREFERENCE_QTU, -1);
        personId_editText.setText(defaultPersonId);
        qty_spinner.setAdapter(createQtyAdapter());
        qty_spinner.setSelection(defaultQty - 1);
        RxTextView.textChanges(personId_editText).throttleLast(500, TimeUnit.MILLISECONDS).subscribe(this::onIdEditTextChang);
        RxAdapterView.itemSelections(qty_spinner).subscribe(this::onQtuSpinnerItemSelected);
        if (BuildConfig.DEBUG) {
            TextView personId_textView = (TextView) view.findViewById(R.id.textView_personId);
            RxView.clicks(personId_textView)
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .subscribe(v -> personId_editText.setText(IDCreator.create()));
        }
        return view;
    }

    public void onQtuSpinnerItemSelected(int position) {
        SharedPreferences sp = activity.getSharedPreferences(Config.SHARE_PREFERENCE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Config.PREFERENCE_QTU, position + 1);
        editor.commit();
    }

    public void onIdEditTextChang(CharSequence s) {
        SharedPreferences sp = activity.getSharedPreferences(Config.SHARE_PREFERENCE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.PREFERENCE_PERSONID, s.toString());
        editor.commit();
    }

    private void book(String from, String to, Calendar getInDate, int qty, String personId, TrainInfo trainInfo, boolean isBackground) {
        Scheduler scheduler = isBackground ? Schedulers.io() : AndroidSchedulers.mainThread();
        Observable.just(null)
                .map(nothing -> {
                    BookRecordModel.getInstance().book(activity, from, to, getInDate, trainInfo.no, qty, personId, trainInfo, BookFlowController.this);
                    return null;
                })
                .subscribeOn(scheduler)
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private SimpleAdapter createQtyAdapter() {
        List<Map<String, Object>> items = new ArrayList<>();
        String itemName = "qtu";
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put(itemName, i);
            items.add(item);
        }
        return new SimpleAdapter(activity, items, R.layout.item_qtu, new String[]{itemName}, new int[]{R.id.textView});
    }

    // Book in my ticket page.
    public void book(long bookRecordId) {
        book(bookRecordId, false);
    }

    // Book in my ticket page.
    public void book(long bookRecordId, boolean isBackground) {
        if (!NetworkChecker.isConnected(activity)) {
            SnackbarHelper.show(parentView, activity.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        Scheduler scheduler = isBackground ? Schedulers.io() : AndroidSchedulers.mainThread();
        Observable.just(bookRecordId)
                .map(id -> {
                    BookRecordModel.getInstance().book(activity, id, this);
                    return null;
                })
                .subscribeOn(scheduler)
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void showRequireRandomInputDialog(Bitmap captcha, BookRecordModel.RandomInputReceiver randomInputReceiver) {
        final View view = LayoutInflater.from(activity).inflate(R.layout.require_randominput, null);
        ImageView captcha_imageView = (ImageView) view.findViewById(R.id.imageView_captcha);
        captcha_imageView.setImageBitmap(captcha);
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("送出", (dialog, which) -> onSendButtonClick(randomInputReceiver, view))
                .setOnCancelListener(dialog -> randomInputReceiver.answerRandomInput(null))
                .show();
    }

    private void onSendButtonClick(BookRecordModel.RandomInputReceiver randomInputReceiver, View view) {
        onSendButtonClick(randomInputReceiver, view, false);
    }

    private void onSendButtonClick(BookRecordModel.RandomInputReceiver randomInputReceiver, View view, boolean isBackground) {
        EditText editText = (EditText) view.findViewById(R.id.editText_randInput);
        Scheduler scheduler = isBackground ? Schedulers.io() : AndroidSchedulers.mainThread();
        Observable.just(editText.getText().toString())
                .map(r -> {
                    randomInputReceiver.answerRandomInput(r);
                    return null;
                })
                .subscribeOn(scheduler)
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.is_booking));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void onRequireRandomInput(BookRecordModel.RandomInputReceiver randomInputReceiver, Bitmap captcha) {
        activity.runOnUiThread(() -> {
            dismissProgressDialog();
            showRequireRandomInputDialog(captcha, randomInputReceiver);
        });
    }

    @Override
    public void onFinish(BookResult bookResult) {
        dismissProgressDialog();
        listener.onFinish(bookResult);
    }

    public interface Listener {
        void onFinish(BookResult bookResult);
    }
}