package com.dowob.twrb.AutoBook;

import com.dowob.twrb.features.tickets.book.autobook.DailyBookService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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