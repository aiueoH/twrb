package com.dowob.twrb.features.tickets.book;

import android.support.annotation.Nullable;

import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.features.tickets.AdaptHelper;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.timetable.TrainInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

public class BookRecordFactory {
    public static BookRecord createBookRecord(BookInfo bookInfo, TrainInfo trainInfo) {
        BookRecord br = new BookRecord();
        br.setId(BookRecord.generateId());
        AdaptHelper.to(bookInfo, br);
        setTrainInfoIntoBookRecord(trainInfo, br);
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
            String code,
            TrainInfo trainInfo) {
        BookRecord br = new BookRecord();
        br.setId(BookRecord.generateId());
        br.setPersonId(personId);
        br.setGetInDate(getinDate.getTime());
        br.setFromStation(from);
        br.setToStation(to);
        br.setOrderQtu(qty);
        br.setTrainNo(no);
        br.setReturnTicket(returnTicket);
        br.setCode(code);

        setTrainInfoIntoBookRecord(trainInfo, br);

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

    private static void setTrainInfoIntoBookRecord(TrainInfo trainInfo, BookRecord br) {
        br.setDepartureDateTime(parseTime(trainInfo.departureTime));
        br.setArrivalDateTime(parseTime(trainInfo.arrivalTime));
        br.setFares(Integer.parseInt(trainInfo.fares));
        br.setTrainType(trainInfo.type);
    }

    @Nullable
    private static Date parseTime(String time) {
        try {
            return new SimpleDateFormat("HH:mm").parse(time);
        } catch (Exception e) {
            return null;
        }
    }
}
