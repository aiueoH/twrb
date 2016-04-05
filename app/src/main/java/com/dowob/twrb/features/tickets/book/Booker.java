package com.dowob.twrb.features.tickets.book;

import com.twrb.core.NonAutoBooker;
import com.twrb.core.URLConnectionBooker;
import com.twrb.core.book.BookResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.List;

public class Booker {
    private NonAutoBooker mNonAutoBooker;

    /**
     * @param from
     * @param to
     * @param date
     * @param no
     * @param qty
     * @param personId
     * @return BookResult, {code, getByNo in time, station and post office pickup date, convenience store pickup time, internet puckup time}
     * @throws IOException
     */
    public AbstractMap.SimpleEntry<Result, List<String>> book(String from, String to, Calendar date, String no, int qty, String personId) {
        com.twrb.core.Booker booker = new URLConnectionBooker();
        try {
            AbstractMap.SimpleEntry<BookResult, List<String>> result = booker.book(from, to, date, no, qty, personId);
            return new AbstractMap.SimpleEntry<>(bookResultToResult(result.getKey()), result.getValue());
        } catch (IOException e) {
            return new AbstractMap.SimpleEntry<>(Result.IO_EXCEPTION, null);
        }
    }

    public ByteArrayOutputStream step1(String from, String to, Calendar date, String no, int qty, String personId) {
        mNonAutoBooker = new NonAutoBooker();
        try {
            return mNonAutoBooker.step1(from, to, date, no, qty, personId);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AbstractMap.SimpleEntry<Result, List<String>> step2(String randInput) {
        try {
            AbstractMap.SimpleEntry<BookResult, List<String>> result = mNonAutoBooker.step2(randInput);
            return new AbstractMap.SimpleEntry<>(bookResultToResult(result.getKey()), result.getValue());
        } catch (IOException e) {
            e.printStackTrace();
            return new AbstractMap.SimpleEntry<>(Result.IO_EXCEPTION, null);
        }
    }

    public static Result bookResultToResult(BookResult bookResult) {
        if (bookResult.equals(BookResult.OK)) return Result.OK;
        if (bookResult.equals(BookResult.NO_SEAT)) return Result.NO_SEAT;
        if (bookResult.equals(BookResult.NO_TICKET)) return Result.NO_TICKET;
        if (bookResult.equals(BookResult.OUT_TIME)) return Result.OUT_TIME;
        if (bookResult.equals(BookResult.NOT_YET_BOOK)) return Result.NOT_YET_BOOK;
        if (bookResult.equals(BookResult.WRON_DATE_OR_CONTENT_FORMAT))
            return Result.WRONG_DATE_OR_CONTENT_FORMAT;
        if (bookResult.equals(BookResult.WRONG_STATION)) return Result.WRONG_STATION;
        if (bookResult.equals(BookResult.WRONG_DATA)) return Result.WRONG_DATA;
        if (bookResult.equals(BookResult.WRONG_NO)) return Result.WRONG_NO;
        if (bookResult.equals(BookResult.OVER_QUOTA)) return Result.OVER_QUOTA;
        if (bookResult.equals(BookResult.FULL_UP)) return Result.FULL_UP;
        if (bookResult.equals(BookResult.IO_EXCEPTION)) return Result.IO_EXCEPTION;
        if (bookResult.equals(BookResult.CANNOT_GET_PRONOUNCE)) return Result.CANNOT_GET_PRONOUNCE;
        if (bookResult.equals(BookResult.WRONG_RANDINPUT)) return Result.WRONG_RANDINPUT;
        return Result.UNKNOWN;
    }

    public enum Result {
        OK,
        UNKNOWN,
        NO_SEAT,
        NO_TICKET,
        OUT_TIME,
        NOT_YET_BOOK,
        WRONG_DATE_OR_CONTENT_FORMAT,
        WRONG_STATION,
        WRONG_DATA,
        WRONG_NO,
        OVER_QUOTA,
        FULL_UP,
        IO_EXCEPTION,
        CANNOT_GET_PRONOUNCE,
        WRONG_RANDINPUT,
    }
}