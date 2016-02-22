package com.dowob.twrb.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.dowob.twrb.BookRecordFactory;
import com.dowob.twrb.DBObject.AdaptHelper;
import com.dowob.twrb.DBObject.BookRecord;
import com.dowob.twrb.Model.Booker;
import com.dowob.twrb.R;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.helpers.BookHelper;

import java.util.AbstractMap;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

public class BookManager {
    @NonNull
    public static AbstractMap.SimpleEntry<Booker.Result, List<String>> book
            (Context context,
             String from,
             String to,
             Calendar getInDate,
             String no,
             int qty,
             String personId) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result
                = new Booker().book(from, to, getInDate, no, qty, personId);
        if (result.getKey().equals(Booker.Result.OK)) {
            List<String> data = result.getValue();
            BookRecordFactory.createBookRecord(personId, getInDate, from, to, qty, no, "0", data.get(0));
            MyLogger.i("訂票成功。code:" + data.get(0));
        } else {
            MyLogger.i("訂票失敗。" + result.getKey());
        }
        setLastBookTime(context);
        return result;
    }

    @NonNull
    public static AbstractMap.SimpleEntry<Booker.Result, List<String>> book(Context context, long bookRecordId) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result
                = new AbstractMap.SimpleEntry<>(Booker.Result.UNKNOWN, null);
        try {
            Realm.getDefaultInstance().refresh();
            BookRecord bookRecord = BookRecord.get(bookRecordId);
            String from = bookRecord.getFromStation();
            String to = bookRecord.getToStation();
            Calendar getInDate = Calendar.getInstance();
            getInDate.setTime(bookRecord.getGetInDate());
            String no = bookRecord.getTrainNo();
            int qty = Integer.parseInt(bookRecord.getOrderQtuStr());
            String personId = bookRecord.getPersonId();
            result = new Booker().book(from, to, getInDate, no, qty, personId);
            if (result.getKey().equals(Booker.Result.OK)) {
                Realm.getDefaultInstance().beginTransaction();
                List<String> data = result.getValue();
                bookRecord.setCode(data.get(0));
                Realm.getDefaultInstance().commitTransaction();
                MyLogger.i("訂票成功。code:" + data.get(0));
            } else {
                MyLogger.i("訂票失敗。" + result.getKey());
            }
        } finally {
            Realm.getDefaultInstance().close();
            setLastBookTime(context);
        }
        return result;
    }

    public static Boolean cancel(long bookRecordId) {
        boolean result = false;
        try {
            BookInfo bookInfo = new BookInfo();
            Realm.getDefaultInstance().refresh();
            BookRecord bookRecord = BookRecord.get(bookRecordId);
            AdaptHelper.to(bookRecord, bookInfo);
            result = BookHelper.cancel(bookInfo);
            if (result) {
                bookInfo.code = "";
                Realm.getDefaultInstance().beginTransaction();
                AdaptHelper.to(bookInfo, bookRecord);
                bookRecord.setIsCancelled(true);
                Realm.getDefaultInstance().commitTransaction();
                MyLogger.i(result ? "已退訂" : "退訂失敗");
            }
        } finally {
            Realm.getDefaultInstance().close();
        }
        return result;
    }

    private static void setLastBookTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences("twrbtest", Activity.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putLong("lastBookTime", Calendar.getInstance().getTimeInMillis());
        e.commit();
    }

    public static int getBookCDTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences("twrbtest", Activity.MODE_PRIVATE);
        long lastBookTime = sp.getLong("lastBookTime", 0);
        return 10 - (int) ((Calendar.getInstance().getTimeInMillis() - lastBookTime) / 1000);
    }

    @NonNull
    public static String getResultMsg(Context context, Booker.Result result) {
        switch (result) {
            case OK:
                return context.getString(R.string.book_suc);
            case NO_SEAT:
                return context.getString(R.string.book_no_seat);
            case OUT_TIME:
                return context.getString(R.string.book_out_time);
            case NOT_YET_BOOK:
                return context.getString(R.string.book_not_yet);
            case OVER_QUOTA:
                return context.getString(R.string.book_over_quota);
            case FULL_UP:
                return context.getString(R.string.book_full_up);
            case IO_EXCEPTION:
                return context.getString(R.string.book_io_exception);
            default:
                return context.getString(R.string.book_unknown);
        }
    }
}
