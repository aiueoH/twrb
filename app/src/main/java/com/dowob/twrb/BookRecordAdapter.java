package com.dowob.twrb;

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

import com.dowob.twrb.DBObject.AdaptHelper;
import com.dowob.twrb.DBObject.BookRecord;
import com.dowob.twrb.DBObject.BookableStation;
import com.dowob.twrb.Events.OnBookRecordRemovedEvent;
import com.dowob.twrb.Events.OnBookedEvent;
import com.dowob.twrb.Helper.BookManager;
import com.jakewharton.rxbinding.view.RxView;
import com.twrb.core.book.BookResult;

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
        holder.qtu_textView.setText(br.getOrderQtuStr());
        holder.personId_textView.setText(br.getPersonId());
        holder.code_textView.setText(br.getCode());
        holder.code_linearLayout.setVisibility(br.getCode().isEmpty() ? View.GONE : View.VISIBLE);
        holder.book_button.setVisibility(!br.getCode().isEmpty() || br.isCancelled() ? View.GONE : View.VISIBLE);
        holder.cancel_button.setVisibility(br.getCode().isEmpty() || br.isCancelled() ? View.GONE : View.VISIBLE);
        holder.isCancelled_textView.setVisibility(br.isCancelled() ? View.VISIBLE : View.GONE);
        holder.book_button.setOnClickListener(new OnBookBtnClickListener(br));
        holder.cancel_button.setOnClickListener(new OnCancelBtnClickListener(br));
        RxView.clicks(holder.delete_button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
                    int index = bookRecords.indexOf(br);
                    notifyItemRemoved(index);
                    bookRecords.remove(index);
                    Realm realm = Realm.getDefaultInstance();
                    BookRecord tmp = realm.where(BookRecord.class).equalTo("id", br.getId()).findFirst();
                    if (tmp == null) return;
                    realm.beginTransaction();
                    tmp.removeFromRealm();
                    realm.commitTransaction();
                    EventBus.getDefault().post(new OnBookRecordRemovedEvent());
                });
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

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
            int remainCDTime = BookManager.getBookCDTime(context);
            if (remainCDTime > 0) {
                String s = String.format(context.getString(R.string.cold_down_msg), remainCDTime);
                SnackbarHelper.show(parentView, s, Snackbar.LENGTH_LONG);
                return;
            }
            if (BookRecord.isBookable(bookRecord, Calendar.getInstance())) {
                Observable.just(bookRecord.getId())
                    .map(id -> BookManager.book(context, id))
                        .subscribeOn(Schedulers.io())
                    .doOnSubscribe(() -> progressDialog = ProgressDialog.show(context, "", context.getString(R.string.is_booking)))
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            progressDialog.dismiss();
                            EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result.getKey()));
                            String s = result.getKey().equals(BookResult.OK) ? context.getString(R.string.book_suc) : context.getString(R.string.book_fale);
                            SnackbarHelper.show(parentView, s, Snackbar.LENGTH_LONG);
                        });
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
