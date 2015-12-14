package ah.twrbtest.DBObject;

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
                {"2015/09/25", "2015/09/11"},
                {"2015/09/26", "2015/09/11"},
                {"2015/09/27", "2015/09/11"},
                {"2015/09/28", "2015/09/11"},
                {"2015/09/29", "2015/09/11"},
                {"2015/10/08", "2015/09/24"},
                {"2015/10/09", "2015/09/24"},
                {"2015/10/10", "2015/09/24"},
                {"2015/10/11", "2015/09/24"},
                {"2015/10/12", "2015/09/24"},
                {"2015/12/31", "2015/12/17"},
                {"2016/01/01", "2015/12/17"},
                {"2016/01/02", "2015/12/17"},
                {"2016/01/03", "2015/12/17"},
                {"2016/01/04", "2015/12/17"},
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
    private String orderQtuStr;
    private String trainNo;
    private String returnTicket;

    private String code = "";

    private boolean isCancelled = false;

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

    public static boolean isBookable(BookRecord bookRecord, Calendar now) {
        Calendar today = (Calendar) now.clone();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar getInDate = Calendar.getInstance();
        getInDate.setTime(bookRecord.getGetInDate());
        getInDate.set(Calendar.HOUR_OF_DAY, 0);
        getInDate.set(Calendar.MINUTE, 0);
        getInDate.set(Calendar.SECOND, 0);
        getInDate.set(Calendar.MILLISECOND, 0);
        Calendar bookableDate = (Calendar) getInDate.clone();
        if (SPECIAL_DATE.containsKey(getInDate)) {
            bookableDate = SPECIAL_DATE.get(getInDate);
        } else
            bookableDate.add(Calendar.DATE, -14);
        if (now.get(Calendar.HOUR_OF_DAY) >= MID_NIGHT_H && now.get(Calendar.MINUTE) >= MID_NIGHT_M)
            bookableDate.add(Calendar.DATE, -1);
        return (today.before(getInDate) || today.equals(getInDate)) &&
                (today.after(bookableDate) || today.equals(bookableDate));
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
