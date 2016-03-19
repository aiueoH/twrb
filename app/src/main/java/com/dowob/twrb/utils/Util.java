package com.dowob.twrb.utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class Util {
    public static void runOnUiThread(Runnable runnable) {
        Observable.just(runnable)
                .map(r -> {
                    r.run();
                    return null;
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .toBlocking()
                .forEach(o -> {
                });
    }
}
