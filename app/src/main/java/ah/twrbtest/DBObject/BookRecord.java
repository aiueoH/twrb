package ah.twrbtest.DBObject;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BookRecord extends RealmObject {
    @PrimaryKey
    private long id;
    private String personId;
    private Date getInDate;
    private String fromStation;
    private String toStation;
    private String orderQtuStr;
    private String trainNo;
    private String returnTicket;

    private String code = "";

    private boolean isCancelled = false;

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

    public String getOrderQtuStr() {
        return orderQtuStr;
    }

    public void setOrderQtuStr(String orderQtuStr) {
        this.orderQtuStr = orderQtuStr;
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
}
