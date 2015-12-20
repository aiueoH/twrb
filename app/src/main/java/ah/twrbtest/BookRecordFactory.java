package ah.twrbtest;

import com.twrb.core.booking.BookInfo;

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
        System.out.println("------------------------------------");
        System.out.println("------- New BookRecord Added -------");
        System.out.println("------------------------------------");
        System.out.println("Id:" + br.getId());
        System.out.println("GetInDate:" + br.getGetInDate());
        System.out.println("PersonId:" + br.getPersonId());
        System.out.println("FromStation:" + br.getFromStation());
        System.out.println("ToStation:" + br.getToStation());
        return br;
    }
}
