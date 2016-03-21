package com.dowob.twrb.features.tickets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.database.BookableStation;
import com.dowob.twrb.database.TimetableStation;
import com.dowob.twrb.database.TrainInfo;
import com.dowob.twrb.utils.Config;
import com.dowob.twrb.utils.Util;
import com.jakewharton.rxbinding.view.RxView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookRecordAdapter extends RecyclerView.Adapter<BookRecordAdapter.MyViewHolder> {
    private Context context;
    private List<BookRecord> bookRecords;
    private OnItemClickListener onItemClickListener;
    private OnBookButtonClickListener onBookButtonClickListener;

    public BookRecordAdapter(List<BookRecord> bookRecords, OnItemClickListener onItemClickListener, OnBookButtonClickListener onBookButtonClickListener) {
        this.bookRecords = bookRecords;
        this.onItemClickListener = onItemClickListener;
        this.onBookButtonClickListener = onBookButtonClickListener;
    }

    @Override
    public int getItemCount() {
        return bookRecords.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookrecord_v2, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final BookRecord br = this.bookRecords.get(position);
        holder.date_textView.setText(new SimpleDateFormat("yyyy/MM/dd E").format(br.getGetInDate()));
        holder.no_textView_no.setText(br.getTrainNo());
        holder.from_textView.setText(BookableStation.getNameByNo(br.getFromStation()));
        holder.to_textView.setText(BookableStation.getNameByNo(br.getToStation()));
        holder.qty_textView.setText(Integer.toString(br.getOrderQtu()));
        holder.book_textView.setVisibility(!TextUtils.isEmpty(br.getCode()) || br.isCancelled() ? View.INVISIBLE : View.VISIBLE);
        holder.isBooked_linearLayout.setVisibility(TextUtils.isEmpty(br.getCode()) || br.isCancelled() ? View.INVISIBLE : View.VISIBLE);
        holder.isCancelled_linearLayout.setVisibility(br.isCancelled() ? View.VISIBLE : View.INVISIBLE);
        setTrainType(holder, br);
        setDepartureTime(holder, br);
        setArrivalTime(holder, br);
        setBookLinearLayout(holder, br);
        setCardViewClickListener(holder, br);
        int cityNo = Integer.parseInt(TimetableStation.getByBookNo(br.getToStation()).getCityNo());
        holder.mainSpace_imageView.setImageResource(Util.getCityDrawableId(cityNo));

    }

    private void setTrainType(MyViewHolder holder, BookRecord br) {
        TrainInfo trainInfo = br.getTrainInfo();
        if (trainInfo != null) {
            holder.trainType_layout.setVisibility(View.VISIBLE);
            holder.trainType_textView.setText(trainInfo.getTrainType());
        } else
            holder.trainType_layout.setVisibility(View.GONE);
    }

    private void setCardViewClickListener(MyViewHolder holder, BookRecord bookRecord) {
        RxView.clicks(holder.cardView)
                .throttleFirst(Config.BUTTON_CLICK_THROTTLE, TimeUnit.MILLISECONDS)
                .subscribe(aVoid -> onItemClick(holder.cardView, bookRecord));
    }

    private void setBookLinearLayout(MyViewHolder holder, BookRecord bookRecord) {
        boolean isClickable = false;
        Drawable drawable = null;
        holder.book_linearLayou.setOnClickListener(null);
        if (TextUtils.isEmpty(bookRecord.getCode()) && !bookRecord.isCancelled()) {
            RxView.clicks(holder.book_linearLayou)
                    .throttleFirst(Config.BUTTON_CLICK_THROTTLE, TimeUnit.MILLISECONDS)
                    .subscribe(v -> onBookButtonClick(holder.book_linearLayou, bookRecord));
            isClickable = true;
            drawable = Util.getDrawable(context, R.drawable.book_bg);
        }
        holder.book_linearLayou.setClickable(isClickable);
        if (Build.VERSION.SDK_INT >= 16)
            holder.book_linearLayou.setBackground(drawable);
        else
            holder.book_linearLayou.setBackgroundDrawable(drawable);
    }

    private void setArrivalTime(MyViewHolder holder, BookRecord bookRecord) {
        TrainInfo trainInfo = bookRecord.getTrainInfo();
        if (trainInfo != null) {
            String time = new SimpleDateFormat("HH:mm").format(trainInfo.getArrivalDateTime());
            holder.arrival_textView.setVisibility(View.VISIBLE);
            holder.arrival_textView.setText(time);
        } else {
            holder.arrival_textView.setVisibility(View.GONE);
        }
    }

    private void setDepartureTime(MyViewHolder holder, BookRecord bookRecord) {
        TrainInfo trainInfo = bookRecord.getTrainInfo();
        if (trainInfo != null) {
            String time = new SimpleDateFormat("HH:mm").format(trainInfo.getDepartureDateTime());
            holder.departureTime_textView.setVisibility(View.VISIBLE);
            holder.departureTime_textView.setText(time);
        } else {
            holder.departureTime_textView.setVisibility(View.GONE);
        }
    }

    private void onItemClick(View view, BookRecord bookRecord) {
        if (onItemClickListener != null)
            onItemClickListener.onClick(view, bookRecord);
    }

    private void onBookButtonClick(View view, BookRecord bookRecord) {
        if (onBookButtonClickListener != null)
            onBookButtonClickListener.onClick(view, bookRecord);
    }

    interface OnItemClickListener {
        void onClick(View view, BookRecord bookRecord);
    }

    interface OnBookButtonClickListener {
        void onClick(View view, BookRecord bookRecord);
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
        @Bind(R.id.linearLayout_book)
        LinearLayout book_linearLayou;
        @Bind(R.id.textView_qty)
        TextView qty_textView;
        @Bind(R.id.imageView_mainSpace)
        ImageView mainSpace_imageView;
        @Bind(R.id.relativeLayout_mainSpace)
        RelativeLayout mainSpace_relativeLayout;


        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class Builder {
        private List<BookRecord> bookRecords;
        private BookRecordAdapter.OnItemClickListener onItemClickListener;
        private BookRecordAdapter.OnBookButtonClickListener onBookButtonClickListener;

        public Builder setBookRecords(List<BookRecord> bookRecords) {
            this.bookRecords = bookRecords;
            return this;
        }

        public Builder setOnItemClickListener(BookRecordAdapter.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public Builder setOnBookButtonClickListener(BookRecordAdapter.OnBookButtonClickListener onBookButtonClickListener) {
            this.onBookButtonClickListener = onBookButtonClickListener;
            return this;
        }

        public BookRecordAdapter createBookRecordAdapter() {
            return new BookRecordAdapter(bookRecords, onItemClickListener, onBookButtonClickListener);
        }
    }
}
