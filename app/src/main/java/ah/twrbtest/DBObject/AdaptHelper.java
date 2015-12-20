package ah.twrbtest.DBObject;

import com.twrb.core.booking.BookInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdaptHelper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    public static String dateToString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static void to(BookRecord bookRecord, BookInfo bookInfo) {
        bookInfo.PERSON_ID = bookRecord.getPersonId();
        bookInfo.GETIN_DATE = DATE_FORMAT.format(bookRecord.getGetInDate());
        bookInfo.FROM_STATION = bookRecord.getFromStation();
        bookInfo.TO_STATION = bookRecord.getToStation();
        bookInfo.ORDER_QTU_STR = bookRecord.getOrderQtuStr();
        bookInfo.TRAIN_NO = bookRecord.getTrainNo();
        bookInfo.RETURNTICKET = bookRecord.getReturnTicket();
        bookInfo.CODE = bookRecord.getCode();
    }

    public static void to(BookInfo bookInfo, BookRecord bookRecord) {
        bookRecord.setPersonId(bookInfo.PERSON_ID);
        bookRecord.setGetInDate(new Date(Date.parse(bookInfo.GETIN_DATE)));
        bookRecord.setFromStation(bookInfo.FROM_STATION);
        bookRecord.setToStation(bookInfo.TO_STATION);
        bookRecord.setOrderQtuStr(bookInfo.ORDER_QTU_STR);
        bookRecord.setTrainNo(bookInfo.TRAIN_NO);
        bookRecord.setReturnTicket(bookInfo.RETURNTICKET);
        bookRecord.setCode(bookInfo.CODE);
    }
}
