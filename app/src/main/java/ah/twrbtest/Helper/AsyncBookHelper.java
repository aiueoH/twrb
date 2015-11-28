package ah.twrbtest.Helper;

import android.os.AsyncTask;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.helpers.BookingHelper;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.OnBookedEvent;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

public class AsyncBookHelper extends AsyncTask<Long, Integer, Boolean> {
    private BookRecord bookRecord;
    private BookingInfo bookingInfo;

    public AsyncBookHelper(BookRecord bookRecord) {
        this.bookRecord = bookRecord;
        this.bookingInfo = new BookingInfo();
        AdaptHelper.to(this.bookRecord, this.bookingInfo);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        return BookingHelper.book(this.bookingInfo);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Realm.getDefaultInstance().beginTransaction();
        AdaptHelper.to(this.bookingInfo, this.bookRecord);
        Realm.getDefaultInstance().commitTransaction();
        System.out.println(result ? "訂位代碼:" + this.bookingInfo.CODE : "失敗");
        EventBus.getDefault().post(new OnBookedEvent(this.bookRecord.getId(), result));
    }
}
