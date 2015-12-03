package ah.twrbtest.Events;

public class OnCancelledEvent {
    private boolean isSuccess;
    private long bookRecordId;

    public OnCancelledEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
