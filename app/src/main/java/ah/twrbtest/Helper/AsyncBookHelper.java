package ah.twrbtest.Helper;

import com.twrb.core.booking.BookInfo;
import com.twrb.core.helpers.BookingHelper;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;

public class AsyncBookHelper extends NotifiableAsyncTask<Long, Integer, Boolean> {
    private long bookRecordId;
    private BookInfo bookInfo = new BookInfo();

    public AsyncBookHelper(BookRecord bookRecord) {
        this.bookRecordId = bookRecord.getId();
        AdaptHelper.to(BookRecord.get(this.bookRecordId), this.bookInfo);
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        boolean result = false;
        try {
            result = BookingHelper.book(this.bookInfo);
            if (!result) {
                System.out.println("訂票失敗");
                return result;
            }
            Realm.getDefaultInstance().refresh();
            BookRecord br = BookRecord.get(this.bookRecordId);
            Realm.getDefaultInstance().beginTransaction();
            if (br == null || !br.getCode().isEmpty()) {
                br = new BookRecord();
                br.setId(BookRecord.generateId());
                br = Realm.getDefaultInstance().copyToRealm(br);
            }
            AdaptHelper.to(this.bookInfo, br);
            Realm.getDefaultInstance().commitTransaction();
            System.out.println("訂位代碼:" + this.bookInfo.CODE);
        } finally {
            Realm.getDefaultInstance().close();
        }
        return result;
    }
}