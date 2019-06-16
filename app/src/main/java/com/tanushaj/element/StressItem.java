package com.tanushaj.element;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Entity(tableName = "stress_tbl")
public class StressItem {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private long date;

    private int hours;

    StressItem(long date){
        this.hours = 5;
        this.date = date;
    }


    @NonNull
    public long getDate() {
        return date;
    }

    public void setDate(@NonNull long date) {
        this.date = date;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    


}
