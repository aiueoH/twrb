package com.dowob.twrb.features.timetable;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.TimetableStation;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TimetableFragment extends Fragment {

    @Bind(R.id.textView_from)
    TextView from_textView;
    @Bind(R.id.textView_to)
    TextView to_textView;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    private List<TrainInfo> trainInfos;
    private Calendar searchDate;
    private SearchInfo searchInfo;

    public TimetableFragment() {
        OnPassingTrainInfoEvent e = EventBus.getDefault().getStickyEvent(OnPassingTrainInfoEvent.class);
        trainInfos = e.getTrainInfos();
        searchDate = e.getSearchDate();
        searchInfo = e.getSearchInfo();
    }

    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);
        ButterKnife.bind(this, view);
        from_textView.setText(TimetableStation.get(this.searchInfo.fromStation).getNameCh());
        to_textView.setText(TimetableStation.get(this.searchInfo.toStation).getNameCh());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new TrainInfoAdapter(getActivity(), trainInfos, searchDate));
        scrollToMostNearlyTimeTrain();
        return view;
    }

    private void scrollToMostNearlyTimeTrain() {
        Calendar departureDateTime = (Calendar) searchDate.clone();
        for (int i = 0; i < trainInfos.size(); i++) {
            String hm[] = trainInfos.get(i).departureTime.split(":");
            departureDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
            departureDateTime.set(Calendar.MINUTE, Integer.parseInt(hm[1]));
            if (Calendar.getInstance().before(departureDateTime)) {
                this.recyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    public static class OnPassingTrainInfoEvent {
        List<TrainInfo> trainInfos;
        Calendar searchDate;
        SearchInfo searchInfo;

        public OnPassingTrainInfoEvent(List<TrainInfo> trainInfos, Calendar searchDate, SearchInfo searchInfo) {
            this.trainInfos = trainInfos;
            this.searchDate = searchDate;
            this.searchInfo = searchInfo;
        }

        public List<TrainInfo> getTrainInfos() {
            return trainInfos;
        }

        public Calendar getSearchDate() {
            return searchDate;
        }

        public SearchInfo getSearchInfo() {
            return searchInfo;
        }
    }
}
