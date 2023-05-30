package com.glass.payroll;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final Executor executor;
    private final Records database;
    private String userId;
    private LiveData<Settlement> settlementLiveData;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = Records.getDatabase(application);
        executor = Executors.newFixedThreadPool(4);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        settlementLiveData = database.daoSettlements().getSettlement(this.userId);
    }

    public Executor executor() {
        return executor;
    }

    public LiveData<Settlement> settlementLiveData() {
        return settlementLiveData;
    }

    public void add(Settlement settlement) {
        executor.execute(() -> database.daoSettlements().addSettlement(settlement));
    }

    public void add(List<Settlement> settlements) {
        executor.execute(() -> database.daoSettlements().addSettlements(settlements));
    }

    public void restore(List<Settlement> settlements) {
        executor.execute(() -> {
            database.daoSettlements().emptyRecords(userId);
            database.daoSettlements().addSettlements(settlements);
        });
    }

    public List<Settlement> getAllSettlements() {
        return database.daoSettlements().getAllSettlements(userId);
    }


}
