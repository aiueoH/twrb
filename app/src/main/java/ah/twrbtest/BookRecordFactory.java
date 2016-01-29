package ah.twrbtest;

import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;

public class BookRecordFactory {
    public static BookRecord createBookRecord(BookInfo bookInfo) {
        BookRecord br = new BookRecord();
        br.setId(BookRecord.generateId());
        AdaptHelper.to(bookInfo, br);
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().copyToRealm(br);
        Realm.getDefaultInstance().commitTransaction();
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
}
