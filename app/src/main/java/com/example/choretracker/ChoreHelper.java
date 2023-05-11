package com.example.choretracker;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
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
import androidx.sqlite.db.SupportSQLiteDatabase;

@Entity(tableName = "chore_table")
class Chore {
    @PrimaryKey
    @ColumnInfo(name = "chore_id")
    int choreId;

    @ColumnInfo(name = "chore_name")
    String choreName;

    @ColumnInfo(name = "date_opened")
    String dateTimeOpened;

    @ColumnInfo(name = "assigned_to")
    String assignedTo;

    @ColumnInfo(name = "is_completed")
    boolean isCompleted;

    @ColumnInfo(name = "chore_weight")
    String choreWeight;

    public Chore(int choreId, String choreName, String dateTimeOpened, String assignedTo, boolean isCompleted, String choreWeight) {
        this.choreId = choreId;
        this.choreName = choreName;
        this.dateTimeOpened = dateTimeOpened;
        this.assignedTo = assignedTo;
        this.isCompleted = isCompleted;
        this.choreWeight = choreWeight;
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
}

@Database(entities = Chore.class, exportSchema = false, version = 1)
abstract class ChoreDatabase extends RoomDatabase {
    public abstract ChoreDao choreDao();

    private static volatile ChoreDatabase instance;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    static synchronized void initDatabase(final Context context) {
        if (instance == null) {
            Log.i("ChoreDatabase", "Building database");
            instance = Room.databaseBuilder(context.getApplicationContext(), ChoreDatabase.class, "choreList_db").addCallback(roomBuildCallback).build();
        }
    }

    static synchronized ChoreDatabase getDatabase() {
        return instance;
    }

    private static final RoomDatabase.Callback roomBuildCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            executorService.submit(() -> {
                List<Chore> choreList = instance.choreDao().getChores();
                Log.i("ChoreDatabase", "Finished reading database. Found " + choreList.size() + " chores");
            });
        }
    };
}

enum ChoreType {
    INACTIVE,
    OVERDUE,
    ONGOING,
    COMPLETED
}

class ChoreHolder {
    public HashMap<ChoreType, ArrayList<Chore>> choreLists = new HashMap<>();

    private static ChoreHolder instance;
    public static ChoreHolder getInstance() {
        if (instance == null) {
            instance = new ChoreHolder();
        }
        return instance;
    }
}

class OngoingHolder {
    public ArrayList<Chore> ongoingList = new ArrayList<>();
    public ArrayList<Chore> inactiveList = new ArrayList<>();

    private static OngoingHolder instance;
    public static OngoingHolder getInstance() {
        if (instance == null) {
            instance = new OngoingHolder();
        }
        return instance;
    }
}

class ChoreDiff {
    public int weeks = 0;
    public int days = 0;
    public int hours = 0;
}

public class ChoreHelper {
    private static ChoreHelper instance;
    private final ArrayList<Chore> chores = new ArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static synchronized ChoreHelper getInstance() {
        if (instance == null) {
            instance = new ChoreHelper();
        }
        return instance;
    }

    public synchronized void initDatabase(final Context context) {
        Log.i(this.getClass().getSimpleName(), "Initializing database");
        chores.clear();
        ChoreDatabase.initDatabase(context);
        executorService.submit(() -> chores.addAll(ChoreDatabase.getDatabase().choreDao().getChores()));
    }

    public int getNumChores() {
        return chores.size();
    }

    public ArrayList<Chore> getChores() {
        return chores;
    }

    public Chore getChoreAt(int index) {
        return chores.get(index);
    }

