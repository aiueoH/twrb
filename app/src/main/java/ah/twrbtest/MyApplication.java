package ah.twrbtest;

import android.app.AlarmManager;
import android.app.Application;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.twrb.core.MyLogger;
import com.twrb.core.helpers.DefaultSequenceRecognizerCreator;
import com.twrb.core.util.MulawReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import ah.twrbtest.AutoBook.DailyBookService;
import ah.twrbtest.AutoBook.FrequentlyBookService;
import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.DBObject.BookableStation;
import ah.twrbtest.DBObject.City;
import ah.twrbtest.DBObject.TimetableStation;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import ah.twrbtest.Events.OnBookableRecordFoundEvent;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setLogger();
        setupRealm();
        setupTimetableStationIfNotExist();
        setupCityIfNotExist();
        setupBookableStationIfNotExist();
        setupPronounceSamples();
        EventBus.getDefault().register(this);
        registerServiceAlarm(DailyBookService.class, DailyBookService.getNextStartTimeInterval(Calendar.getInstance()) + System.currentTimeMillis());
        registerServiceAlarm(FrequentlyBookService.class, FrequentlyBookService.getNextStartTimeInterval() + System.currentTimeMillis());
    }

    private void setLogger() {
        MyPrinter mp = new MyPrinter();
        mp.setEnable(true);
        MyLogger.setPrinter(mp);
    }

    public void onEvent(OnBookRecordAddedEvent e) {
        MyLogger.i("MyApplication received OnBookRecordAddedEvent.");
        BookRecord br = BookRecord.get(e.getBookRecordId());
        if (br != null && BookRecord.isBookable(br, Calendar.getInstance()))
            EventBus.getDefault().post(new OnBookableRecordFoundEvent());
    }

    public void onEvent(OnBookableRecordFoundEvent e) {
        MyLogger.i("MyApplication received OnBookableRecordFoundEvent.");
        long dailyBookServiceStartTime = DailyBookService.checkTime() ? 0 : DailyBookService.getNextStartTimeInterval(Calendar.getInstance()) + System.currentTimeMillis();
        registerServiceAlarm(DailyBookService.class, dailyBookServiceStartTime);
        registerServiceAlarm(FrequentlyBookService.class, FrequentlyBookService.getNextStartTimeInterval() + System.currentTimeMillis());
    }

    public void registerServiceAlarm(Class<? extends IntentService> cls, long startTime) {
        Intent intent = new Intent(this, cls);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startTime);
        String s = c.getTime().toString();
        MyLogger.i(cls.getName() + " will start at " + s + ".");
    }

    public void cancelPendingIntentService(Class<? extends IntentService> cls) {
        PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, cls), PendingIntent.FLAG_NO_CREATE);
        if (pi != null)
            pi.cancel();
    }

    private List<String> readAllLines(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<>();
        String line;
        while (true) {
            line = br.readLine();
            if (line == null)
                break;
            lines.add(line);
        }
        return lines;
    }

    private void setupRealm() {
        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
    }

    private void setupTimetableStationIfNotExist() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<TimetableStation> rr = realm.where(TimetableStation.class).findAll();
        if (!rr.isEmpty()) {
            MyLogger.i("Timetable stations already existing, " + rr.size() + " stations.");
            return;
        }
        try {
            List<String> lines = readAllLines(getAssets().open(getString(R.string.file_timetableStation)));
            ArrayList<TimetableStation> tss = new ArrayList<>();
            TimetableStation ts;
            for (String s : lines) {
                String[] data = s.split(",");
                ts = new TimetableStation();
                tss.add(ts);
                ts.setCityNo(data[0]);
                ts.setNo(data[1]);
                ts.setNameCh(data[2]);
                ts.setNameEn(data[3]);
                ts.setBookNo(data[4]);
                ts.setIsBookable(!ts.getBookNo().equals("-1"));
            }
            realm.beginTransaction();
            realm.copyToRealm(tss);
            realm.commitTransaction();
            MyLogger.i("Import timetable stations OK.");
        } catch (IOException e) {
            MyLogger.i("Import timetable stations fail.");
            e.printStackTrace();
        }
    }

    private void setupCityIfNotExist() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<City> rr = realm.where(City.class).findAll();
        if (!rr.isEmpty()) {
            MyLogger.i("City already existing, " + rr.size() + " cities.");
            return;
        }
        try {
            List<String> lines = readAllLines(getAssets().open(getString(R.string.file_city)));
            ArrayList<City> cs = new ArrayList<>();
            City c;
            for (String s : lines) {
                String[] data = s.split(",");
                c = new City();
                cs.add(c);
                c.setNo(data[0]);
                c.setNameCh(data[1]);
                c.setNameEn(data[2]);
                RealmList<TimetableStation> list = new RealmList<>();
                for (TimetableStation ts : realm.where(TimetableStation.class).equalTo("cityNo", c.getNo()).findAll())
                    list.add(ts);
                c.setTimetableStations(list);
            }
            realm.beginTransaction();
            realm.copyToRealm(cs);
            realm.commitTransaction();
            MyLogger.i("Import cities OK.");
        } catch (IOException e) {
            MyLogger.i("Import cities fail.");
            e.printStackTrace();
        }
    }

    private void setupBookableStationIfNotExist() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<BookableStation> results = realm.where(BookableStation.class).findAll();
        if (results.isEmpty()) {
            try {
                List<String> lines = readAllLines(getAssets().open(getString(R.string.file_bookablestation)));
                realm.beginTransaction();
                BookableStation bs;
                for (String s : lines) {
                    String[] data = s.split(",");
                    bs = realm.createObject(BookableStation.class);
                    bs.setNo(data[0]);
                    bs.setName(data[1]);
                }
                realm.commitTransaction();
                MyLogger.i("Import bookable stations OK.");
            } catch (IOException e) {
                MyLogger.i("Import bookable stations fail.");
                e.printStackTrace();
            }
        } else {
            MyLogger.i("Bookable stations already existing, " + results.size() + " stations.");
        }
    }

    private void setupPronounceSamples() {
        HashMap<String, int[]> samples = new HashMap<>();
        String path = getString(R.string.default_pronounce_sample_path);
        String files[] = {"sun", "nueng", "song", "sam", "si", "ha", "hok", "chet", "paet", "kao"};
        for (int i = 0; i < 10; i++) {
            String key = String.valueOf(i);
            String file = path.replace("[name]", files[i]);
            InputStream stream;
            try {
                stream = getAssets().open(file);
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes);
                int[] pcm = new MulawReader(bytes).getPCMData();
                samples.put(key, pcm);
                MyLogger.i("Load and decode " + file + " OK. Sample length:" + pcm.length);
            } catch (IOException e) {
                MyLogger.i("Load and decode " + file + " fail.");
                e.printStackTrace();
            }
        }
        DefaultSequenceRecognizerCreator.DEFAULT_SAMPLES = samples;
    }
}
