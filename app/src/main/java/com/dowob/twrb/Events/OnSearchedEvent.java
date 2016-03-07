package com.dowob.twrb.events;

import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.util.List;

public class OnSearchedEvent {
    private SearchInfo searchInfo;
    private List<TrainInfo> trainInfos;

    public OnSearchedEvent(SearchInfo searchInfo, List<TrainInfo> trainInfos) {
        this.searchInfo = searchInfo;
        this.trainInfos = trainInfos;
    }

    public List<TrainInfo> getTrainInfos() {
        return trainInfos;
    }

    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
}
