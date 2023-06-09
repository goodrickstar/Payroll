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

    private LiveData<List<Integer>> settlementYears;
    private LiveData<LocationString> locationStringLiveData;
    private LiveData<Long> mostRecentEndingDate;
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
        mostRecentEndingDate = database.daoSettlements().getMostRecentEndingDate(this.userId);
        stats = database.daoStats().getStats(this.userId);
        truck = database.daoTruck().getTruck(userId);
        trailer = database.daoTrailer().getTrailer(userId);
        trucks = database.daoTruck().getAllTrucks(userId);
        trailers = database.daoTrailer().getAllTrailers(userId);
        keys = database.daoSettlements().getSettlementKeys(userId);
        locationStringLiveData = database.daoLocation().getLocationString(userId);
        settlementYears = database.daoSettlements().getSettlementYearsLive(userId);
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

    public void addWorkOrders(List<WorkOrder> workOrders) {
        executor.execute(() -> database.daoWorkOrders().add(workOrders));
    }


    public void add(Truck truck) {
        truck.setStamp(Instant.now().getEpochSecond());
        executor.execute(() -> database.daoTruck().addTruck(truck));
    }

    public void add(WorkOrder workOrder) {
        workOrder.setStamp(Instant.now().getEpochSecond());
        executor.execute(() -> database.daoWorkOrders().add(workOrder));
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
            database.daoWorkOrders().emptyRecords(userId);
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

    public List<Settlement> getSettlementsFromYear(int year) {
        return database.daoSettlements().getSettlements(userId, year);
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

    public LiveData<Long> getMostRecentEndingDate(){
        return mostRecentEndingDate;
    }
    public LiveData<LocationString> location(){
        return locationStringLiveData;
    }

    public void setStampOnSettlement(long settlementId, long stamp) {
        executor.execute(() -> database.daoSettlements().setStamp(settlementId, stamp));
    }

    public LiveData<SettlementStats> stats(){
        return stats;
    }


    public void add(LocationString locationString) {
        executor.execute(() -> database.daoLocation().addLocation(locationString));
    }

    public LiveData<List<WorkOrder>> workOrders(Truck truck) {
        return database.daoWorkOrders().getWorkOrders(truck.getId());
    }

    public List<WorkOrder> getWorkAllOrders() {
        return database.daoWorkOrders().getAllWorkOrders(userId);
    }
    public List<Integer> getYears() {
        return database.daoSettlements().getSettlementYears(userId);
    }

    public List<Integer> getQuarters(int year) {
        return database.daoSettlements().getQuarters(userId, year);
    }

    public List<Integer> getMonths(int year) {
        return database.daoSettlements().getMonths(userId, year);
    }

    public List<Integer> getWeeks(int year) {
        return database.daoSettlements().getWeeks(userId, year);
    }

    public List<Settlement> getSettlementsFromQuarter(int year, int quarter) {
        return database.daoSettlements().getSettlementsFromQuarters(userId, year, quarter);
    }

    public List<Settlement> getSettlementsFromMonth(int year, int month) {
        return database.daoSettlements().getSettlementsFromMonth(userId, year, month);
    }

    public List<Settlement> getSettlementsFromWeek(int year, int week) {
        return database.daoSettlements().getSettlementsFromWeek(userId, year, week);
    }

    public Settlement getTrueMostRecentSettlement(){
        return database.daoSettlements().getTrueMostRecentSettlement(userId);
    }

}
