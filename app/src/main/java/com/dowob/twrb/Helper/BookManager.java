package com.dowob.twrb.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.dowob.twrb.DBObject.AdaptHelper;
import com.dowob.twrb.DBObject.BookRecord;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.book.BookResult;
import com.twrb.core.helpers.BookHelper;

import java.util.Calendar;

import io.realm.Realm;

public class BookManager {
    public static BookResult book(Context context, long bookRecordId) {
        BookResult result = BookResult.UNKNOWN;
        try {
            BookInfo bookInfo = new BookInfo();
            Realm.getDefaultInstance().refresh();
            BookRecord bookRecord = BookRecord.get(bookRecordId);
            AdaptHelper.to(bookRecord, bookInfo);
            result = BookHelper.book(bookInfo);
            if (!result.equals(BookResult.OK)) {
                MyLogger.i("訂票失敗");
                return result;
            }
            Realm.getDefaultInstance().beginTransaction();
            AdaptHelper.to(bookInfo, bookRecord);
            Realm.getDefaultInstance().commitTransaction();
            MyLogger.i("訂位代碼:" + bookInfo.code);
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
}
