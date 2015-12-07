package ah.twrbtest.AutoBook;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Helper.AsyncBookHelper;
import ah.twrbtest.MyApplication;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 在 Application.onCreate 中呼叫此 service，並在此 service.onDestroy 中註冊下次自動執行
 *
 * 有可能會發生多個 service 同時運行的狀況
 */
public class DailyBookService extends IntentService {
    // 每日訂票時間
    private static final int BEGIN_H = 23;
    private static final int BEGIN_M = 59;
    private static final int END_H = 0;
    private static final int END_M = 5;
    // 確認所有訂票器結束的間隔時間(沒有全部結束不會離開 service)
    private static final long WAIT_BOOKERS_ALL_FINISH_BREAKTIME = 500;  // 500 n-seconds
    // 每一次搜尋新的待訂票間隔時間
    private static final long BOOK_INTERVAL = 3 * 1000; // 3 seconds
    // 下一次啟動 service 的隨機增加時間因子(會乘上 +-0.5)
    private static final int RANDOM_SERVICE_INTERVAL_FACTOR = 30; // 30 seconds

    private Hashtable<Long, AutoBooker> bookers = new Hashtable<>();

    public DailyBookService() {
        super("DailyBookService");
    }

    public static boolean checkTime() {
        java.util.Calendar calendar = Calendar.getInstance();
        int h = calendar.getTime().getHours();
        int m = calendar.getTime().getMinutes();
        return (h == BEGIN_H || h == END_H) && (m >= BEGIN_M || m <= END_M);
    }

    public static long getNextStartTime() {
        Calendar c = Calendar.getInstance();
        if (c.getTime().getHours() >= BEGIN_H)
            c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, BEGIN_H);
        c.set(Calendar.MINUTE, BEGIN_M);
        c.set(Calendar.SECOND, (int) (Math.random() * RANDOM_SERVICE_INTERVAL_FACTOR));
        return c.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println(this.getClass().getName() + " onHandleIntent.");
        MyApplication.getInstance().cancelPendingIntentService(this.getClass());
        bookUntilEndTime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println(this.getClass().getName() + " onDestroy.");
        long interval = getNextStartTime();
        System.out.println(this.getClass().getName() + " will start again at " + new Date(interval + Calendar.getInstance().getTimeInMillis()).toString() + ".");
        registerNextStart(interval + SystemClock.elapsedRealtime());
    }

    private void bookUntilEndTime() {
        while (checkTime()) {
            book();
            try {
                Thread.sleep(BOOK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (!this.bookers.isEmpty())
            try {
                Thread.sleep(WAIT_BOOKERS_ALL_FINISH_BREAKTIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        System.out.println("Not in specific time, will stop DailyBookService.");
    }

    private void book() {
        RealmResults<BookRecord> results = Realm.getDefaultInstance()
                .where(BookRecord.class)
                .equalTo("code", "")
                .equalTo("isCancelled", false)
                .findAll();
        for (BookRecord bookRecord : results)
            if (!bookers.containsKey(bookRecord.getId()))
                bookers.put(bookRecord.getId(), new AutoBooker(bookRecord));
    }

    private void registerNextStart(long nextTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTimeMillis, pendingIntent);
    }

    private class AutoBooker {
        private BookRecord bookRecord;

        public AutoBooker(BookRecord bookRecord) {
            this.bookRecord = bookRecord;
            EventBus.getDefault().register(this);
            book();
        }

        private void finish() {
            EventBus.getDefault().unregister(this);
            bookers.remove(bookRecord.getId());
        }

        private void book() {
            if (checkTime())
                new AsyncBookHelper(this.bookRecord).execute((long) 0);
            else {
                System.out.println(String.format("AutoBooker of BookRecord[%d] out of checking time.", bookRecord.getId()));
                finish();
            }
        }

        public void onEvent(OnBookedEvent e) {
            if (!e.isSuccess()) {
                book();
            } else {
                System.out.println(String.format("AutoBooker of BookRecord[%d] booked success.", bookRecord.getId()));
                finish();
            }
        }
    }
}