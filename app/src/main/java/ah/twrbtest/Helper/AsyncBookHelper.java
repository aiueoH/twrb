package ah.twrbtest.Helper;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.helpers.BookingHelper;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import io.realm.Realm;

public class AsyncBookHelper extends NotifiableAsyncTask<Long, Integer, Boolean> {
    private long bookRecordId;
    private BookingInfo bookingInfo = new BookingInfo();

    public AsyncBookHelper(BookRecord bookRecord) {
        this.bookRecordId = bookRecord.getId();
        AdaptHelper.to(BookRecord.get(this.bookRecordId), this.bookingInfo);
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        boolean result = false;
        try {
            result = BookingHelper.book(this.bookingInfo);
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
            AdaptHelper.to(this.bookingInfo, br);
            Realm.getDefaultInstance().commitTransaction();
            System.out.println("訂位代碼:" + this.bookingInfo.CODE);
        } finally {
            Realm.getDefaultInstance().close();
        }
        return result;
    }
}