package com.dowob.twrb.events;

import com.dowob.twrb.features.tickets.book.BookResult;

public class OnBookedEvent {
    private long bookRecordId;
    private BookResult.Status status;

    public OnBookedEvent(long bookRecordId, BookResult.Status status) {
        this.bookRecordId = bookRecordId;
        this.status = status;
    }

    public long getBookRecordId() {
        return bookRecordId;
    }

    public BookResult.Status getBookResult() {
        return status;
    }
}