package com.dowob.twrb.Helper;

import com.dowob.twrb.DBObject.AdaptHelper;
import com.dowob.twrb.DBObject.BookRecord;
import com.twrb.core.MyLogger;
import com.twrb.core.book.BookInfo;
import com.twrb.core.helpers.BookHelper;

import io.realm.Realm;

public class AsyncCancelHelper extends NotifiableAsyncTask<Long, Integer, Boolean> {
    private long bookRecordId;
    private BookInfo bookInfo = new BookInfo();

    public AsyncCancelHelper(BookRecord bookRecord) {
        this.bookRecordId = bookRecord.getId();
        AdaptHelper.to(BookRecord.get(this.bookRecordId), this.bookInfo);
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        boolean result;
        try {
            result = BookHelper.cancel(this.bookInfo);
            Realm.getDefaultInstance().refresh();
            BookRecord br = BookRecord.get(this.bookRecordId);
            Realm.getDefaultInstance().beginTransaction();
            if (br == null) {
                br = new BookRecord();
                br.setId(BookRecord.generateId());
                br = Realm.getDefaultInstance().copyToRealm(br);
            }
            if (result)
                this.bookInfo.code = "";
            AdaptHelper.to(this.bookInfo, br);
            br.setIsCancelled(true);
            Realm.getDefaultInstance().commitTransaction();
            MyLogger.i(result ? "已退訂" + this.bookInfo.code : "退訂失敗");
        } finally {
            Realm.getDefaultInstance().close();
        }
        return result;
    }
}
