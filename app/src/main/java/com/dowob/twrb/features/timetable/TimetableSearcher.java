package com.dowob.twrb.features.timetable;

import android.support.annotation.NonNull;

import com.twrb.core.timetable.DesktopWebTimetableSearcher;
import com.twrb.core.timetable.MobileWebTimetableSearcher;
import com.twrb.core.timetable.SearchInfo;
import com.twrb.core.timetable.TrainInfo;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

public class TimetableSearcher {
    private static final int RETRY_TIMES = 3;
    private List<TrainInfo> desktopWebTrainInfos = null, mobileWebTrainInfos = null;

    @NonNull
    public AbstractMap.SimpleEntry<Result, List<TrainInfo>> search(SearchInfo searchInfo) {
        searchImp(searchInfo);
        if (desktopWebTrainInfos == null)
            return new AbstractMap.SimpleEntry<>(Result.IO_EXCEPTION, null);
        if (mobileWebTrainInfos != null)
            mergeInfo();
        return new AbstractMap.SimpleEntry<>(Result.OK, desktopWebTrainInfos);
    }

    private void searchImp(SearchInfo searchInfo) {
        Observable.just(new DesktopSearcher(), new MobileSearcher())
                .flatMap(searcher -> Observable.just(searcher)
                        .map(s -> s.search(searchInfo))
                        .subscribeOn(Schedulers.io()))
                .toBlocking()
                .forEach(o -> {
                });
    }

    private void mergeInfo() {
        for (TrainInfo tiD : desktopWebTrainInfos) {
            for (TrainInfo tiM : mobileWebTrainInfos)
                if (tiD.no.equals(tiM.no)) {
                    tiD.delay = tiM.delay;
                    break;
                }
        }
    }

    public enum Result {
        OK,
        IO_EXCEPTION
    }

    private abstract class Searcher {
        public abstract Object search(SearchInfo searchInfo);
    }

    private class DesktopSearcher extends Searcher {
        @Override
        public Object search(SearchInfo searchInfo) {
            for (int i = 0; i < RETRY_TIMES; i++) {
                try {
                    DesktopWebTimetableSearcher searcher = new DesktopWebTimetableSearcher();
                    desktopWebTrainInfos = searcher.search(searchInfo);
                    break;
                } catch (IOException e) {
                }
            }
            return null;
        }
    }

    private class MobileSearcher extends Searcher {
        @Override
        public Object search(SearchInfo searchInfo) {
            for (int i = 0; i < RETRY_TIMES; i++) {
                try {
                    MobileWebTimetableSearcher searcher = new MobileWebTimetableSearcher();
                    mobileWebTrainInfos = searcher.search(searchInfo);
                    break;
                } catch (IOException e) {
                }
            }
            return null;
        }
    }
}
