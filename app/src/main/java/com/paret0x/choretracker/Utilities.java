package com.paret0x.choretracker;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Calendar;
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
    public int months = 0;
    public int weeks = 0;
    public int days = 0;
}

public class Utilities {
    private static Utilities instance;
    public boolean hasDatabaseLoaded = false;
    private final ArrayList<Chore> chores = new ArrayList<>();
    private final ArrayList<HomeRoom> rooms = new ArrayList<>();
    private final HashMap<ChoreStatus, ArrayList<Chore>> choresByStatus = new HashMap<>();
    private final HashMap<Integer, ArrayList<Chore>> choresByRoom = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    public final long milliToDays = 1000 * 60 * 60 * 24;

    public static synchronized Utilities getInstance() {
        if (instance == null) {
            instance = new Utilities();
        }
        return instance;
    }

    public synchronized void populateUtilities() {
        Log.i(this.getClass().getSimpleName(), "Populating utilities");
        chores.clear();
        rooms.clear();
        executorService.submit(() -> {
            Log.i(this.getClass().getSimpleName(), "Reading from database");
            chores.addAll(ChoreDatabase.getDatabase().choreDao().getChores());
            rooms.addAll(ChoreDatabase.getDatabase().choreDao().getRooms());
            hasDatabaseLoaded = true;
            Log.i(this.getClass().getSimpleName(), "Done reading from database");
        });
    }

    /* ===== Database ===== */
    public void addChore(Chore c) {
        c.choreId = (chores.size() > 0) ? (chores.get(chores.size() - 1).choreId + 1) : 1;
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
        chores.forEach(chore -> chore.roomId = -1);
        executorService.submit(() -> ChoreDatabase.getDatabase().choreDao().deleteRoom(r));
    }

    /* ===== ChoreStatus ===== */
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
        ChoreStatus status;

        TimeDiff choreDiff = getTimeDiffForChore(c);
        TimeDiff frequency = getTimeDiff(c.frequency);

        long daysSinceDone = (choreDiff.months * 30L) + (choreDiff.weeks * 7L) + choreDiff.days;
        long daysInFrequency = (frequency.months * 30L) + (frequency.weeks * 7L) + frequency.days;

        if (daysSinceDone < daysInFrequency) {
            status = ChoreStatus.COMPLETED;
        } else if (daysSinceDone < (daysInFrequency * 2L)) {
            status = ChoreStatus.ONGOING;
        } else {
            status = ChoreStatus.OVERDUE;
        }

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

    public ArrayList<Chore> getChoresByStatus(ChoreStatus status) {
        return choresByStatus.get(status);
    }

    /* ===== HomeRoom ===== */
    public int getNumRooms() {
        return rooms.size();
    }

    public HomeRoom getRoomAt(int index) {
        return rooms.get(index);
    }

    public void updateChoresByRoom() {
        // Reset values
        for (HomeRoom r : rooms) {
            if (choresByRoom.containsKey(r.roomId)) {
                Objects.requireNonNull(choresByRoom.get(r.roomId)).clear();
            } else {
                choresByRoom.put(r.roomId, new ArrayList<>());
            }
        }

        // Update all chores
        for (Chore c : chores) {
            Objects.requireNonNull(choresByRoom.get(c.roomId)).add(c);
        }
    }

    public ArrayList<Chore> getChoresByRoom(int roomId) {
        return Objects.requireNonNull(choresByRoom.get(roomId));
    }

    public ArrayList<String> getRoomNames() {
        ArrayList<String> roomNames = new ArrayList<>();

        for (HomeRoom r : rooms) {
            roomNames.add(r.roomName);
        }

        return roomNames;
    }

    public int getRoomIdByName(String roomName) {
        Optional<HomeRoom> foundRoom = rooms.stream().filter(room -> Objects.equals(room.roomName, roomName)).findAny();
        return foundRoom.map(homeRoom -> homeRoom.roomId).orElse(-1);
    }

    public String getRoomNameById(int roomId) {
        Optional<HomeRoom> foundRoom = rooms.stream().filter(room -> room.roomId == roomId).findAny();
        return foundRoom.map(homeRoom -> homeRoom.roomName).orElse(null);
    }

    /* ===== TimeDiff ===== */
    public TimeDiff getTimeDiffForChore(Chore c) {
        LocalDate currentTime = LocalDate.now();
        LocalDate dateLastDone = LocalDate.parse(c.dateLastDone);

        long daysElapsed = ChronoUnit.DAYS.between(dateLastDone, currentTime);
        return getTimeDiff(daysElapsed);
    }

    public TimeDiff getTimeDiff(long duration) {
        TimeDiff diff = new TimeDiff();

        long daysInWeek = 7;
        long daysInMonth = 30;

        diff.months = (int)(duration / daysInMonth);
        duration %= daysInMonth;
        diff.weeks = (int)(duration / daysInWeek);
        duration %= daysInWeek;
        diff.days = (int)duration;

        return diff;
    }
}
