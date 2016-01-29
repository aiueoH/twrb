package ah.twrbtest.AutoBook;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.NotificationCompat;

import com.twrb.core.MyLogger;
import com.twrb.core.book.BookResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Events.OnBookableRecordFoundEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Helper.AsyncBookHelper;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

public class FrequentlyBookService extends IntentService {
    // 每筆訂票的間隔時間
    private static final long BOOK_INTERVAL = 30 * 1000; // 30 seconds
    // 每筆訂票的間隔隨機增加時間因子(會乘上 +-0.5)
    private static final long RANDOM_BOOK_INTERVAL_FACTOR = 10 * 1000; // 10 seconds
    // 下一次啟動 service 的時間
    private static final long SERVICE_INTERVAL = 10 * 60 * 1000; // 10 minutes
    // 下一次啟動 service 的隨機增加時間因子
    private static final long RANDOM_SERVICE_INTERVAL_FACTOR = 5 * 60 * 1000; // 5 minutes

    public FrequentlyBookService() {
        super("FrequentlyBookService");
    }

    public static long getNextStartTimeInterval() {
        return (long) (SERVICE_INTERVAL + RANDOM_SERVICE_INTERVAL_FACTOR * (Math.random() - 0.5));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MyLogger.i(this.getClass().getName() + " onHandleIntent.");
        try {
            startForeground();
            if (DailyBookService.checkTime() || !checkLastStartTime())
                return;
            book();
            setLastFinishTime();
            checkHasBookableRecord();
            Realm.getDefaultInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopForeground();
        }
    }

    private void stopForeground() {
        stopForeground(true);
    }

    private void startForeground() {
        Notification n = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("三不五時訂個票")
                .setContentText("等這個通知消失了再去看看有沒有訂到票吧！")
                .build();
        startForeground(1, n);
    }

    private void checkHasBookableRecord() {
        if (getBookableBookRecord(Calendar.getInstance()).isEmpty())
            MyLogger.i("No bookable BookRecord, do not register next start.");
        else
            EventBus.getDefault().post(new OnBookableRecordFoundEvent());
    }

    private void book() {
        Realm.getDefaultInstance().setAutoRefresh(true);
        HashSet<Long> allBR = new HashSet<>();
        ArrayList<BookRecord> rr = getBookableBookRecord(Calendar.getInstance());
        for (BookRecord br : rr)
            allBR.add(br.getId());
        while (true) {
            rr = getBookableBookRecord(Calendar.getInstance());
            boolean isExist = false;
            for (BookRecord br : rr) {
                long id = 0;
                try {
                    id = br.getId();
                    isExist = allBR.remove(id);
                } catch (Exception e) {
                    e.printStackTrace();
                    isExist = false;
                }
                if (!isExist)
                    continue;
                AsyncBookHelper abh = new AsyncBookHelper(br);
                abh.execute();
                BookResult result = abh.getResult();
                if (result != null) {
                    EventBus.getDefault().post(new OnBookedEvent(id, result));
                }
                if (allBR.isEmpty())
                    return;
                try {
                    long interval = getRandomBookInterval();
                    MyLogger.i(this.getClass().getName() + " book break " + interval + " ns.");
                    Thread.sleep(getRandomBookInterval());
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isExist)
                return;
        }
    }

    private void setLastFinishTime() {
        getSharedPreferences("twrbtest", Activity.MODE_PRIVATE)
                .edit()
                .putLong("lastFrequentlyBookServiceFinishTime", Calendar.getInstance().getTimeInMillis())
                .commit();
    }

    private boolean checkLastStartTime() {
        SharedPreferences sp = getSharedPreferences("twrbtest", Activity.MODE_PRIVATE);
        long lastTime = sp.getLong("lastFrequentlyBookServiceFinishTime", 0);
        if (Calendar.getInstance().getTimeInMillis() - lastTime < SERVICE_INTERVAL - RANDOM_BOOK_INTERVAL_FACTOR) {
            MyLogger.i(this.getClass().getName() + " start interval too short.");
            return false;
        }
        return true;
    }

    private ArrayList<BookRecord> getBookableBookRecord(Calendar now) {
        Realm.getDefaultInstance().refresh();
        RealmResults<BookRecord> rr = Realm.getDefaultInstance()
                .where(BookRecord.class)
                .equalTo("code", "")
                .equalTo("isCancelled", false)
                .findAll();
        ArrayList<BookRecord> brs = new ArrayList<>();
        for (BookRecord br : rr) {
            if (BookRecord.isBookable(br, now))
                brs.add(br);
        }
        return brs;
    }

    private long getRandomBookInterval() {
        int sign = Math.random() - 0.5 > 0 ? 1 : -1;
        return (long) (BOOK_INTERVAL + RANDOM_BOOK_INTERVAL_FACTOR * Math.random() * sign);
    }
}
