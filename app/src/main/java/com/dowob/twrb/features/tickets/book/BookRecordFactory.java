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
        br.setTrainInfo(createTrainInfo(trainInfo));
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
        br.setTrainInfo(createTrainInfo(trainInfo));
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

    private static com.dowob.twrb.database.TrainInfo createTrainInfo(TrainInfo trainInfo) {
        Realm.getDefaultInstance().beginTransaction();
        com.dowob.twrb.database.TrainInfo t = Realm.getDefaultInstance().createObject(com.dowob.twrb.database.TrainInfo.class);
        t.setId(com.dowob.twrb.database.TrainInfo.generateId());
        t.setDepartureDateTime(parseTime(trainInfo.departureTime));
        t.setArrivalDateTime(parseTime(trainInfo.arrivalTime));
        t.setFares(Integer.parseInt(trainInfo.fares));
        t.setTrainType(trainInfo.type);
        t.setAcrossNight(trainInfo.acrossNight);
        t.setBike(trainInfo.bike);
        t.setBreastfeeding(trainInfo.breastfeeding);
        t.setEveryday(trainInfo.everyday);
        t.setHandicapped(trainInfo.handicapped);
        t.setDepartureStation(trainInfo.departureStation);
        t.setDestination(trainInfo.destination);
        t.setRemarks(trainInfo.remarks);
        t.setWay(trainInfo.way);
        Realm.getDefaultInstance().commitTransaction();
        return t;
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