    public void addChore(final String choreName) {
        final int choreId = (chores.size() > 0) ? (chores.get(chores.size() - 1).choreId + 1) : 1;
        Chore c = new Chore(choreId, choreName, "", "", false, "XS");
        chores.add(c);
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().insertChore(c));
    }

    public void updateChore(Chore c) {
        Optional<Chore> foundChore = chores.stream().filter(chore -> chore.choreId == c.choreId).findAny();
        foundChore.ifPresent(chore -> chores.set(chores.indexOf(chore), c));
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().updateChore(c));
    }

    public void deleteChore(Chore c) {
        chores.remove(c);
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().deleteChore(c));
    }

    public Chore getChoreById(int choreId) {
        Optional<Chore> foundChore = chores.stream().filter(chore -> chore.choreId == choreId).findAny();
        return foundChore.orElse(null);
    }

    public int getChoreIndex(int choreId) {
        Optional<Chore> foundChore = chores.stream().filter(chore -> chore.choreId == choreId).findAny();
        return foundChore.map(chores::indexOf).orElse(-1);
    }

    public ChoreType getTypeOfChore(@NonNull Chore c) {
        if (c.isCompleted) {
            return ChoreType.COMPLETED;
        }

        ChoreDiff diff = getDiff(c);
        if (diff == null) {
            return ChoreType.INACTIVE;
        }

        if (diff.weeks >= 1) {
            return ChoreType.OVERDUE;
        } else {
            return ChoreType.ONGOING;
        }
    }

    public void populateChoreHolder() {
        for (ChoreType type : ChoreType.values()) {
            if (ChoreHolder.getInstance().choreLists.containsKey(type)) {
                Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(type)).clear();
            } else {
                ChoreHolder.getInstance().choreLists.put(type, new ArrayList<>());
            }
        }

        for (Chore c : chores) {
            ChoreType type = getTypeOfChore(c);
            Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(type)).add(c);
        }
    }

    public void populateOngoingHolder() {
        OngoingHolder.getInstance().ongoingList.clear();
        OngoingHolder.getInstance().inactiveList.clear();

        for (Chore c : chores) {
            ChoreType type = getTypeOfChore(c);

            if ((type == ChoreType.ONGOING) || (type == ChoreType.OVERDUE)) {
                OngoingHolder.getInstance().ongoingList.add(c);
            } else {
                OngoingHolder.getInstance().inactiveList.add(c);
            }
        }
    }

    public ChoreType getChoreHolderType(int choreId) {
        ChoreType type = ChoreType.INACTIVE;

        for (ChoreType cType : ChoreType.values()) {
            Optional<Chore> foundChore = Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(cType)).stream().filter(chore -> chore.choreId == choreId).findAny();
            if (foundChore.isPresent()) {
                return cType;
            }
        }

        return type;
    }

    public int getChoreHolderIndex(int choreId) {
        int index = -1;

        for (ChoreType cType : ChoreType.values()) {
            Optional<Chore> foundChore = Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(cType)).stream().filter(chore -> chore.choreId == choreId).findAny();
            if (foundChore.isPresent()) {
                return Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(cType)).indexOf(foundChore.get());
            }
        }

        return index;
    }

    public int getOngoingHolderIndex(int choreId) {
        Optional<Chore> foundChore;
        foundChore = OngoingHolder.getInstance().ongoingList.stream().filter(chore -> chore.choreId == choreId).findFirst();
        if (foundChore.isPresent()) {
            return OngoingHolder.getInstance().ongoingList.indexOf(foundChore.get());
        }

        foundChore = OngoingHolder.getInstance().inactiveList.stream().filter(chore -> chore.choreId == choreId).findFirst();
        return foundChore.map(chore -> OngoingHolder.getInstance().inactiveList.indexOf(chore)).orElse(-1);
    }

    @SuppressLint("SimpleDateFormat")
    public ChoreDiff getDiff(Chore c) {
        ChoreDiff diff = new ChoreDiff();

        Date choreOpenedDate;
        try {
            choreOpenedDate = new SimpleDateFormat().parse(c.dateTimeOpened);
        } catch (ParseException e) {
            return null;
        }

        Date currentDate = Calendar.getInstance().getTime();
        long difference = currentDate.getTime() - Objects.requireNonNull(choreOpenedDate).getTime();

        long hoursInMilli = 1000 * 60 * 60;
        long daysInMilli = hoursInMilli * 24;
        long weeksInMilli = daysInMilli * 7;

        diff.weeks = (int)(difference / weeksInMilli);
        difference %= weeksInMilli;
        diff.days = (int)(difference / daysInMilli);
        difference %= daysInMilli;
        diff.hours = (int)(difference / hoursInMilli);

        return diff;
    }
}
