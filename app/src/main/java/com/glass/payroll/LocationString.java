package com.glass.payroll;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "location_records")
public class LocationString {
    @PrimaryKey
    @NonNull
    String userId = "";
    String location = "";

    public LocationString() {
    }

    public LocationString(String uid, String result) {
        this.userId = uid;
        this.location = result;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
