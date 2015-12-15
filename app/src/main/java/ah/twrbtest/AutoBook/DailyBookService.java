package ah.twrbtest.AutoBook;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Events.OnBookableRecordFoundEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Helper.AsyncBookHelper;
import ah.twrbtest.Helper.NotifiableAsyncTask;
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
        return checkTime(Calendar.getInstance());
    }

    private static boolean checkTime(Calendar now) {
        Calendar begin = (Calendar) now.clone();
        Calendar end = (Calendar) now.clone();
        begin.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        begin.set(Calendar.HOUR, BEGIN_H);
        begin.set(Calendar.MINUTE, BEGIN_M);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        end.set(Calendar.HOUR, END_H);
        end.set(Calendar.MINUTE, END_M);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        return now.equals(begin) || now.after(begin) || now.before(end) || now.equals(end);
    }

    public static long getNextStartTimeInterval(Calendar now) {
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.HOUR_OF_DAY) >= BEGIN_H)
            c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, BEGIN_H);
        c.set(Calendar.MINUTE, BEGIN_M);
        c.set(Calendar.SECOND, (int) (Math.random() * RANDOM_SERVICE_INTERVAL_FACTOR));
        return c.getTimeInMillis() - now.getTimeInMillis();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println(this.getClass().getName() + " onHandleIntent.");
        try {
            startForeground();
            bookUntilEndTimeOrNoBookableRecord();
            Calendar c = Calendar.getInstance();
            MyApplication.getInstance().registerServiceAlarm(this.getClass(), getNextStartTimeInterval(c) + c.getTimeInMillis());
            checkHasBookableRecord();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopForeground();
        }
    }

    private void checkHasBookableRecord() {
        if (!getAllBookableRecords(Calendar.getInstance()).isEmpty())
            EventBus.getDefault().post(new OnBookableRecordFoundEvent());
    }

    private void registNextTime() {
        MyApplication.getInstance().registerServiceAlarm(this.getClass(), getNextStartTimeInterval(Calendar.getInstance()) + System.currentTimeMillis());
    }

    private void stopForeground() {
        stopForeground(true);
    }

    private void startForeground() {
        Notification n = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("午夜訂票")
                .setContentText("等這個通知消失了再去看看有沒有訂到票吧！")
                .build();
        startForeground(1, n);
    }

    private void bookUntilEndTimeOrNoBookableRecord() {
        while (checkTime()) {
            if (getAllBookableRecords(Calendar.getInstance()).isEmpty()) {
                System.out.println("No bookable record, stop DailyBookService.");
                return;
            }
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
        System.out.println("Not in specific time, stop DailyBookService.");
    }

    private void book() {
        ArrayList<BookRecord> results = getAllBookableRecords(Calendar.getInstance());
        for (BookRecord bookRecord : results)
            if (!bookers.containsKey(bookRecord.getId()))
                bookers.put(bookRecord.getId(), new AutoBooker(bookRecord));
    }

    @NonNull
    private ArrayList<BookRecord> getAllBookableRecords(Calendar now) {
        Realm.getDefaultInstance().refresh();
        RealmResults<BookRecord> rr = Realm.getDefaultInstance()
                .where(BookRecord.class)
                .equalTo("code", "")
                .equalTo("isCancelled", false)
                .findAll();
        ArrayList<BookRecord> brs = new ArrayList<>();
        for (BookRecord br : rr)
            if (BookRecord.isBookable(br, now))
                brs.add(br);
        return brs;
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
            if (!checkTime()) {
                System.out.println(String.format("AutoBooker of BookRecord[%d] out of checking time.", bookRecord.getId()));
                finish();
                return;
            }
            AsyncBookHelper abh = new AsyncBookHelper(this.bookRecord);
            abh.setOnPostExecuteListener(new NotifiableAsyncTask.OnPostExecuteListener() {
                @Override
                public void onPostExecute(NotifiableAsyncTask notifiableAsyncTask) {
                    Boolean result = (Boolean) notifiableAsyncTask.getResult();
                    if (result == null)
                        result = false;
                    if (!result)
                        book();
                    else {
                        EventBus.getDefault().post(new OnBookedEvent(bookRecord.getId(), result));
                        System.out.println(String.format("AutoBooker of BookRecord[%d] booked success.", bookRecord.getId()));
                        finish();
                    }
                }
            });
            abh.execute();
        }
    }
}