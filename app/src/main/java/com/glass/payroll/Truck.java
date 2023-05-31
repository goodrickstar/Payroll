package com.glass.payroll;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "truck_records")
public class Truck {
    @PrimaryKey
    String id = "";
    String userId = "";
    int odometer = 0;
    int startingOdometer = 0;

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
