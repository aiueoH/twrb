package com.dowob.twrb.DBObject;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateTest {

    @Test
    public void testDate() throws Exception {
        String d = "2000/10/01";
        Date date = new Date(Date.parse(d));
        assertEquals(2000 - 1900, date.getYear());
        assertEquals(10 - 1, date.getMonth());
        assertEquals(1, date.getDate());

        SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");
        assertEquals(d, f.format(date));
    }
}
