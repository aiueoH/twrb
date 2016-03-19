package com.dowob.twrb.features.tickets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
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
import com.dowob.twrb.features.shared.NetworkChecker;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.jakewharton.rxbinding.view.RxView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_bookrecord_v2, parent, false));
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
        holder.date_textView.setText(new SimpleDateFormat("yyyy/MM/dd E").format(br.getGetInDate()));
        String trainType = br.getTrainType();
        if (trainType != null && !trainType.isEmpty()) {
            holder.trainType_layout.setVisibility(View.VISIBLE);
            holder.trainType_textView.setText(trainType);
        } else
            holder.trainType_layout.setVisibility(View.GONE);
        holder.no_textView_no.setText(br.getTrainNo());
        holder.from_textView.setText(BookableStation.getNameByNo(br.getFromStation()));
        holder.to_textView.setText(BookableStation.getNameByNo(br.getToStation()));
        holder.qty_textView.setText(Integer.toString(br.getOrderQtu()));
        holder.cancel_button.setVisibility(br.getCode().isEmpty() || br.isCancelled() ? View.GONE : View.VISIBLE);
        holder.book_textView.setVisibility(!br.getCode().isEmpty() || br.isCancelled() ? View.INVISIBLE : View.VISIBLE);
        holder.book_linearLayou.setOnClickListener(!br.getCode().isEmpty() || br.isCancelled() ? null : new OnBookBtnClickListener(br));
        setBookBg(holder, br);
        holder.cancel_button.setOnClickListener(new OnCancelBtnClickListener(br));
        holder.isBooked_linearLayout.setVisibility(br.getCode().isEmpty() || br.isCancelled() ? View.INVISIBLE : View.VISIBLE);
        holder.isCancelled_linearLayout.setVisibility(br.isCancelled() ? View.VISIBLE : View.INVISIBLE);
        setDeleteButton(holder, br);
        setDepartureTime(holder, br);
        setArrivalTime(holder, br);
        holder.cardView.setOnClickListener(v -> {
            EventBus.getDefault().post(new OnDisplayItemDetailEvent(position));
            EventBus.getDefault().postSticky(new BookRecordActivity.Data(br));
            Intent intent = new Intent(context, BookRecordActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        Pair.create(holder.mainSpace_linearLayout, holder.mainSpace_linearLayout.getTransitionName())
                );
                context.startActivity(intent, options.toBundle());
            } else {
                context.startActivity(intent);
            }
        });
    }

    private void setDeleteButton(MyViewHolder holder, BookRecord bookRecord) {
        RxView.clicks(holder.delete_button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(v -> {
//                    int index = bookRecords.indexOf(bookRecord);
//                    bookRecords.remove(index);
//                    notifyItemRemoved(index);
//                    BookManager bookManager = new BookManager();
//                    bookManager.delete(bookRecord.getId());
                    BookRecordModel.getInstance().delete(bookRecord);
                });
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

    private void setBookBg(MyViewHolder holder, BookRecord bookRecord) {
        Drawable drawable = !bookRecord.getCode().isEmpty() || bookRecord.isCancelled() ? null : getDrawable(R.drawable.book_bg);
        if (Build.VERSION.SDK_INT >= 16)
            holder.book_linearLayou.setBackground(drawable);
        else
            holder.book_linearLayou.setBackgroundDrawable(drawable);
    }

    private Drawable getDrawable(int id) {
        if (Build.VERSION.SDK_INT >= 21)
            return context.getDrawable(id);
        return context.getResources().getDrawable(id);
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
        @Bind(R.id.textView_trainType)
        TextView trainType_textView;
        @Bind(R.id.layout_trainType)
        LinearLayout trainType_layout;
        @Bind(R.id.textView_from)
        TextView from_textView;
        @Bind(R.id.textView_to)
        TextView to_textView;
        @Bind(R.id.linearLayout_dateAndNo)
        LinearLayout dateAndNo_linearLayout;
        @Bind(R.id.textView_arrivalTime)
        TextView arrival_textView;
        @Bind(R.id.textView_departureTime)
        TextView departureTime_textView;
        @Bind(R.id.linearLayout_isBooked)
        LinearLayout isBooked_linearLayout;
        @Bind(R.id.linearLayout_isCancelled)
        LinearLayout isCancelled_linearLayout;
        @Bind(R.id.textView_book)
        TextView book_textView;
        @Bind(R.id.card_view)
        CardView cardView;
        @Bind(R.id.button_cancel)
        Button cancel_button;
        @Bind(R.id.button_delete)
        Button delete_button;
        @Bind(R.id.linearLayout_book)
        LinearLayout book_linearLayou;
        @Bind(R.id.textView_qty)
        TextView qty_textView;
        @Bind(R.id.linearLayout_mainSpace)
        LinearLayout mainSpace_linearLayout;

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

    static class OnDisplayItemDetailEvent {
        private int index;

        public OnDisplayItemDetailEvent(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
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
//            if (!NetworkChecker.isConnected(context)) {
//                SnackbarHelper.show(parentView, context.getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
//                return;
//            }
//            Observable.just(bookRecord.getId())
//                    .map(id -> BookManager.cancel(id))
//                    .subscribeOn(Schedulers.io())
//                    .doOnSubscribe(() -> progressDialog = ProgressDialog.show(context, "", "退票中"))
//                    .subscribeOn(AndroidSchedulers.mainThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(result -> {
//                        progressDialog.dismiss();
//                        notifyItemChanged(bookRecords.indexOf(bookRecord));
//                        String s = context.getString(R.string.cancel_suc);
//                        if (!result)
//                            s = context.getString(R.string.cancel_fale);
//                        SnackbarHelper.show(parentView, s, Snackbar.LENGTH_LONG);
//                    });
        }
    }
}
