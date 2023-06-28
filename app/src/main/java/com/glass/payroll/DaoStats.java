package com.glass.payroll;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

@Dao
public interface DaoStats {

    @Upsert
    void add(SettlementStats stats);

    @Query("SELECT * FROM user_stats WHERE userId = :uid")
    LiveData<SettlementStats> getStats(String uid);

}
