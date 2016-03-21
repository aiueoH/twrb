package com.dowob.twrb.features.tickets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.dowob.twrb.R;
import com.dowob.twrb.features.shared.NetworkChecker;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.features.tickets.book.Booker;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.List;

import rx.Observable;
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

    public void book(long bookRecordId) {
        if (!NetworkChecker.isConnected(activity)) {
            SnackbarHelper.show(parentView, activity.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        Observable.just(bookRecordId)
                .map(id -> {
                    BookRecordModel.getInstance().book(activity, id, this);
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void showRequireRandomInputDialog(Bitmap captcha, BookRecordModel.RandomInputReceiver randomInputReceiver) {
        final View view = LayoutInflater.from(activity).inflate(R.layout.require_randominput, null);
        ImageView captcha_imageView = (ImageView) view.findViewById(R.id.imageView_captcha);
        captcha_imageView.setImageBitmap(captcha);
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("送出", (dialog, which) -> onSendButtonClick(randomInputReceiver, view))
                .setOnCancelListener(dialog -> randomInputReceiver.answerRandomInput(""))
                .show();
    }

    private void onSendButtonClick(BookRecordModel.RandomInputReceiver randomInputReceiver, View view) {
        EditText editText = (EditText) view.findViewById(R.id.editText_randInput);
        Observable.just(editText.getText().toString())
                .map(r -> {
                    randomInputReceiver.answerRandomInput(r);
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(n -> dismissProgressDialog());
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.is_booking));
    }

    private void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void onRequireRandomInput(BookRecordModel.RandomInputReceiver randomInputReceiver, ByteArrayOutputStream captcha) {
        Bitmap captcha_bitmap = BitmapFactory.decodeByteArray(captcha.toByteArray(), 0, captcha.size());
        activity.runOnUiThread(() -> {
            dismissProgressDialog();
            showRequireRandomInputDialog(captcha_bitmap, randomInputReceiver);
        });
    }

    @Override
    public void onFinish(AbstractMap.SimpleEntry<Booker.Result, List<String>> result) {
        listener.onFinish(result);
    }

    interface Listener {
        void onFinish(AbstractMap.SimpleEntry<Booker.Result, List<String>> result);
    }
}