package ah.twrbtest.Events;

import com.twrb.core.book.BookResult;

public class OnBookedEvent {
    private long bookRecordId;
    private BookResult result;

    public OnBookedEvent(long bookRecordId, BookResult result) {
        this.bookRecordId = bookRecordId;
        this.result = result;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }

    public BookResult getBookResult() {
        return result;
    }
}