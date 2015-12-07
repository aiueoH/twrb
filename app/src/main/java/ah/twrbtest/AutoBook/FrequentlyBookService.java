package ah.twrbtest.AutoBook;

import android.app.IntentService;
import android.content.Intent;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Helper.AsyncBookHelper;
import ah.twrbtest.MyApplication;
import io.realm.Realm;
import io.realm.RealmResults;

public class FrequentlyBookService extends IntentService {
    // 每筆訂票的間隔時間
    private static final long BOOK_INTERVAL = 30 * 1000; // 30 seconds
    // 每筆訂票的間隔隨機增加時間因子(會乘上 +-0.5)
    private static final long RANDOM_BOOK_INTERVAL_FACTOR = 10 * 1000; // 10 seconds
    // 下一次啟動 service 的時間
    private static final long SERVICE_INTERVAL = 5 * 60 * 1000; // 5 minutes
    // 下一次啟動 service 的隨機增加時間因子(會乘上 +-0.5)
    private static final long RANDOM_SERVICE_INTERVAL_FACTOR = 3 * 60 * 1000; // 3 minutes

    public FrequentlyBookService() {
        super("FrequentlyBookService");
    }

    public static long getNextStartTimeInterval() {
        return (long) (SERVICE_INTERVAL + RANDOM_SERVICE_INTERVAL_FACTOR * (Math.random() - 0.5));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println(this.getClass().getName() + " onHandleIntent.");
        MyApplication.getInstance().cancelPendingIntentService(this.getClass());
        if (!DailyBookService.checkTime())
            book();
    }

    @Override
    public void onDestroy() {
        System.out.println(this.getClass().getName() + " onDestroy.");
        super.onDestroy();
        if (getBookableBookRecord().isEmpty())
            System.out.println("No bookable BookRecord, do not register next start.");
        else
            MyApplication.getInstance().registerServiceAlarmIfNotExist(this.getClass(), getNextStartTimeInterval() + System.currentTimeMillis());
    }

    private void book() {
        RealmResults<BookRecord> results = getBookableBookRecord();
        for (BookRecord bookRecord : results) {
            new AsyncBookHelper(bookRecord).execute((long) 0);
            try {
                long interval = getRandomBookInterval();
                System.out.println(this.getClass().getName() + " book break " + interval + " ns.");
                Thread.sleep(getRandomBookInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private RealmResults<BookRecord> getBookableBookRecord() {
        return Realm.getDefaultInstance()
                .where(BookRecord.class)
                .equalTo("code", "")
                .equalTo("isCancelled", false)
                .findAll();
    }

    private long getRandomBookInterval() {
        return (long) (BOOK_INTERVAL + RANDOM_BOOK_INTERVAL_FACTOR * (Math.random() - 0.5));
    }
}
