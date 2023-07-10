package com.glass.payroll;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;
@Dao
public interface DaoWorkOrders {

    @Upsert
    void add(WorkOrder workOrder);

    @Upsert
    void add(List<WorkOrder> workOrders);

    @Query("DELETE FROM work_order_records WHERE userId = :userId")
    void emptyRecords(String userId);
    @Query("SELECT * FROM work_order_records WHERE truckId = :truckId ORDER BY reading ASC")
    LiveData<List<WorkOrder>> getWorkOrders(String truckId);

    @Query("SELECT * FROM work_order_records WHERE userId = :uid")
    List<WorkOrder> getAllWorkOrders(String uid);

}
