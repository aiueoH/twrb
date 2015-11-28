package ah.twrbtest;

public class OnBookedEvent {
    private long bookRecordId;
    private boolean isSuccess;

    public OnBookedEvent(long bookRecordId, boolean isSuccess) {
        this.bookRecordId = bookRecordId;
        this.isSuccess = isSuccess;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
