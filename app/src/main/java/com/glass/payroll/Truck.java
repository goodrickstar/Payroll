package com.glass.payroll;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Instant;

@Entity(tableName = "truck_records")
public class Truck {
    @PrimaryKey
    String id = "";
    String userId = "";
    int odometer = 0;
    int startingOdometer = 0;

    long stamp = Instant.now().getEpochSecond();

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getOdometer() {
        return odometer;
    }

    public void setOdometer(int odometer) {
        this.odometer = odometer;
    }

    public int getStartingOdometer() {
        return startingOdometer;
    }

    public void setStartingOdometer(int startingOdometer) {
        this.startingOdometer = startingOdometer;
    }
}
