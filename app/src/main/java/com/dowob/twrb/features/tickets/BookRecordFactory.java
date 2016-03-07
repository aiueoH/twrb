package com.dowob.twrb.features.tickets;

import com.dowob.twrb.DBObject.AdaptHelper;
import com.dowob.twrb.DBObject.BookRecord;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;

import java.util.Calendar;

import io.realm.Realm;

public class BookRecordFactory {
    public static BookRecord createBookRecord(BookInfo bookInfo) {
        BookRecord br = new BookRecord();
        br.setId(BookRecord.generateId());
        AdaptHelper.to(bookInfo, br);
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().copyToRealm(br);
        Realm.getDefaultInstance().commitTransaction();
        Realm.getDefaultInstance().close();
        MyLogger.i("------------------------------------");
        MyLogger.i("------- New BookRecord Added -------");
        MyLogger.i("------------------------------------");
        MyLogger.i("Id:" + br.getId());
        MyLogger.i("GetInDate:" + br.getGetInDate());
        MyLogger.i("PersonId:" + br.getPersonId());
        MyLogger.i("FromStation:" + br.getFromStation());
        MyLogger.i("ToStation:" + br.getToStation());
        return br;
    }

    public static BookRecord createBookRecord(
            String personId,
            Calendar getinDate,
            String from,
            String to,
            int qty,
            String no,
            String returnTicket,
            String code) {
        BookRecord br = new BookRecord();
        br.setId(BookRecord.generateId());
        br.setPersonId(personId);
        br.setGetInDate(getinDate.getTime());
        br.setFromStation(from);
        br.setToStation(to);
        br.setOrderQtuStr(Integer.toString(qty));
        br.setTrainNo(no);
        br.setReturnTicket(returnTicket);
        br.setCode(code);
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().copyToRealm(br);
        Realm.getDefaultInstance().commitTransaction();
        Realm.getDefaultInstance().close();
        MyLogger.i("------------------------------------");
        MyLogger.i("------- New BookRecord Added -------");
        MyLogger.i("------------------------------------");
        MyLogger.i("Id:" + br.getId());
        MyLogger.i("GetInDate:" + br.getGetInDate());
        MyLogger.i("PersonId:" + br.getPersonId());
        MyLogger.i("FromStation:" + br.getFromStation());
        MyLogger.i("ToStation:" + br.getToStation());
        return br;
    }
}
