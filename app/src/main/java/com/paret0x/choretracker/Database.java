package com.paret0x.choretracker;

import android.content.Context;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.List;

@Entity(tableName = "chore_table")
class Chore {
    @PrimaryKey
    @ColumnInfo(name = "chore_id")
    int choreId;

    @ColumnInfo(name = "chore_name")
    String choreName;

    @ColumnInfo(name = "room_id")
    int roomId;

    @ColumnInfo(name = "frequency")
    long frequency; // in days

    @ColumnInfo(name = "last_done")
    long dateLastDone; // seconds since epoch

    public Chore(int choreId, String choreName, int roomId, long frequency, long dateLastDone) {
        this.choreId = choreId;
        this.choreName = choreName;
        this.roomId = roomId;
        this.frequency = frequency;
        this.dateLastDone = dateLastDone;
    }
}

@Entity(tableName = "room_table")
class HomeRoom {
    @PrimaryKey
    @ColumnInfo(name = "room_id")
    int roomId;

    @ColumnInfo(name = "room_name")
    String roomName;

    public HomeRoom(int roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }
}

@Dao
interface ChoreDao {
    @Query("Select * from chore_table")
    List<Chore> getChores();
    @Insert
    void insertChore(Chore c);
    @Update
    void updateChore(Chore c);
    @Delete
    void deleteChore(Chore c);

    @Query("Select * from room_table")
    List<HomeRoom> getRooms();
    @Insert
    void insertRoom(HomeRoom r);
    @Update
    void updateRoom(HomeRoom r);
    @Delete
    void deleteRoom(HomeRoom r);
}

@Database(entities = {Chore.class, HomeRoom.class}, exportSchema = false, version = 2)
abstract class ChoreDatabase extends RoomDatabase {
    public abstract ChoreDao choreDao();

    private static volatile ChoreDatabase instance;

    static synchronized void initDatabase(final Context context) {
        if (instance != null) {
            instance.close();
        }

        Log.i("ChoreDatabase", "Building database");
        instance = Room.databaseBuilder(context.getApplicationContext(), ChoreDatabase.class, "choreList_db").build();
    }

    static synchronized ChoreDatabase getDatabase() {
        return instance;
    }
}