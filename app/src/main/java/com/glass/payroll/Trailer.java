package com.glass.payroll;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;

@Entity(tableName = "trailer_records")
public class Trailer {

    public Trailer() {
    }

    @Ignore
    public Trailer(String userId, @NonNull String id) {
        this.id = id;
        this.userId = userId;
    }

    @PrimaryKey
    @NonNull
    String id = "";
    String userId = "";

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

}
