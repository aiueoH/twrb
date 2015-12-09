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
}