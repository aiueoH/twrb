package ah.twrbtest.Helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.twrb.core.booking.BookingInfo;
import com.twrb.core.helpers.BookingHelper;

import ah.twrbtest.DBObject.AdaptHelper;
import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.OnCancelledEvent;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

public class AsyncCancelHelper extends AsyncTask<Long, Integer, Boolean> {
    private ProgressDialog progressDialog;
    private Context context;
    private BookRecord bookRecord;
    private BookingInfo bookingInfo;

    public AsyncCancelHelper(Context context, BookRecord bookRecord) {
        this.context = context;
        this.bookRecord = bookRecord;
        this.bookingInfo = new BookingInfo();
        AdaptHelper.to(this.bookRecord, this.bookingInfo);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = ProgressDialog.show(this.context, "", "叮叮叮叮叮叮叮叮叮");
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        return BookingHelper.cancel(this.bookingInfo);
    }

    @Override
    protected void onPostExecute(Boolean resut) {
        super.onPostExecute(resut);
        Realm.getDefaultInstance().beginTransaction();
        this.bookRecord.setIsCancelled(true);
        Realm.getDefaultInstance().commitTransaction();
        if (resut)
            this.bookingInfo.CODE = "";
        this.progressDialog.dismiss();
        System.out.println(resut ? "已退訂" + this.bookingInfo.CODE : "退訂失敗");
        EventBus.getDefault().post(new OnCancelledEvent());
    }
}
