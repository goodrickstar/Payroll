package com.glass.payroll;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Arrays;

@Entity(tableName = "user_stats")
public class SettlementStats {
    public SettlementStats() {
    }

    @PrimaryKey
            @NonNull
    String userId;

    double totalGross = 0.0;
    double totalFuel = 0.0;
    double totalMiles = 0.0;
    double totalProfit = 0.0;
    double totalGallons = 0.0;
    double avgBalance = 0.0;
    double avgGross = 0.0;
    double avgMiles = 0.0;
    double avgEmptyMiles = 0.0;
    double avgLoadedMiles = 0.0;
    double avgGallons = 0.0;
    double avgFuelCost = 0.0;
    double avgRate = 0.0;

    public SettlementStats(String userId) {
        this.userId = userId;
    }

    @Ignore


    public double getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(double totalGross) {
        this.totalGross = totalGross;
    }

    public double getTotalFuel() {
        return totalFuel;
    }

    public void setTotalFuel(double totalFuel) {
        this.totalFuel = totalFuel;
    }

    public double getTotalMiles() {
        return totalMiles;
    }

    public void setTotalMiles(double totalMiles) {
        this.totalMiles = totalMiles;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getTotalGallons() {
        return totalGallons;
    }

    public void setTotalGallons(double totalGallons) {
        this.totalGallons = totalGallons;
    }

    public double getAvgBalance() {
        return avgBalance;
    }

    public void setAvgBalance(double avgBalance) {
        this.avgBalance = avgBalance;
    }

    public double getAvgGross() {
        return avgGross;
    }

    public void setAvgGross(double avgGross) {
        this.avgGross = avgGross;
    }

    public double getAvgMiles() {
        return avgMiles;
    }

    public void setAvgMiles(double avgMiles) {
        this.avgMiles = avgMiles;
    }

    public double getAvgEmptyMiles() {
        return avgEmptyMiles;
    }

    public void setAvgEmptyMiles(double avgEmptyMiles) {
        this.avgEmptyMiles = avgEmptyMiles;
    }

    public double getAvgLoadedMiles() {
        return avgLoadedMiles;
    }

    public void setAvgLoadedMiles(double avgLoadedMiles) {
        this.avgLoadedMiles = avgLoadedMiles;
    }

    public double getAvgGallons() {
        return avgGallons;
    }

    public void setAvgGallons(double avgGallons) {
        this.avgGallons = avgGallons;
    }

    public double getAvgFuelCost() {
        return avgFuelCost;
    }

    public void setAvgFuelCost(double avgFuelCost) {
        this.avgFuelCost = avgFuelCost;
    }

    public double getAvgRate() {
        return avgRate;
    }

    public void setAvgRate(double avgRate) {
        this.avgRate = avgRate;
    }
}
