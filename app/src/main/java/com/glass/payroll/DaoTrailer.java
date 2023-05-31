package com.glass.payroll;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface DaoTrailer {

    @Upsert
    void addTrailers(List<Trailer> trailer);

    @Upsert
    long addTrailer(Trailer trailer);

    @Update
    void updateTrailer(Trailer trailer);

    @Query("DELETE FROM trailer_records WHERE id = :id")
    void deleteTrailer(long id);

    @Query("DELETE FROM trailer_records WHERE userId = :userId")
    void emptyRecords(String userId);

    @Query("SELECT * FROM trailer_records WHERE userId = :uid ORDER BY stamp DESC")
    List<Trailer> getAllTrailers(String uid);

    @Query("SELECT * FROM trailer_records WHERE userId = :uid ORDER BY stamp DESC LIMIT 1")
    LiveData<Trailer> getTrailer(String uid);

}
