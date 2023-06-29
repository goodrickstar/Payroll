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

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY stamp DESC LIMIT 10")
    List<Settlement> getSettlements(String uid);

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY start DESC")
    LiveData<List<Settlement>> getAllSettlements(String uid);

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY stamp DESC LIMIT 1")
    LiveData<Settlement> getSettlement(String uid);

    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY stamp DESC LIMIT 1")
    Settlement getCurrentSettlement(String uid);

    @Query("SELECT id FROM settlement_records WHERE userId = :uid ORDER BY start DESC LIMIT 10")
    LiveData<List<Long>> getSettlementKeys(String uid);

    @Query("UPDATE settlement_records SET stamp = :stamp WHERE id = :settlementId")
    void setStamp(long settlementId, long stamp);

    @Query("SELECT stop FROM settlement_records WHERE userId = :uid ORDER BY stop DESC LIMIT 1")
    LiveData<Long> getMostRecentEndingDate(String uid);
}
