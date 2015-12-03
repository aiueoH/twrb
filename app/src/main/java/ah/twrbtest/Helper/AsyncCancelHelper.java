package ah.twrbtest.Helper;

import android.os.AsyncTask;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.helpers.BookingHelper;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Events.OnCancelledEvent;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

public class AsyncCancelHelper extends AsyncTask<Long, Integer, Boolean> {
    private long bookRecordId;
    private BookingInfo bookingInfo;

    public AsyncCancelHelper(BookRecord bookRecord) {
        this.bookRecordId = bookRecord.getId();
        this.bookingInfo = new BookingInfo();
        AdaptHelper.to(bookRecord, this.bookingInfo);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        return BookingHelper.cancel(this.bookingInfo);
    }

    @Override
    protected void onPostExecute(Boolean resut) {
        super.onPostExecute(resut);
        if (resut)
            this.bookingInfo.CODE = "";
        BookRecord bookRecord = Realm.getDefaultInstance().where(BookRecord.class).equalTo("id", this.bookRecordId).findFirst();
        if (bookRecord == null) {
            bookRecord = Realm.getDefaultInstance().createObject(BookRecord.class);
            bookRecord.setId(this.bookRecordId);
        }
        Realm.getDefaultInstance().beginTransaction();
        AdaptHelper.to(this.bookingInfo, bookRecord);
        bookRecord.setIsCancelled(true);
        Realm.getDefaultInstance().commitTransaction();
        System.out.println(resut ? "已退訂" + this.bookingInfo.CODE : "退訂失敗");
        EventBus.getDefault().post(new OnCancelledEvent(resut));
    }
}
