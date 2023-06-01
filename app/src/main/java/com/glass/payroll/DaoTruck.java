package com.glass.payroll;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface DaoTruck {

    @Upsert
    void addTrucks(List<Truck> truck);

    @Upsert
    long addTruck(Truck truck);

    @Update
    void updateTruck(Truck truck);

    @Query("DELETE FROM truck_records WHERE id = :id")
    void deleteTruck(long id);

    @Query("DELETE FROM truck_records WHERE userId = :userId")
    void emptyRecords(String userId);

    @Query("SELECT * FROM truck_records WHERE userId = :uid ORDER BY stamp DESC")
    LiveData<List<Truck>> getAllTrucks(String uid);

    @Query("SELECT * FROM truck_records WHERE userId = :uid ORDER BY stamp DESC LIMIT 1")
    LiveData<Truck> getTruck(String uid);

}
