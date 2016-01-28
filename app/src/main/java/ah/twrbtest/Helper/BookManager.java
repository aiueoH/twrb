package ah.twrbtest.Helper;

import com.twrb.core.book.BookInfo;
import com.twrb.core.book.BookResult;
import com.twrb.core.helpers.BookHelper;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;

public class BookManager {
    public static BookResult book(long bookRecordId) {
        BookResult result = BookResult.UNKNOWN;
        try {
            BookInfo bookInfo = new BookInfo();
            Realm.getDefaultInstance().refresh();
            BookRecord bookRecord = BookRecord.get(bookRecordId);
            AdaptHelper.to(bookRecord, bookInfo);
            result = BookHelper.book(bookInfo);
            if (!result.equals(BookResult.OK)) {
                System.out.println("訂票失敗");
                return result;
            }
            Realm.getDefaultInstance().beginTransaction();
            AdaptHelper.to(bookInfo, bookRecord);
            Realm.getDefaultInstance().commitTransaction();
            System.out.println("訂位代碼:" + bookInfo.code);
        } finally {
            Realm.getDefaultInstance().close();
        }
        return result;
    }

    public static Boolean cancel(long bookRecordId) {
        boolean result = false;
        try {
            BookInfo bookInfo = new BookInfo();
            Realm.getDefaultInstance().refresh();
            BookRecord bookRecord = BookRecord.get(bookRecordId);
            AdaptHelper.to(bookRecord, bookInfo);
            result = BookHelper.cancel(bookInfo);
            if (result) {
                bookInfo.code = "";
                Realm.getDefaultInstance().beginTransaction();
                AdaptHelper.to(bookInfo, bookRecord);
                bookRecord.setIsCancelled(true);
                Realm.getDefaultInstance().commitTransaction();
                System.out.println(result ? "已退訂" : "退訂失敗");
            }
        } finally {
            Realm.getDefaultInstance().close();
        }
        return result;
    }
}
