package com.glass.payroll;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trailer_records")
public class Trailer {
    @PrimaryKey
    String id = "";
    String userId = "";

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
