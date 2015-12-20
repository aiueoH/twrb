package ah.twrbtest.DBObject;

import com.twrb.core.booking.BookInfo;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class AdaptHelperTest {

    @Test
    public void testTo() throws Exception {
        String date = "2010/01/10";
        BookRecord r = new BookRecord();
        r.setId(BookRecord.generateId());
        BookInfo i = new BookInfo();
        r.setGetInDate(new Date(Date.parse(date)));
        AdaptHelper.to(r, i);
        assertEquals(date, i.GETIN_DATE);
    }

    @Test
    public void testTo1() throws Exception {
        String date = "2010/01/10";
        BookInfo i = new BookInfo();
        BookRecord r = new BookRecord();
        r.setId(BookRecord.generateId());
        i.GETIN_DATE = date;
        AdaptHelper.to(i, r);
        assertEquals(date, AdaptHelper.dateToString(r.getGetInDate()));
        assertEquals(2010 - 1900, r.getGetInDate().getYear());
        assertEquals(1 - 1, r.getGetInDate().getMonth());
        assertEquals(10, r.getGetInDate().getDate());
    }
}