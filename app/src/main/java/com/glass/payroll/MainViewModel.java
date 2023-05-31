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
    private LiveData<Settlement> settlement;
    private LiveData<List<Settlement>> settlements;
    private LiveData<Truck> truck;
    private LiveData<Trailer> trailer;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = Records.getDatabase(application);
        executor = Executors.newFixedThreadPool(4);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        settlement = database.daoSettlements().getSettlement(this.userId);
        settlements = database.daoSettlements().getAllSettlements(this.userId);
        truck = database.daoTruck().getTruck(this.userId);
        trailer = database.daoTrailer().getTrailer(this.userId);
    }

    public Executor executor() {
        return executor;
    }

    public LiveData<Settlement> settlement() {
        return settlement;
    }
    public LiveData<Truck> truck() {
        return truck;
    }
    public LiveData<Trailer> trailer() {
        return trailer;
    }

    public void add(Settlement settlement) {
        executor.execute(() -> database.daoSettlements().addSettlement(settlement));
    }

    public void add(List<Settlement> settlements) {
        executor.execute(() -> database.daoSettlements().addSettlements(settlements));
    }


    public void add(Truck truck) {
        executor.execute(() -> database.daoTruck().addTruck(truck));
    }

    public void restore(List<Settlement> settlements) {
        executor.execute(() -> {
            database.daoSettlements().emptyRecords(userId);
            database.daoSettlements().addSettlements(settlements);
        });
    }

    public LiveData<List<Settlement>> getAllSettlements() {
        return settlements;
    }

    public Settlement getSettlement() {
        return database.daoSettlements().getCurrentSettlement(userId);
    }

    public void delete(Settlement settlement) {
        executor.execute(() -> database.daoSettlements().deleteSettlement(settlement.getId()));
    }

}
