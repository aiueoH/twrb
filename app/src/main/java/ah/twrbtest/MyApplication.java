package ah.twrbtest;

import android.app.Application;
import android.content.Intent;

import com.twrb.core.helpers.DefaultSequenceRecognizerCreator;
import com.twrb.core.util.MulawReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ah.twrbtest.AutoBook.DailyBookService;
import ah.twrbtest.AutoBook.FrequentlyBookService;
import ah.twrbtest.DBObject.BookableStation;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);

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
        setupPronounceSamples();

        startService(new Intent(MyApplication.this, DailyBookService.class));
        startService(new Intent(MyApplication.this, FrequentlyBookService.class));
    }

    public void onEvent(OnBookRecordAddedEvent e) {
        startService(new Intent(MyApplication.this, FrequentlyBookService.class));
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
