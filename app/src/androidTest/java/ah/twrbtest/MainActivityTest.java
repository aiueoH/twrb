package ah.twrbtest;

import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;

import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        RealmConfiguration config = new RealmConfiguration.Builder(getActivity()).build();
        Realm.setDefaultConfiguration(config);
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().where(BookRecord.class).findAll().clear();
        Realm.getDefaultInstance().commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
    }

    public void testActivityExists() {
        MainActivity activity = getActivity();
        assertNotNull(activity);
    }
}