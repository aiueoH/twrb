package ah.twrbtest.AutoBook;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;
import io.realm.RealmConfiguration;

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
        realm.beginTransaction();
        realm.where(BookRecord.class).findAll().clear();
        realm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(BookRecord.class).findAll().clear();
        realm.commitTransaction();
    }

    @SmallTest
    public void testStartable() {
        setService();
        assertNotNull(getService());
    }

    @SuppressWarnings("unchecked")
    @SmallTest
    public void testGetAllBookableRecords() throws Exception {
        setService();
        Field begin_h, begin_m, end_h, end_m;
        Calendar c;
        Method method;
        ArrayList<BookRecord> bookRecords;
        method = DailyBookService.class.getDeclaredMethod("getAllBookableRecords", Calendar.class);
        begin_h = DailyBookService.class.getDeclaredField("BEGIN_H");
        begin_h.setAccessible(true);
        begin_m = DailyBookService.class.getDeclaredField("BEGIN_M");
        begin_m.setAccessible(true);
        end_h = DailyBookService.class.getDeclaredField("END_H");
        end_h.setAccessible(true);
        end_m = DailyBookService.class.getDeclaredField("END_M");
        end_m.setAccessible(true);
        method.setAccessible(true);

        addBookRecord(2015, 1, 1);
        addBookRecord(2015, 1, 2);
        addBookRecord(2015, 1, 3);
        addBookRecord(2015, 1, 3);
        addBookRecord(2015, 1, 13);
        addBookRecord(2015, 1, 14);
        addBookRecord(2015, 1, 15);
        addBookRecord(2015, 1, 16);
        addBookRecord(2015, 1, 17);
        addBookRecord(2015, 1, 19);
        addBookRecord(2015, 1, 20);
        addBookRecord(2015, 1, 21);

        c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.set(2015, 0, 1, 0, 0);
        bookRecords = (ArrayList<BookRecord>) method.invoke(getService(), c);
        assertEquals(0, bookRecords.size());

        c.set(2015, 1, 1, (int) begin_h.get(null), (int) begin_m.get(null) - 1);
        bookRecords = (ArrayList<BookRecord>) method.invoke(getService(), c);
        assertEquals(7, bookRecords.size());

        c.set(2015, 1, 1, (int) begin_h.get(null), (int) begin_m.get(null));
        bookRecords = (ArrayList<BookRecord>) method.invoke(getService(), c);
        assertEquals(8, bookRecords.size());
    }

    private void setService() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), DailyBookService.class);
        startService(startIntent);
    }

    private void addBookRecord(int y, int m, int d) throws Exception {
        Realm.getDefaultInstance().beginTransaction();
        Calendar c = Calendar.getInstance();
        c.set(y, m, d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        BookRecord br = new BookRecord();
        br.setGetInDate(c.getTime());
        Realm.getDefaultInstance().copyToRealm(br);
        Realm.getDefaultInstance().commitTransaction();
    }
}
