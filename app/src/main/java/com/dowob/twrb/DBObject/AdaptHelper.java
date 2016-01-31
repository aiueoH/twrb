package com.dowob.twrb.DBObject;

import com.twrb.core.book.BookInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdaptHelper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    public static String dateToString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static void to(BookRecord bookRecord, BookInfo bookInfo) {
        bookInfo.personId = bookRecord.getPersonId();
        bookInfo.getinDate = DATE_FORMAT.format(bookRecord.getGetInDate());
        bookInfo.fromStation = bookRecord.getFromStation();
        bookInfo.toStation = bookRecord.getToStation();
        bookInfo.orderQtuStr = bookRecord.getOrderQtuStr();
        bookInfo.trainNo = bookRecord.getTrainNo();
        bookInfo.returnTicket = bookRecord.getReturnTicket();
        bookInfo.code = bookRecord.getCode();
    }

    public static void to(BookInfo bookInfo, BookRecord bookRecord) {
        bookRecord.setPersonId(bookInfo.personId);
        bookRecord.setGetInDate(new Date(Date.parse(bookInfo.getinDate)));
        bookRecord.setFromStation(bookInfo.fromStation);
        bookRecord.setToStation(bookInfo.toStation);
        bookRecord.setOrderQtuStr(bookInfo.orderQtuStr);
        bookRecord.setTrainNo(bookInfo.trainNo);
        bookRecord.setReturnTicket(bookInfo.returnTicket);
        bookRecord.setCode(bookInfo.code);
    }
}
