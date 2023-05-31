package com.glass.payroll;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;

@Entity(tableName = "truck_records")
public class Truck {

    public Truck() {
    }

    @PrimaryKey
            @NonNull
    String id = "";
    String userId = "";
    int odometer = 0;
    int startingOdometer = 0;

    long stamp = Instant.now().getEpochSecond();

    @Ignore
    public Truck(String userId, String truckId, int odometer) {
        this.userId = userId;
        this.id = truckId;
        this.startingOdometer = odometer;
        this.odometer = odometer;
    }

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
