package com.dowob.twrb.features.tickets;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.events.OnBookRecordAddedEvent;
import com.dowob.twrb.events.OnBookedEvent;
import com.dowob.twrb.features.tickets.book.BookRecordFactory;
import com.dowob.webviewbooker.Order;
import com.dowob.webviewbooker.WebViewBooker;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.book.BookResult;
import com.twrb.core.helpers.BookHelper;
import com.twrb.core.timetable.TrainInfo;

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
    private List<Observer> observers = new ArrayList<>();

    private BookRecordModel() {
        EventBus.getDefault().register(this);
    }

    public static BookRecordModel getInstance() {
        return instance;
    }

    public void onEvent(OnBookRecordAddedEvent e) {
        notifyOnBookRecordCreate(e.getBookRecordId());
    }

    public void book(Context context, String from, String to, Calendar getInDate, String no, int qty, String personId, TrainInfo trainInfo, BookListener bookListener) {
        new NewOrderBookExecutor(context, bookListener, from, to, getInDate, no, qty, personId, trainInfo).book();
    }

    public void book(Context context, long bookRecordId, BookListener bookListener) {
        new ExistingOrderBookExecutor(context, bookRecordId, bookListener).book();
    }

    public long save(BookInfo bookInfo, TrainInfo trainInfo) {
        long id = BookRecordFactory.createBookRecord(bookInfo, trainInfo).getId();
        EventBus.getDefault().post(new OnBookRecordAddedEvent(id));
        return id;
    }

    public boolean cancel(long bookRecordId) {
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
        if (result)
            notifyOnBookRecordUpdate(bookRecordId);
        return result;
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
        notifyOnBookRecordRemove(bookRecordId);
        return true;
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

    private void notifyOnBookRecordCreate(long bookRecordId) {
        for (Observer observer : observers)
            observer.notifyBookRecordCreate(bookRecordId);
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
        default void notifyBookRecordCreate(long bookRecordId) {
        }

        void notifyBookRecordUpdate(long bookRecordId);

        void notifyBookRecordRemove(long bookRecordId);
    }

    interface BookListener {
        void onRequireRandomInput(RandomInputReceiver randomInputReceiver, Bitmap captcha);

        void onFinish(com.dowob.twrb.features.tickets.book.BookResult bookResult);
    }

    public interface RandomInputReceiver {
        void answerRandomInput(String randomInput);
    }

    private abstract class BookExecutor implements RandomInputReceiver {
        private Context context;
        protected Order order;
        private BookListener bookListener;
        protected WebViewBooker webViewBooker;

        public BookExecutor(Context context, BookListener bookListener) {
            this.context = context;
            this.bookListener = bookListener;
        }

        public void book() {
            webViewBooker = new WebViewBooker(context,
                    this::onWebViewBookerRequireCaptcha,
                    this::onWebViewBookerFinish);
            webViewBooker.sendOrder(order);
        }

        final public void onWebViewBookerRequireCaptcha(Bitmap captchaImg) {
            bookListener.onRequireRandomInput(this, captchaImg);
        }

        final public void onWebViewBookerFinish(AbstractMap.SimpleEntry<BookResult, List<String>> result) {
            onBooked(new com.dowob.twrb.features.tickets.book.BookResult(result));
        }

        protected void onBooked(com.dowob.twrb.features.tickets.book.BookResult bookResult) {
            webViewBooker.destroy();
            bookListener.onFinish(bookResult);
        }

        @Override
        public void answerRandomInput(String randomInput) {
            if (randomInput != null)
                webViewBooker.sendCaptcha(randomInput);
            else
                webViewBooker.destroy();
        }
    }

    private class NewOrderBookExecutor extends BookExecutor {
        private TrainInfo trainInfo;
        private String from, to, no, personId;
        private Calendar getInDate;
        private int qty;

        public NewOrderBookExecutor(Context context, BookListener bookListener,
                                    String from,
                                    String to,
                                    Calendar getInDate,
                                    String no,
                                    int qty,
                                    String personId,
                                    TrainInfo trainInfo) {
            super(context, bookListener);
            this.trainInfo = trainInfo;
            this.from = from;
            this.to = to;
            this.no = no;
            this.personId = personId;
            this.getInDate = getInDate;
            this.qty = qty;
            this.order = new Order.Builder()
                    .setFrom(from)
                    .setTo(to)
                    .setGetInDate(getInDate)
                    .setPersonId(personId)
                    .setTrainNo(no)
                    .setQty(qty)
                    .createOrder();
        }

        @Override
        public void onBooked(com.dowob.twrb.features.tickets.book.BookResult bookResult) {
            if (bookResult.isOK()) {
                BookRecord bookRecord = BookRecordFactory
                        .createBookRecord(personId,
                                getInDate,
                                from,
                                to,
                                qty,
                                no,
                                "0",
                                bookResult.getCode(),
                                trainInfo);
                bookResult.setBookRecordId(bookRecord.getId());
                EventBus.getDefault().post(new OnBookRecordAddedEvent(bookRecord.getId()));
                EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), bookResult.getStatus()));
            }
            super.onBooked(bookResult);
        }
    }

    private class ExistingOrderBookExecutor extends BookExecutor {
        private long bookRecordId;

        public ExistingOrderBookExecutor(Context context, long bookRecordId, BookListener bookListener) {
            super(context, bookListener);
            this.bookRecordId = bookRecordId;
            BookRecord bookRecord = BookRecord.get(bookRecordId);
            String from = bookRecord.getFromStation();
            String to = bookRecord.getToStation();
            Calendar getInDate = Calendar.getInstance();
            getInDate.setTime(bookRecord.getGetInDate());
            String no = bookRecord.getTrainNo();
            int qty = bookRecord.getOrderQtu();
            String personId = bookRecord.getPersonId();
            order = new Order.Builder()
                    .setFrom(from)
                    .setTo(to)
                    .setGetInDate(getInDate)
                    .setPersonId(personId)
                    .setTrainNo(no)
                    .setQty(qty)
                    .createOrder();
        }

        @Override
        public void onBooked(com.dowob.twrb.features.tickets.book.BookResult bookResult) {
            if (bookResult.isOK()) {
                BookRecord bookRecord = BookRecord.get(bookRecordId);
                Realm.getDefaultInstance().beginTransaction();
                bookRecord.setCode(bookResult.getCode());
                Realm.getDefaultInstance().commitTransaction();
                notifyOnBookRecordUpdate(bookRecordId);
            }
            super.onBooked(bookResult);
        }
    }
}
