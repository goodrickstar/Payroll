package com.glass.payroll;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "settlement_records")
public class Settlement {


    private long stamp = System.currentTimeMillis();

    @PrimaryKey(autoGenerate = true)
    private long id = 0L;

    private String userId = "";
    private String truck = "";
    private String trailer = "";
    private long start = 0;
    private long stop = 0;
    private int emptyMiles = 0;
    private int loadedMiles = 0;
    private int week = 1;
    private int month = 1;
    private int quarter = 1;
    private int year = 23;
    private double dieselGallons = 23;
    private double defGallons = 23;
    private double gross = 0.0;
    private double balance = 0.0;
    private double fuelCost = 0.0;
    private double defCost = 0.0;
    private double fixedCost = 0.0;
    private double miscCost = 0.0;
    private double payoutCost = 0.0;
    private double maintenanceCost = 0.0;
    private Payout payout = new Payout();
    private ArrayList<Load> loads = new ArrayList<>();
    private ArrayList<Fuel> fuel = new ArrayList<>();
    private ArrayList<Cost> fixed = new ArrayList<>();
    private ArrayList<Cost> miscellaneous = new ArrayList<>();

    public Settlement() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getPayoutCost() {
        return payoutCost;
    }

    public void setPayoutCost(double payoutCost) {
        this.payoutCost = payoutCost;
    }

    public double getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(double maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStop() {
        return stop;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }

    public int getEmptyMiles() {
        return emptyMiles;
    }

    public void setEmptyMiles(int emptyMiles) {
        this.emptyMiles = emptyMiles;
    }

    public int getLoadedMiles() {
        return loadedMiles;
    }

    public void setLoadedMiles(int loadedMiles) {
        this.loadedMiles = loadedMiles;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getDieselGallons() {
        return dieselGallons;
    }

    public void setDieselGallons(double dieselGallons) {
        this.dieselGallons = dieselGallons;
    }

    public double getDefGallons() {
        return defGallons;
    }

    public void setDefGallons(double defGallons) {
        this.defGallons = defGallons;
    }

    public double getGross() {
        return gross;
    }

    public void setGross(double gross) {
        this.gross = gross;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getFuelCost() {
        return fuelCost;
    }

    public void setFuelCost(double fuelCost) {
        this.fuelCost = fuelCost;
    }

    public double getDefCost() {
        return defCost;
    }

    public void setDefCost(double defCost) {
        this.defCost = defCost;
    }

    public double getFixedCost() {
        return fixedCost;
    }

    public void setFixedCost(double fixedCost) {
        this.fixedCost = fixedCost;
    }

    public double getMiscCost() {
        return miscCost;
    }

    public void setMiscCost(double miscCost) {
        this.miscCost = miscCost;
    }

    public Payout getPayout() {
        return payout;
    }

    public void setPayout(Payout payout) {
        this.payout = payout;
    }

    public ArrayList<Load> getLoads() {
        return loads;
    }

    public void setLoads(ArrayList<Load> loads) {
        this.loads = loads;
    }

    public ArrayList<Fuel> getFuel() {
        return fuel;
    }

    public void setFuel(ArrayList<Fuel> fuel) {
        this.fuel = fuel;
    }

    public ArrayList<Cost> getFixed() {
        return fixed;
    }

    public void setFixed(ArrayList<Cost> fixed) {
        this.fixed = fixed;
    }

    public ArrayList<Cost> getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(ArrayList<Cost> miscellaneous) {
        this.miscellaneous = miscellaneous;
    }
}
