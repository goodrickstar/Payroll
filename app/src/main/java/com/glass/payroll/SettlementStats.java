package com.glass.payroll;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.database.annotations.NotNull;

@Entity(tableName = "user_stats")
public class SettlementStats {
    public SettlementStats() {
    }

    @PrimaryKey @NonNull
    String userId;

    double totalGross = 0.0;
    double totalFuel = 0.0;
    double totalMiles = 0.0;
    double totalProfit = 0.0;
    double totalDieselGallons = 0.0;
    double totalDefGallons = 0.0;
    double avgBalance = 0.0;
    double avgGross = 0.0;
    double avgMiles = 0.0;
    double avgEmptyMiles = 0.0;
    double avgLoadedMiles = 0.0;
    double avgDieselGallons = 0.0;
    double avgDefGallons = 0.0;
    double avgFuelCost = 0.0;
    double avgGeneralRate = 0.0;
    double avgHazmatRate = 0.0;
    double avgReeferRate = 0.0;
    double avgHazmatAndReeferRate = 0.0;

    @Ignore
    public SettlementStats(String userId) {
        this.userId = userId;
    }

    public double getTotalDefGallons() {
        return totalDefGallons;
    }

    public void setTotalDefGallons(double totalDefGallons) {
        this.totalDefGallons = totalDefGallons;
    }

    public double getAvgDefGallons() {
        return avgDefGallons;
    }

    public void setAvgDefGallons(double avgDefGallons) {
        this.avgDefGallons = avgDefGallons;
    }

    public double getAvgHazmatRate() {
        return avgHazmatRate;
    }

    public void setAvgHazmatRate(double avgHazmatRate) {
        this.avgHazmatRate = avgHazmatRate;
    }

    public double getAvgReeferRate() {
        return avgReeferRate;
    }

    public void setAvgReeferRate(double avgReeferRate) {
        this.avgReeferRate = avgReeferRate;
    }

    public double getAvgHazmatAndReeferRate() {
        return avgHazmatAndReeferRate;
    }

    public void setAvgHazmatAndReeferRate(double avgHazmatAndReeferRate) {
        this.avgHazmatAndReeferRate = avgHazmatAndReeferRate;
    }

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

    public double getTotalDieselGallons() {
        return totalDieselGallons;
    }

    public void setTotalDieselGallons(double totalDieselGallons) {
        this.totalDieselGallons = totalDieselGallons;
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

    public double getAvgDieselGallons() {
        return avgDieselGallons;
    }

    public void setAvgDieselGallons(double avgDieselGallons) {
        this.avgDieselGallons = avgDieselGallons;
    }

    public double getAvgFuelCost() {
        return avgFuelCost;
    }

    public void setAvgFuelCost(double avgFuelCost) {
        this.avgFuelCost = avgFuelCost;
    }

    public double getAvgGeneralRate() {
        return avgGeneralRate;
    }

    public void setAvgGeneralRate(double avgGeneralRate) {
        this.avgGeneralRate = avgGeneralRate;
    }
}
