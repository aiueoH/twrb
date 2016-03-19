package com.dowob.twrb.features.tickets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.events.OnBookRecordAddedEvent;
import com.dowob.twrb.features.tickets.book.BookRecordFactory;
import com.dowob.twrb.features.tickets.book.Booker;
import com.twrb.core.book.BookInfo;
import com.twrb.core.timetable.TrainInfo;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class BookRecordModel {

    private static BookRecordModel instance = new BookRecordModel();
    private BookManager bookManager;
    private List<Observer> observers = new ArrayList<>();

    private BookRecordModel() {
    }

    public static BookRecordModel getInstance() {
        return instance;
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

    public ByteArrayOutputStream book(Context context, long bookRecordId) {
        bookManager = new BookManager();
        return bookManager.step1(context, bookRecordId);
    }

    public AbstractMap.SimpleEntry<Booker.Result, List<String>> sendRandomInput(long bookRecordId, String randomInput) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result = bookManager.step2(bookRecordId, randomInput);
        if (result.getKey().equals(Booker.Result.OK))
            notifyOnBookRecordUpdate(bookRecordId);
        return result;
    }

    public ByteArrayOutputStream book(String from, String to, Calendar getInDate, String no, int qty, String personId, TrainInfo trainInfo) {
        bookManager = new BookManager();
        return bookManager.step1(from, to, getInDate, no, qty, personId, trainInfo);
    }

    public AbstractMap.SimpleEntry<Booker.Result, List<String>> sendRandomInput(String randomInput) {
        AbstractMap.SimpleEntry<Booker.Result, List<String>> result = bookManager.step2(randomInput);
        if (result.getKey().equals(Booker.Result.OK))
            notifyOnBookRecordCreate();
        return result;
    }

    public long save(BookInfo bookInfo, TrainInfo trainInfo) {
        long id = BookRecordFactory.createBookRecord(bookInfo, trainInfo).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(id));
        return id;
    }

    public boolean cancel(long bookRecordId) {
        boolean result = BookManager.cancel(bookRecordId);
        if (result)
            notifyOnBookRecordUpdate(bookRecordId);
        return result;
    }

    public void delete(BookRecord bookRecord) {
        notifyOnBookRecordRemove(bookRecord.getId());
        new BookManager().delete(bookRecord.getId());
    }

    public List<BookRecord> getBookRecords() {
        RealmResults<BookRecord> results = Realm.getDefaultInstance().where(BookRecord.class).findAll();
        results.sort("id", Sort.DESCENDING);
        return new ArrayList<>(results);
    }

    @Nullable
    public BookRecord getBookRecord(long bookRecordId) {
        return Realm.getDefaultInstance().where(BookRecord.class).equalTo("id", bookRecordId).findFirst();
    }

    private void notifyOnBookRecordCreate() {
        for (Observer observer : observers)
            observer.notifyBookRecordCreate();
    }

    private void notifyOnBookRecordUpdate(long bookRecordId) {
        for (Observer observer : observers)
            observer.notifyBookRecordUpdate(bookRecordId);
    }

    private void notifyOnBookRecordRemove(long bookRecordId) {
        for (Observer observer : observers)
            observer.notifyBookRecordRemove(bookRecordId);
    }

    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        observers.remove(observer);
    }

    interface Observer {
        void notifyBookRecordCreate();

        void notifyBookRecordUpdate(long bookRecordId);

        void notifyBookRecordRemove(long bookRecordId);
    }
}