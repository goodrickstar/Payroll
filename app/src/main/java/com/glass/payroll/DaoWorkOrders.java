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

    @Query("SELECT * FROM work_order_records WHERE truckId = :truckId ORDER BY reading ASC")
    LiveData<List<WorkOrder>> getWorkOrders(String truckId);

}
