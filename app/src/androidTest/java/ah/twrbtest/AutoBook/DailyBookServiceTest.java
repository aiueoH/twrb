package ah.twrbtest.AutoBook;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;
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
        realm.beginTransaction();
        realm.where(BookRecord.class).findAll().clear();
        realm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void testStartable() {
        setService();
        assertNotNull(getService());
    }

    @SmallTest
    public void testGetAllBookableRecords() throws Exception {
        setService();
        Field begin_h, begin_m, end_h, end_m;
        Calendar c;
        Method method;
        RealmResults<BookRecord> rr;
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

        addBookRecord(2015, 0, 1);
        addBookRecord(2015, 0, 2);
        addBookRecord(2015, 0, 3);
        addBookRecord(2015, 0, 3);
        addBookRecord(2015, 0, 13);
        addBookRecord(2015, 0, 14);
        addBookRecord(2015, 0, 15);
        addBookRecord(2015, 0, 16);
        addBookRecord(2015, 0, 17);

        c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.set(2015, 0, 1, (int) begin_h.get(null), (int) begin_m.get(null) - 1);
        rr = (RealmResults<BookRecord>) method.invoke(getService(), c);
        assertEquals(5, rr.size());

        c.set(2015, 0, 1, (int) begin_h.get(null), (int) begin_m.get(null));
        rr = (RealmResults<BookRecord>) method.invoke(getService(), c);
        assertEquals(6, rr.size());
    }

    @SmallTest
    public void testGetBookableDateEnd() throws Exception {
        setService();

        Method method;
        Field begin_h, begin_m;
        method = DailyBookService.class.getDeclaredMethod("getBookableDateEnd", Calendar.class);
        method.setAccessible(true);
        begin_h = DailyBookService.class.getDeclaredField("BEGIN_H");
        begin_h.setAccessible(true);
        begin_m = DailyBookService.class.getDeclaredField("BEGIN_M");
        begin_m.setAccessible(true);

        Calendar c = Calendar.getInstance();
        Calendar result;

        c.set(2015, 0, 1, (int) begin_h.get(null), (int) begin_m.get(null) - 1);
        result = (Calendar) method.invoke(getService(), c);
        assertEquals(2015, result.get(Calendar.YEAR));
        assertEquals(0, result.get(Calendar.MONTH));
        assertEquals(14, result.get(Calendar.DATE));

        c.set(2015, 0, 1, (int) begin_h.get(null), (int) begin_m.get(null));
        result = (Calendar) method.invoke(getService(), c);
        assertEquals(2015, result.get(Calendar.YEAR));
        assertEquals(0, result.get(Calendar.MONTH));
        assertEquals(15, result.get(Calendar.DATE));
    }

    @SmallTest
    public void testSetHMSMsTo0() throws Exception {
        setService();
        Calendar c = Calendar.getInstance();
        Calendar result = (Calendar) c.clone();
        Method method = DailyBookService.class.getDeclaredMethod("setHMSMsTo0", Calendar.class);
        method.setAccessible(true);
        method.invoke(getService(), c);
        assertEquals(c.get(Calendar.YEAR), result.get(Calendar.YEAR));
        assertEquals(c.get(Calendar.MONTH), result.get(Calendar.MONTH));
        assertEquals(c.get(Calendar.DATE), result.get(Calendar.DATE));
        assertEquals(c.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(c.get(Calendar.MINUTE), 0);
        assertEquals(c.get(Calendar.SECOND), 0);
        assertEquals(c.get(Calendar.MILLISECOND), 0);
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
