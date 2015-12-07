package ah.twrbtest.AutoBook;

import android.test.ServiceTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Calendar;

import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class DailyBookServiceTest extends ServiceTestCase<DailyBookService> {
    public DailyBookServiceTest() {
        super(DailyBookService.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).build();
        Realm.setDefaultConfiguration(config);
        Realm realm = Realm.getDefaultInstance();
        BookRecord br;
        Calendar c = Calendar.getInstance();
        realm.beginTransaction();
        realm.where(BookRecord.class).findAll().clear();
        for (int i = 1; i <= 5; i++) {
            c.set(2015, 0, i);
            br = new BookRecord();
            br.setGetInDate(c.getTime());
            realm.copyToRealm(br);
        }
        realm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void testGetAllBookableRecords() throws Exception {
        Method method = DailyBookService.class.getDeclaredMethod("getAllBookableRecords", null);
        method.setAccessible(true);
        RealmResults<BookRecord> rr = (RealmResults<BookRecord>) method.invoke(new DailyBookService(), null);
        assertEquals(5, rr.size());
    }
}