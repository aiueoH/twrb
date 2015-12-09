package ah.twrbtest.DBObject;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BookRecord extends RealmObject {
    // 每日訂票時間
    public static final int MID_NIGHT_H = 23;
    public static final int MID_NIGHT_M = 59;
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

    public BookRecord() {
        this.id = generateId();
    }

    public static BookRecord get(long id) {
        return Realm.getDefaultInstance().where(BookRecord.class).equalTo("id", id).findFirst();
    }

    public static long generateId() {
        return System.currentTimeMillis() + new Object().hashCode();
    }

    public static boolean isBookable(BookRecord bookRecord, Calendar now) {
        Calendar today = (Calendar) now.clone();
        if (now.get(Calendar.HOUR_OF_DAY) >= MID_NIGHT_H && now.get(Calendar.MINUTE) >= MID_NIGHT_M)
            today.add(Calendar.DATE, 1);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar bookableDate = Calendar.getInstance();
        bookableDate.setTime(bookRecord.getGetInDate());
        bookableDate.set(Calendar.HOUR_OF_DAY, 0);
        bookableDate.set(Calendar.MINUTE, 0);
        bookableDate.set(Calendar.SECOND, 0);
        bookableDate.set(Calendar.MILLISECOND, 0);
        bookableDate.add(Calendar.DATE, -14);
        return today.after(bookableDate) || today.equals(bookableDate);
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
