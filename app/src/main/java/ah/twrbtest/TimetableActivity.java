package ah.twrbtest;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ah.twrbtest.DBObject.TimetableStation;
import ah.twrbtest.Events.OnSearchedEvent;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TimetableActivity extends Activity {
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    @Bind(R.id.textView_date)
    TextView date_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_timetable);
        ButterKnife.bind(this);

        OnSearchedEvent e = EventBus.getDefault().getStickyEvent(OnSearchedEvent.class);
        ArrayList<TrainInfo> trainInfos = e.getTrainInfos();
        SearchInfo searchInfo = e.getSearchInfo();

        String date = "";
        try {
            DateFormat output = new SimpleDateFormat("yyyy/MM/dd E");
            DateFormat input = new SimpleDateFormat("yyyy/MM/dd");
            date = output.format(input.parse(searchInfo.SEARCHDATE));
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        this.date_textView.setText(date);
        this.from_textView.setText(TimetableStation.get(searchInfo.FROMSTATION).getNameCh());
        this.to_textView.setText(TimetableStation.get(searchInfo.TOSTATION).getNameCh());

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(new TrainInfoAdapter(this, trainInfos));
    }
}
