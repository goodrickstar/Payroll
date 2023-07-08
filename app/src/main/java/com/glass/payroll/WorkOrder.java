package com.glass.payroll;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;
@Entity(tableName = "work_order_records")
public class WorkOrder {

    @PrimaryKey
    @NonNull
    String orderId = "";

    String truckId = "";
    String userId = "";
    String label = "";
    int reading = 0;

    int interval = 0;

    long stamp = Instant.now().getEpochSecond();
    long created = Instant.now().getEpochSecond();

    @Ignore
    public WorkOrder(String userId, String truckId, int reading, String label, int interval) {
        this.truckId = truckId;
        this.userId = userId;
        this.label = label;
        this.reading = reading;
        this.interval = interval;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getReading() {
        return reading;
    }

    public void setReading(int reading) {
        this.reading = reading;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
