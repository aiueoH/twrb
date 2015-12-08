package ah.twrbtest.AutoBook;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class DailyBookServiceTest {

    private DailyBookService dailyBookService;

    @Before
    public void setUp() throws Exception {
        this.dailyBookService = new DailyBookService();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetBookableDateEnd() throws Exception {
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
        result = (Calendar) method.invoke(this.dailyBookService, c);
        assertEquals(2015, result.get(Calendar.YEAR));
        assertEquals(0, result.get(Calendar.MONTH));
        assertEquals(15, result.get(Calendar.DATE));

        c.set(2015, 0, 1, (int) begin_h.get(null), (int) begin_m.get(null));
        result = (Calendar) method.invoke(this.dailyBookService, c);
        assertEquals(2015, result.get(Calendar.YEAR));
        assertEquals(0, result.get(Calendar.MONTH));
        assertEquals(16, result.get(Calendar.DATE));
    }

    @Test
    public void testSetHMSMsTo0() throws Exception {
        Calendar c = Calendar.getInstance();
        Calendar result = (Calendar) c.clone();
        Method method = DailyBookService.class.getDeclaredMethod("setHMSMsTo0", Calendar.class);
        method.setAccessible(true);
        method.invoke(this.dailyBookService, c);
        assertEquals(c.get(Calendar.YEAR), result.get(Calendar.YEAR));
        assertEquals(c.get(Calendar.MONTH), result.get(Calendar.MONTH));
        assertEquals(c.get(Calendar.DATE), result.get(Calendar.DATE));
        assertEquals(c.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(c.get(Calendar.MINUTE), 0);
        assertEquals(c.get(Calendar.SECOND), 0);
        assertEquals(c.get(Calendar.MILLISECOND), 0);
    }

}