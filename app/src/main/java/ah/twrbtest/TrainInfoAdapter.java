package ah.twrbtest;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twrb.core.timetable.TrainInfo;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TrainInfoAdapter extends RecyclerView.Adapter<TrainInfoAdapter.MyViewHolder> {
    private static HashMap<String, Integer> TRAINTYPE_COLOR = new HashMap<String, Integer>() {{
        put("自強", Color.parseColor("#990D00"));
        put("莒光", Color.parseColor("#99684D"));
        put("區間車", Color.parseColor("#545399"));
        put("區間快", Color.parseColor("#528999"));
        put("普悠瑪", Color.parseColor("#99028F"));
        put("太魯閣", Color.parseColor("#5E994C"));
    }};

    private Context context;
    private List<TrainInfo> trainInfos;

    public TrainInfoAdapter(Context context, List<TrainInfo> trainInfos) {
        this.context = context;
        this.trainInfos = trainInfos;
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
        holder.depatureTime_textView.setText(ti.departureTime);
        holder.arrivalTime_textView.setText(ti.arrivalTime);
        holder.trainType_textView.setTextColor(getTrainTypeColor(ti.type));
        holder.trainNo_textView.setTextColor(getTrainTypeColor(ti.type));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new OnItemClickEvent(ti));
            }
        });
        String delay = "";
        if (ti.delay.equals("0"))
            delay = "準點";
        else if (!ti.delay.isEmpty())
            delay = "誤點 " + ti.delay + " 分";
        holder.delay_textView.setText(delay);
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
        TextView depatureTime_textView;
        @Bind(R.id.textView_arrivalTime)
        TextView arrivalTime_textView;
        @Bind(R.id.textView_delay)
        TextView delay_textView;
        @Bind(R.id.card_view)
        CardView cardView;

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
