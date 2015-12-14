package ah.twrbtest.AutoBook;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Events.OnBookableRecordFoundEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.Helper.AsyncBookHelper;
import ah.twrbtest.MyApplication;
import de.greenrobot.event.EventBus;
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
        if (getBookableBookRecord(Calendar.getInstance()).isEmpty())
            System.out.println("No bookable BookRecord, do not register next start.");
        else
            EventBus.getDefault().post(new OnBookableRecordFoundEvent());
        Realm.getDefaultInstance().close();
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
                Boolean result = abh.getResult();
                if (result != null) {
                    EventBus.getDefault().post(new OnBookedEvent(id, result));
                }
                try {
                    long interval = getRandomBookInterval();
                    System.out.println(this.getClass().getName() + " book break " + interval + " ns.");
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
        return (long) (BOOK_INTERVAL + RANDOM_BOOK_INTERVAL_FACTOR * (Math.random() - 0.5));
    }
}
