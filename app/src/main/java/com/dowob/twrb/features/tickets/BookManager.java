package com.dowob.twrb.features.tickets;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.events.OnBookRecordAddedEvent;
import com.dowob.twrb.events.OnBookedEvent;
import com.dowob.twrb.features.tickets.book.BookRecordFactory;
import com.dowob.twrb.features.tickets.book.Booker;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.helpers.BookHelper;
import com.twrb.core.timetable.TrainInfo;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

class BookManager {
    private String from, to, no, personId;
    private Calendar getInDate;
    private int qty;
    private TrainInfo trainInfo;
    private Booker mBooker;

    @NonNull
    public static AbstractMap.SimpleEntry<Booker.Result, List<String>> book
            (Context context,
             String from,
             String to,
             Calendar getInDate,
             String no,
             int qty,
             String personId,
             TrainInfo trainInfo) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result
                = new Booker().book(from, to, getInDate, no, qty, personId);
        if (result.getKey().equals(Booker.Result.OK)) {
            List<String> data = result.getValue();
            BookRecord bookRecord = BookRecordFactory
                    .createBookRecord(personId, getInDate, from, to, qty, no, "0", data.get(0), trainInfo);
            EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
            EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result.getKey()));
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
            int qty = bookRecord.getOrderQtu();
            String personId = bookRecord.getPersonId();

            result = new Booker().book(from, to, getInDate, no, qty, personId);
            if (result.getKey().equals(Booker.Result.OK)) {
                Realm.getDefaultInstance().beginTransaction();
                List<String> data = result.getValue();
                bookRecord.setCode(data.get(0));
                Realm.getDefaultInstance().commitTransaction();
                MyLogger.i("訂票成功。code:" + data.get(0));
            } else {
                EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result.getKey()));
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
            }
            MyLogger.i(result ? "已退訂" : "退訂失敗");
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
            case WRONG_RANDINPUT:
                return context.getString(R.string.book_wrong_randinput);
            default:
                return context.getString(R.string.book_unknown);
        }
    }

    // For book by timetable.
    @Nullable
    public ByteArrayOutputStream step1(String from, String to, Calendar getInDate, String no, int qty, String personId, TrainInfo trainInfo) {
        mBooker = new Booker();
        this.trainInfo = trainInfo;
        return step1(from, to, getInDate, no, qty, personId);
    }

    @Nullable
    private ByteArrayOutputStream step1(String from, String to, Calendar getInDate, String no, int qty, String personId) {
        mBooker = new Booker();
        this.from = from;
        this.to = to;
        this.getInDate = getInDate;
        this.no = no;
        this.qty = qty;
        this.personId = personId;
        return mBooker.step1(from, to, getInDate, no, qty, personId);
    }

    // For book by timetable.
    public AbstractMap.SimpleEntry<Booker.Result, Long> step2(String randInput) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result = mBooker.step2(randInput);
        if (result.getKey().equals(Booker.Result.OK)) {
            List<String> data = result.getValue();
            BookRecord bookRecord = BookRecordFactory
                    .createBookRecord(personId, getInDate, from, to, qty, no, "0", data.get(0), trainInfo);
            EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
            EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result.getKey()));
            MyLogger.i("訂票成功。code:" + data.get(0));
            return new AbstractMap.SimpleEntry<>(result.getKey(), bookRecord.getId());
        } else {
            MyLogger.i("訂票失敗。" + result.getKey());
            return new AbstractMap.SimpleEntry<>(result.getKey(), null);
        }
    }

    // For book by my ticket.
    @Nullable
    public ByteArrayOutputStream step1(Context context, long bookRecordId) {
        Realm.getDefaultInstance().refresh();
        BookRecord bookRecord = BookRecord.get(bookRecordId);
        String from = bookRecord.getFromStation();
        String to = bookRecord.getToStation();
        Calendar getInDate = Calendar.getInstance();
        getInDate.setTime(bookRecord.getGetInDate());
        String no = bookRecord.getTrainNo();
        int qty = bookRecord.getOrderQtu();
        String personId = bookRecord.getPersonId();
        return step1(from, to, getInDate, no, qty, personId);
    }

    // For book by my ticket.
    public AbstractMap.SimpleEntry<Booker.Result, List<String>> step2(long id, String randInput) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result = mBooker.step2(randInput);
        Realm.getDefaultInstance().refresh();
        BookRecord bookRecord = BookRecord.get(id);
        if (result.getKey().equals(Booker.Result.OK)) {
            Realm.getDefaultInstance().beginTransaction();
            List<String> data = result.getValue();
            bookRecord.setCode(data.get(0));
            Realm.getDefaultInstance().commitTransaction();
//            EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result.getKey()));
            MyLogger.i("訂票成功。code:" + data.get(0));
        } else {
//            EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result.getKey()));
            MyLogger.i("訂票失敗。" + result.getKey());
        }
        return result;
    }

    public long save(BookInfo bookInfo, TrainInfo trainInfo) {
        long id = BookRecordFactory.createBookRecord(bookInfo, trainInfo).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(id));
        return id;
    }

    public boolean delete(long bookRecordId) {
        Realm realm = Realm.getDefaultInstance();
        BookRecord bookRecord = realm.where(BookRecord.class).equalTo("id", bookRecordId).findFirst();
        if (bookRecord == null)
            return false;
        com.dowob.twrb.database.TrainInfo trainInfo = bookRecord.getTrainInfo();
        realm.beginTransaction();
        if (trainInfo != null)
            trainInfo.removeFromRealm();
        bookRecord.removeFromRealm();
        realm.commitTransaction();
        return true;
    }
}
