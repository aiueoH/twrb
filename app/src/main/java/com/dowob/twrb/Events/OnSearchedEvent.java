package com.dowob.twrb.Events;

import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.util.ArrayList;

public class OnSearchedEvent {
    private SearchInfo searchInfo;
    private ArrayList<TrainInfo> trainInfos;

    public OnSearchedEvent(SearchInfo searchInfo, ArrayList<TrainInfo> trainInfos) {
        this.searchInfo = searchInfo;
        this.trainInfos = trainInfos;
    }

    public ArrayList<TrainInfo> getTrainInfos() {
        return trainInfos;
    }

    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
}
