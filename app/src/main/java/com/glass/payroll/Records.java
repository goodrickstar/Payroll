package com.glass.payroll;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Settlement.class}, version = 1)
abstract public class Records extends RoomDatabase {
    public abstract DaoSettlements daoSettlements();


    private static volatile Records INSTANCE;

    static Records getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (Records.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Records.class, "word_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
