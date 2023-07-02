package com.glass.payroll;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;
@Dao
public interface DaoLocation {

    @Upsert
    void addLocation(LocationString locationString);

    @Query("SELECT * FROM location_records WHERE userId = :uid LIMIT 1")
    LiveData<LocationString> getLocationString(String uid);
}
