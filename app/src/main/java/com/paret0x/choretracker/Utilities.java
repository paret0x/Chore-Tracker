package com.paret0x.choretracker;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum ChoreStatus {
    INACTIVE,
    OVERDUE,
    ONGOING,
    COMPLETED
}

class TimeDiff {
    public int weeks = 0;
    public int days = 0;
    public int hours = 0;
}

public class Utilities {
    private static Utilities instance;
    private final ArrayList<Chore> chores = new ArrayList<>();
    private final ArrayList<HomeRoom> rooms = new ArrayList<>();
    private final HashMap<ChoreStatus, ArrayList<Chore>> choresByStatus = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static synchronized Utilities getInstance() {
        if (instance == null) {
            instance = new Utilities();
        }
        return instance;
    }

    public synchronized void populateUtilities(final Context context) {
        Log.i(this.getClass().getSimpleName(), "Initializing database");
        chores.clear();
        rooms.clear();
        ChoreDatabase.initDatabase(context);
        executorService.submit(() -> rooms.addAll(ChoreDatabase.getDatabase().choreDao().getRooms()));
        executorService.submit(() -> chores.addAll(ChoreDatabase.getDatabase().choreDao().getChores()));
    }

    public int getNumChores() {
        return chores.size();
    }

    public int getNumRooms() {
        return rooms.size();
    }

    public ArrayList<Chore> getChores() {
        return chores;
    }

    public ArrayList<HomeRoom> getRooms() {
        return rooms;
    }

    public Chore getChoreAt(int index) {
        return chores.get(index);
    }

    public HomeRoom getRoomAt(int index) {
        return rooms.get(index);
    }

    public void addChore(final String choreName) {
        final int choreId = (chores.size() > 0) ? (chores.get(chores.size() - 1).choreId + 1) : 1;
        Chore c = new Chore(choreId, choreName, -1,"", "", "");
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

    public void addRoom(final String roomName) {
        final int roomId = (rooms.size() > 0) ? (rooms.get(rooms.size() - 1).roomId + 1) : 1;
        HomeRoom r = new HomeRoom(roomId, roomName);
        rooms.add(r);
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().insertRoom(r));
    }

    public void updateRoom(HomeRoom r) {
        Optional<HomeRoom> foundRoom = rooms.stream().filter(room -> room.roomId == r.roomId).findAny();
        foundRoom.ifPresent(room -> rooms.set(rooms.indexOf(room), r));
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().updateRoom(r));
    }

    public void deleteRoom(HomeRoom r) {
        rooms.remove(r);
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().deleteRoom(r));
    }

    public int getRoomIndex(HomeRoom room) {
        return rooms.indexOf(room);
    }

    public ChoreStatus getChoreStatus(int choreId) {
        ChoreStatus status = ChoreStatus.INACTIVE;

        for (ChoreStatus statusValue : ChoreStatus.values()) {
            Optional<Chore> foundChore = Objects.requireNonNull(choresByStatus.get(statusValue)).stream().filter(chore -> chore.choreId == choreId).findAny();
            if (foundChore.isPresent()) {
                status = statusValue;
                break;
            }
        }

        return status;
    }

    public ChoreStatus determineChoreStatus(@NonNull Chore c) {
        ChoreStatus status = ChoreStatus.INACTIVE;

        TimeDiff diff = getTimeDiff(c);
        if (diff == null) {
            return status;
        }

        status = ChoreStatus.ONGOING;
        return status;
    }

    public void updateChoreStatuses() {
        // Reset values
        for (ChoreStatus status : ChoreStatus.values()) {
            if (choresByStatus.containsKey(status)) {
                Objects.requireNonNull(choresByStatus.get(status)).clear();
            } else {
                choresByStatus.put(status, new ArrayList<>());
            }
        }

        // Update all chores
        for (Chore c : chores) {
            ChoreStatus type = determineChoreStatus(c);
            Objects.requireNonNull(choresByStatus.get(type)).add(c);
        }
    }

    public void moveChoreByStatus(Chore c, ChoreStatus oldStatus, ChoreStatus newStatus) {
        Objects.requireNonNull(choresByStatus.get(oldStatus)).remove(c);
        Objects.requireNonNull(choresByStatus.get(newStatus)).add(c);
        updateChore(c);
    }

    public int getNumOfChoresByStatus(ChoreStatus status) {
        if (choresByStatus.containsKey(status)) {
            return Objects.requireNonNull(choresByStatus.get(status)).size();
        } else {
            return 0;
        }
    }

    public Chore getChoreByStatusIndex(ChoreStatus status, int index) {
        return Objects.requireNonNull(choresByStatus.get(status)).get(index);
    }

    public int getChoreStatusIndex(int choreId) {
        int index = -1;

        for (ChoreStatus statusValue : ChoreStatus.values()) {
            Optional<Chore> foundChore = Objects.requireNonNull(choresByStatus.get(statusValue)).stream().filter(chore -> chore.choreId == choreId).findAny();
            if (foundChore.isPresent()) {
                index = Objects.requireNonNull(choresByStatus.get(statusValue)).indexOf(foundChore.get());
                break;
            }
        }

        return index;
    }

    @SuppressLint("SimpleDateFormat")
    public TimeDiff getTimeDiff(Chore c) {
        TimeDiff diff = new TimeDiff();

        Date choreOpenedDate;
        try {
            choreOpenedDate = new SimpleDateFormat().parse(c.dateLastDone);
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

    /*
    public Chore getChoreById(int choreId) {
        Optional<Chore> foundChore = chores.stream().filter(chore -> chore.choreId == choreId).findAny();
        return foundChore.orElse(null);
    }

    public int getChoreIndex(int choreId) {
        Optional<Chore> foundChore = chores.stream().filter(chore -> chore.choreId == choreId).findAny();
        return foundChore.map(chores::indexOf).orElse(-1);
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
    */
}
