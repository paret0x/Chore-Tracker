package com.paret0x.choretracker;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

interface FragmentChanger {
    void openSettings();
    void openEditRoom(HomeRoom existingRoom);
    void openEditChore(Chore existingChore);
}

public class MainActivity extends FragmentActivity implements FragmentChanger{
    private ImageView actionButton;
    private TextView fragmentTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChoreDatabase.initDatabase(this);
        Utilities.getInstance().populateUtilities();

        fragmentTitle = findViewById(R.id.nav_title);
        actionButton = findViewById(R.id.nav_settings);

        openTasks();
    }

    public void openTasks() {
        String title = "Current Chores";
        fragmentTitle.setText(title);

        actionButton.setImageResource(R.drawable.icon_settings);
        actionButton.setOnClickListener(view -> openSettings());

        TasksFragment newFragment = new TasksFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, newFragment).commit();
    }

    public void openSettings() {
        String title = "Settings";
        fragmentTitle.setText(title);

        actionButton.setImageResource(R.drawable.icon_return);
        actionButton.setOnClickListener(view -> openTasks());

        SettingsFragment newFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, newFragment).commit();
    }

    public void openEditRoom(HomeRoom existingRoom) {
        String title;
        if (existingRoom == null) {
            title = "Add Room";
        } else {
            title = "Edit Room";
        }
        fragmentTitle.setText(title);

        actionButton.setImageResource(R.drawable.icon_return);
        actionButton.setOnClickListener(view -> openSettings());

        EditRoomFragment newFragment = new EditRoomFragment();
        newFragment.setExistingRoom(existingRoom);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, newFragment).commit();
    }

    public void openEditChore(Chore existingChore) {
        String title;
        if (existingChore == null) {
            title = "Add Chore";
        } else {
            title = "Edit Chore";
        }
        fragmentTitle.setText(title);

        actionButton.setImageResource(R.drawable.icon_return);
        actionButton.setOnClickListener(view -> openSettings());

        EditChoreFragment newFragment = new EditChoreFragment();
        newFragment.setExistingChore(existingChore);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, newFragment).commit();
    }
}