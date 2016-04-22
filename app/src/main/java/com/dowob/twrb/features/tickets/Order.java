package com.dowob.twrb.features.tickets;

import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.utils.Util;
import java.util.Calendar;

public class Order {
    private final String from;
    private final String to;
    private final Calendar getInDate;
    private final String no;
    private final int qty;
    private final String personId;

    public Order(String from, String to, Calendar getInDate, String no, int qty, String personId) {
        this.from = from;
        this.to = to;
        this.getInDate = getInDate;
        this.no = no;
        this.qty = qty;
        this.personId = personId;
    }

    static Order createByBookRecordId(long bookRecordId) {
        BookRecord bookRecord = BookRecord.get(bookRecordId);
        return new Builder()
                .setFrom(bookRecord.getFromStation())
                .setTo(bookRecord.getToStation())
                .setGetInDate(Util.dateToCalendar(bookRecord.getGetInDate()))
                .setNo(bookRecord.getTrainNo())
                .setQty(bookRecord.getOrderQtu())
                .setPersonId(bookRecord.getPersonId())
                .createOrder();
    }

    public com.dowob.webviewbooker.Order getWebViewBookerOrder() {
        return new com.dowob.webviewbooker.Order.Builder()
                .setFrom(from)
                .setTo(to)
                .setGetInDate(getInDate)
                .setPersonId(personId)
                .setTrainNo(no)
                .setQty(qty)
                .createOrder();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Calendar getGetInDate() {
        return getInDate;
    }

    public String getNo() {
        return no;
    }

    public int getQty() {
        return qty;
    }

    public String getPersonId() {
        return personId;
    }

    static class Builder {
        private String from;
        private String to;
        private Calendar getInDate;
        private String no;
        private int qty;
        private String personId;

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder setGetInDate(Calendar getInDate) {
            this.getInDate = getInDate;
            return this;
        }

        public Builder setNo(String no) {
            this.no = no;
            return this;
        }

        public Builder setQty(int qty) {
            this.qty = qty;
            return this;
        }

        public Builder setPersonId(String personId) {
            this.personId = personId;
            return this;
        }

        public Order createOrder() {
            return new Order(from, to, getInDate, no, qty, personId);
        }
    }
}
