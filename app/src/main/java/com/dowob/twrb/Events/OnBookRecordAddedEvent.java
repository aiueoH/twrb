package com.dowob.twrb.events;

public class OnBookRecordAddedEvent {
    private long bookRecordId;

    public OnBookRecordAddedEvent(long bookRecordId) {
        this.bookRecordId = bookRecordId;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }
}
