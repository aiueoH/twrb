package com.dowob.twrb.database;

import android.support.annotation.Nullable;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class TrainInfo extends RealmObject {
    @PrimaryKey
    private long id;
    @Required
    private Date departureDateTime;
    @Required
    private Date arrivalDateTime;
    @Required
    private String departureStation;
    @Required
    private String destination;
    @Required
    private String trainType;
    @Required
    private String way;
    @Required
    private String remarks;
    private int fares;
    private boolean everyday;
    private boolean handicapped;
    private boolean bike;
    private boolean breastfeeding;
    private boolean acrossNight;

    @Nullable
    public static TrainInfo get(long id) {
        return Realm.getDefaultInstance().where(TrainInfo.class).equalTo("id", id).findFirst();
    }

    public static long generateId() {
        long id = System.currentTimeMillis();
        while (get(id) != null)
            id += 1;
        return id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(Date departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public Date getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(Date arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getFares() {
        return fares;
    }

    public void setFares(int fares) {
        this.fares = fares;
    }

    public boolean isEveryday() {
        return everyday;
    }

    public void setEveryday(boolean everyday) {
        this.everyday = everyday;
    }

    public boolean isHandicapped() {
        return handicapped;
    }

    public void setHandicapped(boolean handicapped) {
        this.handicapped = handicapped;
    }

    public boolean isBike() {
        return bike;
    }

    public void setBike(boolean bike) {
        this.bike = bike;
    }

    public boolean isBreastfeeding() {
        return breastfeeding;
    }

    public void setBreastfeeding(boolean breastfeeding) {
        this.breastfeeding = breastfeeding;
    }

    public boolean isAcrossNight() {
        return acrossNight;
    }

    public void setAcrossNight(boolean acrossNight) {
        this.acrossNight = acrossNight;
    }
}
