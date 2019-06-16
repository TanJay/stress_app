package com.tanushaj.element;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface StressItemDAO {

    @Insert
    void insert(StressItem stressItem);

    @Query("Select * from stress_tbl")
    LiveData<List<StressItem>> getStresItems();

    @Query("DELETE FROM stress_tbl")
    void deleteAll();

}
