package com.glass.payroll;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final Executor executor;
    private final Records database;
    private String userId;
    private LiveData<Settlement> settlement;
    private LiveData<List<Settlement>> settlements;
    private LiveData<List<Long>> keys;
    private LiveData<SettlementStats> stats;
    private LiveData<Truck> truck;
    private LiveData<Trailer> trailer;

    private LiveData<List<Truck>> trucks;
    private LiveData<List<Trailer>> trailers;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = Records.getDatabase(application);
        executor = Executors.newFixedThreadPool(4);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        settlement = database.daoSettlements().getSettlement(this.userId);
        settlements = database.daoSettlements().getAllSettlements(this.userId);
        stats = database.daoStats().getStats(this.userId);
        truck = database.daoTruck().getTruck(userId);
        trailer = database.daoTrailer().getTrailer(userId);
        trucks = database.daoTruck().getAllTrucks(userId);
        trailers = database.daoTrailer().getAllTrailers(userId);
        keys = database.daoSettlements().getSettlementKeys(userId);
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
        settlement.setStamp(Instant.now().getEpochSecond());
        executor.execute(() -> database.daoSettlements().addSettlement(settlement));
    }

    public void add(SettlementStats stats) {
        executor.execute(() -> database.daoStats().add(stats));
    }

    public void add(List<Settlement> settlements) {
        executor.execute(() -> database.daoSettlements().addSettlements(settlements));
    }

    public void addTrucks(List<Truck> trucks) {
        executor.execute(() -> database.daoTruck().addTrucks(trucks));
    }

    public void addTrailers(List<Trailer> trailers) {
        executor.execute(() -> database.daoTrailer().addTrailers(trailers));
    }


    public void add(Truck truck) {
        truck.setStamp(Instant.now().getEpochSecond());
        executor.execute(() -> database.daoTruck().addTruck(truck));
    }

    public void add(Trailer trailer) {
        trailer.setStamp(Instant.now().getEpochSecond());
        executor.execute(() -> database.daoTrailer().addTrailer(trailer));
    }

    public void emptyTables() {
        executor.execute(() -> {
            database.daoSettlements().emptyRecords(userId);
            database.daoTruck().emptyRecords(userId);
            database.daoTrailer().emptyRecords(userId);
        });
    }
    public LiveData<List<Settlement>> getAllSettlements() {
        return settlements;
    }

    public LiveData<List<Truck>> trucks() {
        return trucks;
    }

    public LiveData<List<Trailer>> trailers() {
        return trailers;
    }

    public Settlement getSettlement() {
        return database.daoSettlements().getCurrentSettlement(userId);
    }

    public List<Settlement> getSettlements() {
        return database.daoSettlements().getSettlements(userId);
    }
    public List<Truck> getTrucks() {
        return database.daoTruck().getTrucks(userId);
    }
    public List<Trailer> getTrailers() {
        return database.daoTrailer().getTrailers(userId);
    }

    public void delete(Settlement settlement) {
        executor.execute(() -> database.daoSettlements().deleteSettlement(settlement.getId()));
    }

    public LiveData<List<Long>> keys() {
        return keys;
    }

    public void setStampOnSettlement(long settlementId, long stamp) {
        executor.execute(() -> database.daoSettlements().setStamp(settlementId, stamp));
    }

    public LiveData<SettlementStats> stats(){
        return stats;
    }

}
