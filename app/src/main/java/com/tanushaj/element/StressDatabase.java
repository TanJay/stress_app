package com.tanushaj.element;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {StressItem.class}, version = 1, exportSchema = false)
public abstract class StressDatabase extends RoomDatabase {

    public abstract StressItemDAO stressItemDAO();

    private static volatile StressDatabase INSTANCE;

    static StressDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (StressDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StressDatabase.class, "stress_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
