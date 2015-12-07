package ah.twrbtest;

import android.app.AlarmManager;
import android.app.Application;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.twrb.core.helpers.DefaultSequenceRecognizerCreator;
import com.twrb.core.util.MulawReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import ah.twrbtest.AutoBook.DailyBookService;
import ah.twrbtest.AutoBook.FrequentlyBookService;
import ah.twrbtest.DBObject.BookableStation;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmConfiguration;
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
        setupRealm();
        setupBookableStationIfNotExist();
        setupPronounceSamples();
        EventBus.getDefault().register(this);
        registerServiceAlarmIfNotExist(DailyBookService.class, Calendar.getInstance().getTimeInMillis());
        registerServiceAlarmIfNotExist(FrequentlyBookService.class, FrequentlyBookService.getNextStartTime());
    }

    private void setupRealm() {
        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
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
                System.out.println("Import stations OK.");
            } catch (IOException e) {
                System.out.println("Import stations fail.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Bookable stations already existing, " + results.size() + " stations.");
        }
    }

    public void onEvent(OnBookRecordAddedEvent e) {
        System.out.println("MyApplication received OnBookRecordAddedEvent.");
        registerServiceAlarmIfNotExist(DailyBookService.class, Calendar.getInstance().getTimeInMillis());
        registerServiceAlarmIfNotExist(FrequentlyBookService.class, FrequentlyBookService.getNextStartTime());
    }

    public void registerServiceAlarmIfNotExist(Class<? extends IntentService> cls, long startTime) {
        Intent intent = new Intent(this, cls);
        if (PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_NO_CREATE) == null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            System.out.println(cls.getName() + " will start at " + new Date(startTime).toString() + ".");
        } else {
            System.out.println(cls.getName() + " already exist.");
        }
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

    private void setupPronounceSamples() {
        HashMap<String, int[]> samples = new HashMap<>();
        String path = getString(R.string.default_pronounce_sample_path);
        for (int i = 0; i < 10; i++) {
            String key = String.valueOf(i);
            String file = path.replace("[num]", String.valueOf(i));
            InputStream stream;
            try {
                stream = getAssets().open(file);
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes);
                int[] pcm = new MulawReader(bytes).getPCMData();
                samples.put(key, pcm);
                System.out.println("Load and decode " + file + " OK. Sample length:" + pcm.length);
            } catch (IOException e) {
                System.out.println("Load and decode " + file + " fail.");
                e.printStackTrace();
            }
        }
        DefaultSequenceRecognizerCreator.DEFAULT_SAMPLES = samples;
    }
}
