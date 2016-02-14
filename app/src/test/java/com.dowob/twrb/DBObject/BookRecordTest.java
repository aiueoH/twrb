package com.dowob.twrb.DBObject;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

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
        // saturday and sunday --------------------------------------------------------------------
        today.set(2016, 2 - 1, 19, 0, 0);

        getInDate.set(2016, 3 - 1, 4);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2016, 3 - 1, 5);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2016, 3 - 1, 6);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2016, 3 - 1, 7);
        br.setGetInDate(getInDate.getTime());
        assertFalse(BookRecord.isBookable(br, today));

        // special dates ---------------------------------------------------------------------------
        getInDate.set(2016, 6 - 1, 9);
        today.set(2016, 5 - 1, 24, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertFalse(BookRecord.isBookable(br, today));

        getInDate.set(2016, 6 - 1, 9);
        today.set(2016, 5 - 1, 25, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2016, 6 - 1, 10);
        today.set(2016, 5 - 1, 25, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2016, 6 - 1, 9);
        today.set(2016, 5 - 1, 24, BookRecord.MID_NIGHT_H, BookRecord.MID_NIGHT_M - 1);
        br.setGetInDate(getInDate.getTime());
        assertFalse(BookRecord.isBookable(br, today));

        getInDate.set(2016, 6 - 1, 9);
        today.set(2016, 5 - 1, 24, BookRecord.MID_NIGHT_H, BookRecord.MID_NIGHT_M);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));

        getInDate.set(2016, 6 - 1, 13);
        today.set(2016, 5 - 1, 25, 0, 0);
        br.setGetInDate(getInDate.getTime());
        assertTrue(BookRecord.isBookable(br, today));
    }
}