package com.dowob.twrb.events;

import com.dowob.twrb.features.tickets.book.Booker;

public class OnBookedEvent {
    private long bookRecordId;
    private Booker.Result result;

    public OnBookedEvent(long bookRecordId, Booker.Result result) {
        this.bookRecordId = bookRecordId;
        this.result = result;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }

    public Booker.Result getBookResult() {
        return result;
    }
}