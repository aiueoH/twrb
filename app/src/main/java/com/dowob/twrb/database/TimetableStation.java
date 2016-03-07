package com.dowob.twrb.database;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TimetableStation extends RealmObject {
    @PrimaryKey
    private String no;
    private String cityNo;
    private String bookNo;
    private String nameCh;
    private String nameEn;
    private boolean isBookable;

    @Nullable
    public static TimetableStation get(String no) {
        return Realm.getDefaultInstance().where(TimetableStation.class).equalTo("no", no).findFirst();
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getCityNo() {
        return cityNo;
    }

    public void setCityNo(String cityNo) {
        this.cityNo = cityNo;
    }

    public String getBookNo() {
        return bookNo;
    }

    public void setBookNo(String bookNo) {
        this.bookNo = bookNo;
    }

    public String getNameCh() {
        return nameCh;
    }

    public void setNameCh(String nameCh) {
        this.nameCh = nameCh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public boolean isBookable() {
        return isBookable;
    }

    public void setIsBookable(boolean isBookable) {
        this.isBookable = isBookable;
    }
}
