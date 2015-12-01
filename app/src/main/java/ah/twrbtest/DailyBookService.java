package ah.twrbtest;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Helper.AsyncBookHelper;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Wei on 2015/11/27.
 * 在 Application.onCreate 中呼叫此 service，並在此 service.onDestroy 中註冊下次自動執行
 *
 * 有可能會發生多個 service 同時運行的狀況
 */
public class DailyBookService extends IntentService {

    private static final int BEGIN_H = 23;
    private static final int BEGIN_M = 59;
    private static final int END_H = 0;
    private static final int END_M = 5;

    private Hashtable<Long, AutoBooker> bookers = new Hashtable<>();

    public DailyBookService() {
        super(DailyBookService.class.getName());
    }

    public static boolean checkTime() {
        java.util.Calendar calendar = Calendar.getInstance();
        int h = calendar.getTime().getHours();
        int m = calendar.getTime().getMinutes();
        return (h == BEGIN_H || h == END_H) && (m >= BEGIN_M || m <= END_M);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("Daily book service start.");
        book();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerService();
    }

    private void book() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkTime()) {
                    RealmResults<BookRecord> results = Realm.getDefaultInstance()
                            .where(BookRecord.class)
                            .equalTo("code", "")
                            .equalTo("isCancelled", false)
                            .findAll();
                    for (BookRecord bookRecord : results)
                        if (!bookers.containsKey(bookRecord.getId()))
                            bookers.put(bookRecord.getId(), new AutoBooker(bookRecord));
                } else {
                    System.out.println("Not in specific time, daily book service stopped.");
                    timer.cancel();
                }
            }
        }, 0, 3000);
    }

    private void registerService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(DailyBookService.this, DailyBookService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, getNextTimeMillis(), pendingIntent);
    }

    private long getNextTimeMillis() {
        Calendar c = Calendar.getInstance();
        if (c.getTime().getHours() >= BEGIN_H)
            c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, BEGIN_H);
        c.set(Calendar.MINUTE, BEGIN_M);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() + SystemClock.elapsedRealtime();
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