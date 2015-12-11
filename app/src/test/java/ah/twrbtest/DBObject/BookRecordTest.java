package ah.twrbtest.DBObject;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Created by Wei on 2015/12/9.
 */
public class BookRecordTest {

    @Test
    public void testIsBookable() throws Exception {
        Calendar getInDate, today;
        BookRecord br;
        getInDate = Calendar.getInstance();
        today = Calendar.getInstance();
        br = new BookRecord();

        getInDate.set(2015, 1, 20);
        br.setGetInDate(getInDate.getTime());

        today.set(2015, 1, 1, 0, 0);
        assertFalse(BookRecord.isBookable(br, today));

        today.set(2015, 1, 5, 0, 0);
        assertFalse(BookRecord.isBookable(br, today));

        today.set(2015, 1, 5, BookRecord.MID_NIGHT_H, BookRecord.MID_NIGHT_M - 1);
        assertFalse(BookRecord.isBookable(br, today));

        today.set(2015, 1, 5, BookRecord.MID_NIGHT_H, BookRecord.MID_NIGHT_M);
        assertTrue(BookRecord.isBookable(br, today));

        today.set(2015, 1, 6, 0, 0);
        assertTrue(BookRecord.isBookable(br, today));

        today.set(2015, 1, 7, 0, 0);
        assertTrue(BookRecord.isBookable(br, today));

        // special dates ---------------------------------------------------------------------------
        getInDate.set(2015, 8, 25);
        today.set(2015, 8, 10, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertFalse(BookRecord.isBookable(br, today));

        getInDate.set(2015, 8, 25);
        today.set(2015, 8, 11, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2015, 8, 26);
        today.set(2015, 8, 11, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2015, 8, 26);
        today.set(2015, 8, 10, BookRecord.MID_NIGHT_H, BookRecord.MID_NIGHT_M - 1);
        br.setGetInDate(getInDate.getTime());
        assertFalse(BookRecord.isBookable(br, today));

        getInDate.set(2015, 8, 26);
        today.set(2015, 8, 10, BookRecord.MID_NIGHT_H, BookRecord.MID_NIGHT_M);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2015, 8, 27);
        today.set(2015, 8, 11, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));
    }
}