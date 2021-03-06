package com.dowob.twrb.features.tickets;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.database.BookableStation;
import com.dowob.twrb.database.TimetableStation;
import com.dowob.twrb.database.TrainInfo;
import com.dowob.twrb.features.shared.NetworkChecker;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.utils.Util;
import com.jakewharton.rxbinding.view.RxView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookRecordActivity extends AppCompatActivity implements BookRecordModel.Observer {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.textView_date)
    TextView date_textView;
    @Bind(R.id.textView_no)
    TextView no_textView;
    @Bind(R.id.textView_trainType)
    TextView trainType_textView;
    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    @Bind(R.id.textView_arrivalTime)
    TextView arrival_textView;
    @Bind(R.id.textView_departureTime)
    TextView departureTime_textView;
    @Bind(R.id.textView_fare)
    TextView fare_textView;
    @Bind(R.id.textView_totalPrice)
    TextView totalPrice_textView;
    @Bind(R.id.linearLayout_fare)
    LinearLayout fare_linearLayout;
    @Bind(R.id.linearLayout_totalPrice)
    LinearLayout totalPrice_linearLayout;
    @Bind(R.id.textView_qtu)
    TextView qtu_textView;
    @Bind(R.id.textView_personid)
    TextView personId_textView;
    @Bind(R.id.textView_code)
    TextView code_textView;
    @Bind(R.id.linearLayout_code)
    LinearLayout code_linearLayout;
    @Bind(R.id.button_cancel)
    Button cancel_button;
    @Bind(R.id.button_delete)
    Button delete_button;
    @Bind(R.id.button_book)
    Button book_button;
    @Bind(R.id.linearLayout_isBooked)
    LinearLayout isBooked_linearLayout;
    @Bind(R.id.linearLayout_isCancelled)
    LinearLayout isCancelled_linearLayout;
    @Bind(R.id.layout_trainType)
    LinearLayout trainType_layout;
    @Bind(R.id.imageView_mainSpace)
    ImageView mainSpace_imageView;
    @Bind(R.id.relativeLayout_mainSpace)
    RelativeLayout mainSpace_relativeLayout;

    private View parentView;

    private BookRecord bookRecord;
    private ProgressDialog progressDialog;
    private BookRecordModel bookRecordModel;
    private boolean isWaitingRandomInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookrecord);
        ButterKnife.bind(this);
        parentView = trainType_layout;
        bookRecordModel = BookRecordModel.getInstance();
        bookRecordModel.registerObserver(this);
        getBookRecord();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bookRecordModel.unregisterObserver(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getBookRecord() {
        Data data = EventBus.getDefault().getStickyEvent(Data.class);
        EventBus.getDefault().removeStickyEvent(data);
        bookRecord = BookRecord.get(data.getBookRecordId());
    }

    private void updateUI() {
        setupToolbar();
        date_textView.setText(new SimpleDateFormat("yyyy/MM/dd E").format(bookRecord.getGetInDate()));
        no_textView.setText(bookRecord.getTrainNo());
        from_textView.setText(BookableStation.getNameByNo(bookRecord.getFromStation()));
        to_textView.setText(BookableStation.getNameByNo(bookRecord.getToStation()));
        qtu_textView.setText(Integer.toString(bookRecord.getOrderQtu()));
        personId_textView.setText(bookRecord.getPersonId());
        code_textView.setText(bookRecord.getCode());
        code_linearLayout.setVisibility(bookRecord.getCode().isEmpty() ? View.GONE : View.VISIBLE);
        book_button.setVisibility(!bookRecord.getCode().isEmpty() || bookRecord.isCancelled() ? View.INVISIBLE : View.VISIBLE);
        cancel_button.setVisibility(bookRecord.getCode().isEmpty() || bookRecord.isCancelled() ? View.GONE : View.VISIBLE);
        isBooked_linearLayout.setVisibility(bookRecord.getCode().isEmpty() || bookRecord.isCancelled() ? View.GONE : View.VISIBLE);
        isCancelled_linearLayout.setVisibility(bookRecord.isCancelled() ? View.VISIBLE : View.GONE);
        int cityNo = Integer.parseInt(TimetableStation.getByBookNo(bookRecord.getToStation()).getCityNo());
        mainSpace_imageView.setImageResource(Util.getCityDrawableId(cityNo));
        setTrainType();
        setArrivalTime();
        setDepartureTime();
        setFareAndTotalPrice();
        RxView.clicks(delete_button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
                    onDeleteButtonClick();
                });
        RxView.clicks(cancel_button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
                    onCancelButtonClick();
                });
        RxView.clicks(book_button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
                    onBookButtonClick();
                });
    }

    private void onDeleteButtonClick() {
        Calendar departureDateTime = Calendar.getInstance();
        Date getInDate = bookRecord.getGetInDate();
        Date departureTime = bookRecord.getTrainInfo().getDepartureDateTime();
        departureDateTime.set(getInDate.getYear() + 1900, getInDate.getMonth(), getInDate.getDate(), departureTime.getHours(), departureTime.getMinutes(), departureTime.getSeconds());
        boolean isBuyable = BookRecord.isBuyable(departureDateTime, Calendar.getInstance());
        boolean isBooked = !TextUtils.isEmpty(bookRecord.getCode());
        if (isBooked && isBuyable)
            showCheckingDeletionDialog();
        else
            deleteAndCloseActivity();
    }

    private void showCheckingDeletionDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.check_before_delete_book_record_when_uncancelled)
                .setPositiveButton("刪除", (dialog, which) -> deleteAndCloseActivity())
                .setNeutralButton("不要刪除", null)
                .show();
    }

    private void deleteAndCloseActivity() {
        BookRecordModel.getInstance().delete(bookRecord.getId());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setSharedElementsUseOverlay(false);
        onBackPressed();
    }

    private void setTrainType() {
        TrainInfo trainInfo = bookRecord.getTrainInfo();
        if (trainInfo != null) {
            trainType_layout.setVisibility(View.VISIBLE);
            trainType_textView.setText(trainInfo.getTrainType());
        } else
            trainType_layout.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.toolbar_title_ticketDetail));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setArrivalTime() {
        TrainInfo trainInfo = bookRecord.getTrainInfo();
        if (trainInfo != null) {
            String time = new SimpleDateFormat("HH:mm").format(trainInfo.getArrivalDateTime());
            arrival_textView.setVisibility(View.VISIBLE);
            arrival_textView.setText(time);
        } else {
            arrival_textView.setVisibility(View.GONE);
        }
    }

    private void setDepartureTime() {
        TrainInfo trainInfo = bookRecord.getTrainInfo();
        if (trainInfo != null) {
            String time = new SimpleDateFormat("HH:mm").format(trainInfo.getDepartureDateTime());
            departureTime_textView.setVisibility(View.VISIBLE);
            departureTime_textView.setText(time);
        } else {
            departureTime_textView.setVisibility(View.GONE);
        }
    }

    private void setFareAndTotalPrice() {
        TrainInfo trainInfo = bookRecord.getTrainInfo();
        if (trainInfo != null) {
            fare_linearLayout.setVisibility(View.VISIBLE);
            fare_textView.setText(Integer.toString(trainInfo.getFares()));
            totalPrice_linearLayout.setVisibility(View.VISIBLE);
            totalPrice_textView.setText(Integer.toString(trainInfo.getFares() * bookRecord.getOrderQtu()));
        } else {
            fare_linearLayout.setVisibility(View.GONE);
            totalPrice_linearLayout.setVisibility(View.GONE);
        }
    }

    private void onBookButtonClick() {
        new BookFlowController(this, parentView, result -> SnackbarHelper.show(parentView, result.getStatusMsg(this), Snackbar.LENGTH_LONG))
                .book(bookRecord.getId());
    }

    private void onCancelButtonClick() {
        if (!NetworkChecker.isConnected(this)) {
            SnackbarHelper.show(parentView, getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(R.string.check_before_cancel_book_record)
                .setPositiveButton("退訂", (dialog, which) -> cancelBookRecord())
                .setNeutralButton("我還不想退訂", null)
                .show();
    }

    private void cancelBookRecord() {
        Observable.just(bookRecord.getId())
                .map(bookRecordModel::cancel)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> progressDialog = ProgressDialog.show(this, "", "退票中"))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCanceled);
    }

    private void onCanceled(Boolean result) {
        if (progressDialog != null)
            progressDialog.dismiss();
        String s = getString(R.string.cancel_suc);
        if (!result)
            s = getString(R.string.cancel_fale);
        SnackbarHelper.show(parentView, s, Snackbar.LENGTH_LONG);
    }

    @Override
    public void notifyBookRecordUpdate(long bookRecordId) {
        runOnUiThread(() -> {
            if (bookRecord.getId() == bookRecordId)
                updateUI();
        });
    }

    @Override
    public void notifyBookRecordRemove(long bookRecordId) {

    }

    public static class Data {
        private long bookRecordId;

        public Data(long bookRecordId) {
            this.bookRecordId = bookRecordId;
        }

        public long getBookRecordId() {
            return bookRecordId;
        }
    }
}
