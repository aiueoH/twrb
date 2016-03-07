package com.dowob.twrb.features.timetable;

import android.support.annotation.NonNull;

import com.twrb.core.timetable.MobileWebTimetableSearcher;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

public class TimetableSearcher {
    @NonNull
    public AbstractMap.SimpleEntry<Result, List<TrainInfo>> search(SearchInfo searchInfo) {
        try {
            List<TrainInfo> trainInfos = MobileWebTimetableSearcher.search(searchInfo);
            return new AbstractMap.SimpleEntry<>(Result.OK, trainInfos);
        } catch (IOException e) {
            return new AbstractMap.SimpleEntry<>(Result.IO_EXCEPTION, null);
        }
    }

    public enum Result {
        OK,
        IO_EXCEPTION
    }
}
