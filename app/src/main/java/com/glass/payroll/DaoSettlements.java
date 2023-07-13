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

    @Query("SELECT * FROM settlement_records WHERE userId = :uid AND year = :year ORDER BY start DESC")
    List<Settlement> getSettlements(String uid, int year);

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


    @Query("SELECT * FROM settlement_records WHERE userId = :uid ORDER BY stop DESC LIMIT 1")
    Settlement getTrueMostRecentSettlement(String uid);

    @Query("SELECT year FROM settlement_records WHERE userId = :uid GROUP BY year ORDER BY year DESC")
    LiveData<List<Integer>> getSettlementYearsLive(String uid);

    @Query("SELECT year FROM settlement_records WHERE userId = :uid GROUP BY year ORDER BY year DESC")
    List<Integer> getSettlementYears(String uid);

    @Query("SELECT quarter FROM settlement_records WHERE userId = :userId AND year = :year GROUP BY quarter ORDER BY quarter DESC")
    List<Integer> getQuarters(String userId, int year);

    @Query("SELECT month FROM settlement_records WHERE userId = :userId AND year = :year GROUP BY month ORDER BY month DESC")
    List<Integer> getMonths(String userId, int year);

    @Query("SELECT week FROM settlement_records WHERE userId = :userId AND year = :year GROUP BY week ORDER BY week DESC")
    List<Integer> getWeeks(String userId, int year);

    @Query("SELECT * FROM settlement_records WHERE userId = :userId AND year = :year AND quarter = :quarter")
    List<Settlement> getSettlementsFromQuarters(String userId, int year, int quarter);

    @Query("SELECT * FROM settlement_records WHERE userId = :userId AND year = :year AND month = :month")
    List<Settlement> getSettlementsFromMonth(String userId, int year, int month);

    @Query("SELECT * FROM settlement_records WHERE userId = :userId AND year = :year AND week = :week")
    List<Settlement> getSettlementsFromWeek(String userId, int year, int week);
}
