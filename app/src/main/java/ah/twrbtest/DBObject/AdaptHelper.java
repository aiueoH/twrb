package ah.twrbtest.DBObject;

import com.twrb.core.booking.BookingInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdaptHelper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    public static String dateToString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static void to(BookRecord bookRecord, BookingInfo bookingInfo) {
        bookingInfo.PERSON_ID = bookRecord.getPersonId();
        bookingInfo.GETIN_DATE = DATE_FORMAT.format(bookRecord.getGetInDate());
        bookingInfo.FROM_STATION = bookRecord.getFromStation();
        bookingInfo.TO_STATION = bookRecord.getToStation();
        bookingInfo.ORDER_QTU_STR = bookRecord.getOrderQtuStr();
        bookingInfo.TRAIN_NO = bookRecord.getTrainNo();
        bookingInfo.RETURNTICKET = bookRecord.getReturnTicket();
        bookingInfo.CODE = bookRecord.getCode();
    }

    public static void to(BookingInfo bookingInfo, BookRecord bookRecord) {
        bookRecord.setPersonId(bookingInfo.PERSON_ID);
        bookRecord.setGetInDate(new Date(Date.parse(bookingInfo.GETIN_DATE)));
        bookRecord.setFromStation(bookingInfo.FROM_STATION);
        bookRecord.setToStation(bookingInfo.TO_STATION);
        bookRecord.setOrderQtuStr(bookingInfo.ORDER_QTU_STR);
        bookRecord.setTrainNo(bookingInfo.TRAIN_NO);
        bookRecord.setReturnTicket(bookingInfo.RETURNTICKET);
        bookRecord.setCode(bookingInfo.CODE);
    }
}
