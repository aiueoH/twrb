package com.dowob.twrb.Events;

public class OnCancelledEvent {
    private boolean isSuccess;
    private long bookRecordId;

    public OnCancelledEvent(long bookRecordId, boolean isSuccess) {
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
