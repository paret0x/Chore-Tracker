package com.paret0x.choretracker;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {
    private ImageView actionButton;
    private TextView fragmentTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentTitle = findViewById(R.id.nav_title);
        actionButton = findViewById(R.id.nav_settings);

        Utilities.getInstance().populateUtilities(this);

        closeSettings();
    }

    public void openSettings() {
        String title = "Settings";
        fragmentTitle.setText(title);

        actionButton.setImageResource(R.drawable.icon_return);
        actionButton.setOnClickListener(view -> closeSettings());

        Fragment newFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, newFragment).commit();
    }

    public void closeSettings() {
        String title = "Current Chores";
        fragmentTitle.setText(title);

        actionButton.setImageResource(R.drawable.icon_settings);
        actionButton.setOnClickListener(view -> openSettings());

        Fragment newFragment = new TasksFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, newFragment).commit();
    }
}