package com.dowob.twrb.features.tickets;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.database.BookableStation;
import com.dowob.twrb.events.OnBookRecordRemovedEvent;
import com.dowob.twrb.features.shared.NetworkChecker;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.features.tickets.book.BookManager;
import com.jakewharton.rxbinding.view.RxView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookRecordAdapter extends RecyclerView.Adapter<BookRecordAdapter.MyViewHolder> {
    private Context context;
    private View parentView;
    private List<BookRecord> bookRecords;

    public BookRecordAdapter(Context context, List<BookRecord> bookRecords) {
        this.context = context;
        this.bookRecords = bookRecords;
    }

    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    public List<BookRecord> getBookRecords() {
        return bookRecords;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_bookrecord, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        try {
            onBindViewHolderImp(holder, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBindViewHolderImp(MyViewHolder holder, int position) {
        final BookRecord br = this.bookRecords.get(position);
        holder.date_textView.setText(AdaptHelper.dateToString(br.getGetInDate()));
        holder.no_textView_no.setText(br.getTrainNo());
        holder.from_textView.setText(BookableStation.getNameByNo(br.getFromStation()));
        holder.to_textView.setText(BookableStation.getNameByNo(br.getToStation()));
        holder.qtu_textView.setText(Integer.toString(br.getOrderQtu()));
        holder.personId_textView.setText(br.getPersonId());
        holder.code_textView.setText(br.getCode());
        holder.code_linearLayout.setVisibility(br.getCode().isEmpty() ? View.GONE : View.VISIBLE);
        holder.book_button.setVisibility(!br.getCode().isEmpty() || br.isCancelled() ? View.GONE : View.VISIBLE);
        holder.cancel_button.setVisibility(br.getCode().isEmpty() || br.isCancelled() ? View.GONE : View.VISIBLE);
        holder.book_button.setOnClickListener(new OnBookBtnClickListener(br));
        holder.cancel_button.setOnClickListener(new OnCancelBtnClickListener(br));
        holder.isBooked_linearLayout.setVisibility(br.getCode().isEmpty() || br.isCancelled() ? View.GONE : View.VISIBLE);
        holder.isCancelled_linearLayout.setVisibility(br.isCancelled() ? View.VISIBLE : View.GONE);
        setDeleteButton(holder, br);
        setDepartureTime(holder, br);
        setArrivalTime(holder, br);
        setFareAndTotalPrice(holder, br);
    }

    private void setDeleteButton(MyViewHolder holder, BookRecord bookRecord) {
        RxView.clicks(holder.delete_button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
                    int index = bookRecords.indexOf(bookRecord);
                    notifyItemRemoved(index);
                    bookRecords.remove(index);
                    Realm realm = Realm.getDefaultInstance();
                    BookRecord tmp = realm.where(BookRecord.class).equalTo("id", bookRecord.getId()).findFirst();
                    if (tmp == null) return;
                    realm.beginTransaction();
                    tmp.removeFromRealm();
                    realm.commitTransaction();
                    EventBus.getDefault().post(new OnBookRecordRemovedEvent());
                });
    }

    private void setFareAndTotalPrice(MyViewHolder holder, BookRecord br) {
        if (br.getFares() != 0) {
            holder.fare_linearLayout.setVisibility(View.VISIBLE);
            holder.fare_textView.setText(Integer.toString(br.getFares()));
            holder.totalPrice_linearLayout.setVisibility(View.VISIBLE);
            holder.totalPrice_textView.setText(Integer.toString(br.getFares() * br.getOrderQtu()));
        } else {
            holder.fare_linearLayout.setVisibility(View.GONE);
            holder.totalPrice_linearLayout.setVisibility(View.GONE);
        }
    }

    private void setArrivalTime(MyViewHolder holder, BookRecord bookRecord) {
        if (bookRecord.getArrivalDateTime() != null) {
            String time = new SimpleDateFormat("HH:mm").format(bookRecord.getArrivalDateTime());
            holder.arrival_textView.setVisibility(View.VISIBLE);
            holder.arrival_textView.setText(time);
        } else {
            holder.arrival_textView.setVisibility(View.GONE);
        }
    }

    private void setDepartureTime(MyViewHolder holder, BookRecord bookRecord) {
        if (bookRecord.getDepartureDateTime() != null) {
            String time = new SimpleDateFormat("HH:mm").format(bookRecord.getDepartureDateTime());
            holder.departureTime_textView.setVisibility(View.VISIBLE);
            holder.departureTime_textView.setText(time);
        } else {
            holder.departureTime_textView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.bookRecords.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.textView_date)
        TextView date_textView;
        @Bind(R.id.textView_no)
        TextView no_textView_no;
        @Bind(R.id.textView_from)
        TextView from_textView;
        @Bind(R.id.textView_to)
        TextView to_textView;
        @Bind(R.id.textView_qtu)
        TextView qtu_textView;
        @Bind(R.id.textView_personid)
        TextView personId_textView;
        @Bind(R.id.textView_code)
        TextView code_textView;
        @Bind(R.id.textView_isCancelled)
        TextView isCancelled_textView;
        @Bind(R.id.linearLayout_code)
        LinearLayout code_linearLayout;
        @Bind(R.id.button_cancel)
        Button cancel_button;
        @Bind(R.id.button_delete)
        Button delete_button;
        @Bind(R.id.button_book)
        Button book_button;
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
        @Bind(R.id.linearLayout_isBooked)
        LinearLayout isBooked_linearLayout;
        @Bind(R.id.linearLayout_isCancelled)
        LinearLayout isCancelled_linearLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class OnBookEvent {
        long id;

        public OnBookEvent(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    abstract class OnBtnClickListener implements View.OnClickListener {
        protected ProgressDialog progressDialog;
        protected BookRecord bookRecord;

        public OnBtnClickListener(BookRecord bookRecord) {
            this.bookRecord = bookRecord;
        }
    }

    class OnBookBtnClickListener extends OnBtnClickListener {
        public OnBookBtnClickListener(BookRecord bookRecord) {
            super(bookRecord);
        }

        @Override
        public void onClick(View v) {
            if (!NetworkChecker.isConnected(context)) {
                SnackbarHelper.show(parentView, context.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
                return;
            }
            if (BookRecord.isBookable(bookRecord, Calendar.getInstance())) {
                EventBus.getDefault().post(new OnBookEvent(bookRecord.getId()));
            } else {
                SnackbarHelper.show(parentView, context.getString(R.string.not_time_for_book), Snackbar.LENGTH_LONG);
            }
        }
    }

    class OnCancelBtnClickListener extends OnBtnClickListener {
        public OnCancelBtnClickListener(BookRecord bookRecord) {
            super(bookRecord);
        }

        @Override
        public void onClick(View v) {
            if (!NetworkChecker.isConnected(context)) {
                SnackbarHelper.show(parentView, context.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
                return;
            }
            Observable.just(bookRecord.getId())
                    .map(id -> BookManager.cancel(id))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(() -> progressDialog = ProgressDialog.show(context, "", "退票中"))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        progressDialog.dismiss();
                        notifyItemChanged(bookRecords.indexOf(bookRecord));
                        String s = context.getString(R.string.cancel_suc);
                        if (!result)
                            s = context.getString(R.string.cancel_fale);
                        SnackbarHelper.show(parentView, s, Snackbar.LENGTH_LONG);
                    });
        }
    }
}
