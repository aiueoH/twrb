package com.dowob.twrb.features.tickets.book;

import android.content.Context;
import android.support.annotation.NonNull;

import com.dowob.twrb.R;

import java.util.AbstractMap;
import java.util.List;

public class BookResult {
    public enum Status {
        OK,
        UNKNOWN,
        TIMEOUT,
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

    private Status status;
    private long bookRecordId;
    private String code;
    private String getInTime;
    private String postOfficePickupDate;
    private String convenienceStorePickupDateTime;
    private String internetPickupDateTime;

    public BookResult(AbstractMap.SimpleEntry<com.twrb.core.book.BookResult, List<String>> result) {
        this.status = bookResultToResult(result.getKey());
        if (isOK()) {
            List<String> infos = result.getValue();
            this.code = infos.get(0);
            this.getInTime = infos.get(1);
            this.postOfficePickupDate = infos.get(2);
            this.convenienceStorePickupDateTime = infos.get(3);
            this.internetPickupDateTime = infos.get(4);
        }
    }

    public Status getStatus() {
        return status;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }

    public void setBookRecordId(long bookRecordId) {
        this.bookRecordId = bookRecordId;
    }

    public String getCode() {
        return code;
    }

    public String getStatusMsg(Context context) {
        return getResultMsg(context, status);
    }

    public boolean isOK() {
        return status.equals(Status.OK);
    }

    @NonNull
    public static String getResultMsg(Context context, Status status) {
        switch (status) {
            case OK:
                return context.getString(R.string.book_suc);
            case NO_SEAT:
                return context.getString(R.string.book_no_seat);
            case NO_TICKET:
                return context.getString(R.string.book_no_ticket);
            case OUT_TIME:
                return context.getString(R.string.book_out_time);
            case NOT_YET_BOOK:
                return context.getString(R.string.book_not_yet);
            case OVER_QUOTA:
                return context.getString(R.string.book_over_quota);
            case FULL_UP:
                return context.getString(R.string.book_full_up);
            case IO_EXCEPTION:
                return context.getString(R.string.book_io_exception);
            case WRONG_RANDINPUT:
                return context.getString(R.string.book_wrong_randinput);
            default:
                return context.getString(R.string.book_unknown);
        }
    }

    public static Status bookResultToResult(com.twrb.core.book.BookResult bookResult) {
        if (bookResult.equals(com.twrb.core.book.BookResult.OK)) return Status.OK;
        if (bookResult.equals(com.twrb.core.book.BookResult.TIMEOUT)) return Status.TIMEOUT;
        if (bookResult.equals(com.twrb.core.book.BookResult.NO_SEAT)) return Status.NO_SEAT;
        if (bookResult.equals(com.twrb.core.book.BookResult.NO_TICKET)) return Status.NO_TICKET;
        if (bookResult.equals(com.twrb.core.book.BookResult.OUT_TIME)) return Status.OUT_TIME;
        if (bookResult.equals(com.twrb.core.book.BookResult.NOT_YET_BOOK)) return Status.NOT_YET_BOOK;
        if (bookResult.equals(com.twrb.core.book.BookResult.WRON_DATE_OR_CONTENT_FORMAT)) return Status.WRONG_DATE_OR_CONTENT_FORMAT;
        if (bookResult.equals(com.twrb.core.book.BookResult.WRONG_STATION)) return Status.WRONG_STATION;
        if (bookResult.equals(com.twrb.core.book.BookResult.WRONG_DATA)) return Status.WRONG_DATA;
        if (bookResult.equals(com.twrb.core.book.BookResult.WRONG_NO)) return Status.WRONG_NO;
        if (bookResult.equals(com.twrb.core.book.BookResult.OVER_QUOTA)) return Status.OVER_QUOTA;
        if (bookResult.equals(com.twrb.core.book.BookResult.FULL_UP)) return Status.FULL_UP;
        if (bookResult.equals(com.twrb.core.book.BookResult.IO_EXCEPTION)) return Status.IO_EXCEPTION;
        if (bookResult.equals(com.twrb.core.book.BookResult.CANNOT_GET_PRONOUNCE)) return Status.CANNOT_GET_PRONOUNCE;
        if (bookResult.equals(com.twrb.core.book.BookResult.WRONG_RANDINPUT)) return Status.WRONG_RANDINPUT;
        return Status.UNKNOWN;
    }
}
