package com.dowob.twrb.features.tickets;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.events.OnBookRecordAddedEvent;
import com.dowob.twrb.events.OnBookedEvent;
import com.dowob.twrb.features.tickets.book.BookRecordFactory;
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

    public void book(Context context, Order order, TrainInfo trainInfo, BookListener bookListener) {
        new NewOrderBookExecutor(context, bookListener, new Order(order.getFrom(), order.getTo(), order.getGetInDate(), order.getNo(), order.getQty(), order.getPersonId()), trainInfo).book();
    }

    public void book(Context context, long bookRecordId, BookListener bookListener) {
        new ExistingOrderBookExecutor(context, bookRecordId, bookListener).book();
    }

    public long save(Order order, TrainInfo trainInfo) {
        long id = BookRecordFactory.createBookRecord(order, trainInfo).getId();
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
            webViewBooker.sendOrder(order.getWebViewBookerOrder());
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

        public NewOrderBookExecutor(Context context, BookListener bookListener, Order order, TrainInfo trainInfo) {
            super(context, bookListener);
            this.order = order;
            this.trainInfo = trainInfo;
        }

        @Override
        public void onBooked(com.dowob.twrb.features.tickets.book.BookResult bookResult) {
            if (bookResult.isOK()) {
                BookRecord bookRecord = BookRecordFactory.createBookRecord(order, bookResult.getCode(), trainInfo);
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
            this.order = Order.createByBookRecordId(bookRecordId);
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
