package com.dowob.twrb.database;

import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BookRecord extends RealmObject {
    // 每日訂票時間
    public static final int MID_NIGHT_H = 23;
    public static final int MID_NIGHT_M = 59;
    // 特殊節日提早訂票日期
    private static final HashMap<Calendar, Calendar> SPECIAL_DATE;

    static {
        String[][] date_pairs = {
                {"2016/02/27", "2016/02/12"},
                {"2016/02/28", "2016/02/12"},
                {"2016/02/29", "2016/02/12"},
                {"2016/03/01", "2016/02/12"},

                {"2016/04/02", "2016/03/18"},
                {"2016/04/03", "2016/03/18"},
                {"2016/04/04", "2016/03/18"},
                {"2016/04/05", "2016/03/18"},
                {"2016/04/06", "2016/03/18"},

                {"2016/05/07", "2016/04/22"},
                {"2016/05/08", "2016/04/22"},
                {"2016/05/09", "2016/04/22"},

                {"2016/06/09", "2016/05/25"},
                {"2016/06/10", "2016/05/25"},
                {"2016/06/11", "2016/05/25"},
                {"2016/06/12", "2016/05/25"},
                {"2016/06/13", "2016/05/25"},

                {"2016/09/15", "2016/08/31"},
                {"2016/09/16", "2016/08/31"},
                {"2016/09/17", "2016/08/31"},
                {"2016/09/18", "2016/08/31"},
                {"2016/09/19", "2016/08/31"},

                {"2016/10/08", "2016/09/23"},
                {"2016/10/09", "2016/09/23"},
                {"2016/10/10", "2016/09/23"},
                {"2016/10/11", "2016/09/23"},

                {"2016/12/31", "2016/12/16"},
                {"2017/01/01", "2016/12/16"},
                {"2017/01/02", "2016/12/16"},
                {"2017/01/03", "2016/12/16"},
        };
        SPECIAL_DATE = new HashMap<>();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        for (String[] date_pair : date_pairs) {
            Calendar c0 = Calendar.getInstance();
            Calendar c1 = Calendar.getInstance();
            try {
                c0.setTime(df.parse(date_pair[0]));
                c0.set(Calendar.HOUR_OF_DAY, 0);
                c0.set(Calendar.MINUTE, 0);
                c0.set(Calendar.SECOND, 0);
                c0.set(Calendar.MILLISECOND, 0);
                c1.setTime(df.parse(date_pair[1]));
                c1.set(Calendar.HOUR_OF_DAY, 0);
                c1.set(Calendar.MINUTE, 0);
                c1.set(Calendar.SECOND, 0);
                c1.set(Calendar.MILLISECOND, 0);
                SPECIAL_DATE.put(c0, c1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @PrimaryKey
    private long id;
    private String personId;
    private Date getInDate;
    private String fromStation;
    private String toStation;
    private int orderQtu;
    private String trainNo;
    private String returnTicket;
    private String code = "";
    private boolean isCancelled = false;
    private TrainInfo trainInfo;

    @Nullable
    public static BookRecord get(long id) {
        return Realm.getDefaultInstance().where(BookRecord.class).equalTo("id", id).findFirst();
    }

    public static long generateId() {
        long id = System.currentTimeMillis();
        while (get(id) != null)
            id += 1;
        return id;
    }

    public static boolean isBuyable(Calendar departure, Calendar now) {
        return departure.getTimeInMillis() - now.getTimeInMillis() >= 30 * 60 * 1000;
    }

    public static boolean isBookable(Calendar departure, Calendar now) {
        Calendar before1Hour = (Calendar) departure.clone();
        before1Hour.add(Calendar.HOUR_OF_DAY, -1);
        if (!now.before(before1Hour))
            return false;
        Calendar today = (Calendar) now.clone();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar getInDate = (Calendar) departure.clone();
        getInDate.set(Calendar.HOUR_OF_DAY, 0);
        getInDate.set(Calendar.MINUTE, 0);
        getInDate.set(Calendar.SECOND, 0);
        getInDate.set(Calendar.MILLISECOND, 0);
        Calendar bookableDate = (Calendar) getInDate.clone();
        if (SPECIAL_DATE.containsKey(getInDate)) {
            bookableDate = SPECIAL_DATE.get(getInDate);
        } else {
            if (today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                bookableDate.add(Calendar.DATE, -16);
            else
                bookableDate.add(Calendar.DATE, -14);
        }
        if (now.get(Calendar.HOUR_OF_DAY) >= MID_NIGHT_H && now.get(Calendar.MINUTE) >= MID_NIGHT_M)
            bookableDate.add(Calendar.DATE, -1);
        return (today.before(getInDate) || today.equals(getInDate)) &&
            (today.after(bookableDate) || today.equals(bookableDate));
    }

    public static boolean isBookable(BookRecord bookRecord, Calendar now) {
        Calendar getInDate = Calendar.getInstance();
        getInDate.setTime(bookRecord.getGetInDate());
        getInDate.set(Calendar.HOUR_OF_DAY, 23);
        getInDate.set(Calendar.MINUTE, 59);
        getInDate.set(Calendar.SECOND, 59);
        getInDate.set(Calendar.MILLISECOND, 999);
        return isBookable(getInDate, now);
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public Date getGetInDate() {
        return getInDate;
    }

    public void setGetInDate(Date getInDate) {
        this.getInDate = getInDate;
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public int getOrderQtu() {
        return orderQtu;
    }

    public void setOrderQtu(int orderQtu) {
        this.orderQtu = orderQtu;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getReturnTicket() {
        return returnTicket;
    }

    public void setReturnTicket(String returnTicket) {
        this.returnTicket = returnTicket;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TrainInfo getTrainInfo() {
        return trainInfo;
    }

    public void setTrainInfo(TrainInfo trainInfo) {
        this.trainInfo = trainInfo;
    }
}
