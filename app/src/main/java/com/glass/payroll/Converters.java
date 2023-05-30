package com.glass.payroll;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String loadToString(ArrayList<Load> data) {
        return new Gson().toJson(data);
    }

    @TypeConverter
    public static String fuelToString(ArrayList<Fuel> data) {
        return new Gson().toJson(data);
    }

    @TypeConverter
    public static String costToString(ArrayList<Cost> data) {
        return new Gson().toJson(data);
    }

    @TypeConverter
    public static String payoutToString(Payout data) {
        return new Gson().toJson(data);
    }

    @TypeConverter
    public static ArrayList<Load> loadFromString(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Load>>() {
        }.getType());
    }

    @TypeConverter
    public static ArrayList<Cost> costFromString(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Cost>>() {
        }.getType());
    }

    @TypeConverter
    public static ArrayList<Fuel> fuelFromString(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Fuel>>() {
        }.getType());
    }

    @TypeConverter
    public static Payout payoutFromString(String data) {
        return new Gson().fromJson(data, Payout.class);
    }

}
