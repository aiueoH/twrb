package com.dowob.twrb.helpers;

import com.dowob.twrb.features.tickets.AdaptHelper;
import com.dowob.twrb.database.BookRecord;
import com.twrb.core.book.BookInfo;
import com.twrb.core.book.BookResult;

public class AsyncBookHelper extends NotifiableAsyncTask<Long, Integer, BookResult> {
    private long bookRecordId;
    private BookInfo bookInfo = new BookInfo();

    public AsyncBookHelper(BookRecord bookRecord) {
        this.bookRecordId = bookRecord.getId();
        AdaptHelper.to(BookRecord.get(this.bookRecordId), this.bookInfo);
    }

    @Override
    protected BookResult doInBackground(Long... params) {
//        BookResult result = BookResult.UNKNOWN;
//        try {
//            result = BookHelper.book(this.bookInfo);
//            if (!result.equals(BookResult.OK)) {
//                MyLogger.i("訂票失敗");
//                return result;
//            }
//            Realm.getDefaultInstance().refresh();
//            BookRecord br = BookRecord.get(this.bookRecordId);
//            Realm.getDefaultInstance().beginTransaction();
//            if (br == null || !br.getCode().isEmpty()) {
//                br = new BookRecord();
//                br.setId(BookRecord.generateId());
//                br = Realm.getDefaultInstance().copyToRealm(br);
//            }
//            AdaptHelper.to(this.bookInfo, br);
//            Realm.getDefaultInstance().commitTransaction();
//            MyLogger.i("訂位代碼:" + this.bookInfo.code);
//        } finally {
//            Realm.getDefaultInstance().close();
//        }
//        return result;
        return null;
    }
}