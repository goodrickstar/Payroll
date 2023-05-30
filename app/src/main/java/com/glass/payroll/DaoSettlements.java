package com.glass.payroll;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface DaoSettlements {

    @Upsert
    void addSettlements(List<Settlement> settlement);

    @Upsert
    long addSettlement(Settlement settlement);

    @Update
    void updateSettlement(Settlement settlement);

    @Query("DELETE FROM settlement_records WHERE id = :id")
    void deleteSettlement(long id);

    @Query("DELETE FROM settlement_records WHERE userId = :userId")
    void emptyRecords(String userId);

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY start DESC LIMIT 10")
    List<String> getSettlements(String uid);

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY start DESC")
    List<Settlement> getAllSettlements(String uid);

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY start DESC LIMIT 1")
    LiveData<Settlement> getSettlement(String uid);

}
