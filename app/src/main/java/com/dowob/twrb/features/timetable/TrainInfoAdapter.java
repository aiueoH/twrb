package com.dowob.twrb.features.timetable;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.twrb.core.MyLogger;
import com.twrb.core.timetable.TrainInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TrainInfoAdapter extends RecyclerView.Adapter<TrainInfoAdapter.MyViewHolder> {
    private static HashMap<String, Integer> TRAINTYPE_COLOR = new HashMap<String, Integer>() {{
        put("自強", Color.parseColor("#81433D"));
        put("莒光", Color.parseColor("#99684D"));
        put("區間車", Color.parseColor("#545399"));
        put("區間快", Color.parseColor("#528999"));
        put("普悠瑪", Color.parseColor("#9C6798"));
        put("太魯閣", Color.parseColor("#5E994C"));
        put("復興", Color.parseColor("#0d5a86"));
    }};

    private Context context;
    private List<TrainInfo> trainInfos;
    private Calendar searchDate;

    public TrainInfoAdapter(Context context, List<TrainInfo> trainInfos, Calendar searchDate) {
        this.context = context;
        this.trainInfos = trainInfos;
        this.searchDate = searchDate;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_traininfo, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final TrainInfo ti = this.trainInfos.get(position);
        holder.trainType_textView.setText(ti.type);
        holder.trainNo_textView.setText(ti.no);
        holder.departureTime_textView.setText(ti.departureTime);
        holder.arrivalTime_textView.setText(ti.arrivalTime);
        holder.trainType_textView.setTextColor(getTrainTypeColor(ti.type));
        holder.trainNo_textView.setTextColor(getTrainTypeColor(ti.type));
        holder.fare_textView.setText("$" + ti.fares);
        if (ti.isBookableNow) {
            holder.book_textView.setText("按此訂票");
            holder.book_textView.setTextColor(getColor(R.color.colorPrimaryDark));
            holder.book_linearLayout.setOnClickListener(v -> EventBus.getDefault().post(new OnItemClickEvent(ti)));
            setBookBg(holder.book_linearLayout, getDrawable(R.drawable.book_bg));
        } else {
            holder.book_textView.setText("無法訂票");
            holder.book_textView.setTextColor(getColor(R.color.gray_normal));
            holder.book_linearLayout.setOnClickListener(null);
            setBookBg(holder.book_linearLayout, null);
        }
        if (ti.delay != null && !ti.delay.isEmpty() && !ti.delay.equals("0")) {
            String delay = "誤點 " + ti.delay + " 分";
            holder.delay_textView.setVisibility(View.VISIBLE);
            holder.delay_textView.setText(delay);
        } else
            holder.delay_textView.setVisibility(View.INVISIBLE);
    }

    private Drawable getDrawable(int id) {
        if (Build.VERSION.SDK_INT >= 21)
            return context.getDrawable(id);
        return context.getResources().getDrawable(id);
    }

    private int getColor(int id) {
        if (Build.VERSION.SDK_INT >= 23)
            return context.getColor(id);
        return context.getResources().getColor(id);
    }

    private void setBookBg(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16)
            view.setBackground(drawable);
        else
            view.setBackgroundDrawable(drawable);
    }

    private Calendar createDepartureDateTime(String departureTime) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("HH:mm").parse(departureTime));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        Calendar departureDateTime = (Calendar) searchDate.clone();
        departureDateTime.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        departureDateTime.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        MyLogger.v("departure " + departureDateTime.getTime().toString());
        return departureDateTime;
    }

    private boolean isBookable(TrainInfo trainInfo) {
        if (trainInfo.type.equals("自強") ||
            trainInfo.type.equals("莒光") ||
            trainInfo.type.equals("普悠瑪") ||
            trainInfo.type.equals("太魯閣") ||
            trainInfo.type.equals("復興")) {
            Calendar departureDateTime = createDepartureDateTime(trainInfo.departureTime);
            if (departureDateTime != null && BookRecord.isBookable(departureDateTime, Calendar.getInstance()))
                return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return trainInfos.size();
    }

    private int getTrainTypeColor(String trainType) {
        Integer color = TRAINTYPE_COLOR.get(trainType);
        return color == null ? Color.BLACK : color;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.textView_trainType)
        TextView trainType_textView;
        @Bind(R.id.textView_trainNo)
        TextView trainNo_textView;
        @Bind(R.id.textView_departureTime)
        TextView departureTime_textView;
        @Bind(R.id.textView_arrivalTime)
        TextView arrivalTime_textView;
        @Bind(R.id.textView_delay)
        TextView delay_textView;
        @Bind(R.id.textView_fare)
        TextView fare_textView;
        @Bind(R.id.textView_book)
        TextView book_textView;
        @Bind(R.id.linearLayout_book)
        LinearLayout book_linearLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class OnItemClickEvent {
        private TrainInfo trainInfo;

        public OnItemClickEvent(TrainInfo trainInfo) {
            this.trainInfo = trainInfo;
        }

        public TrainInfo getTrainInfo() {
            return trainInfo;
        }
    }
}
