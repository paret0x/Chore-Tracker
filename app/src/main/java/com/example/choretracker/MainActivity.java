package com.example.choretracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends FragmentActivity {
    private int currentFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Init database
        ChoreHelper.getInstance().initDatabase(this);

        // Set main content view
        setContentView(R.layout.activity_main);

        // Create bottom navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            // Change to default fragment
            runOnUiThread(() -> navView.setSelectedItemId(R.id.navigation_tasks));
        } else {
            // App refreshed (dark mode?). Load last loaded fragment
            int newFrag = savedInstanceState.getInt("Frag");

            if (newFrag == 1) {
                switchFragments(R.id.navigation_tasks);
            } else if (newFrag == 2) {
                switchFragments(R.id.navigation_update);
            } else if (newFrag == 3) {
                switchFragments(R.id.navigation_list);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("Frag", currentFrag);
    }

    public void switchFragments(int newFragmentId) {
        Fragment selectedFragment;
        String fragmentTitle;

        if (newFragmentId == R.id.navigation_tasks) {
            selectedFragment = new TasksFragment();
            currentFrag = 1;
            fragmentTitle = "Current Chores";
        } else if (newFragmentId == R.id.navigation_update) {
            selectedFragment = new UpdateFragment();
            currentFrag = 2;
            fragmentTitle = "Update Current Chores";
        } else if (newFragmentId == R.id.navigation_list) {
            selectedFragment = new ListFragment();
            currentFrag = 3;
            fragmentTitle = "Chore List";
        } else {
            return;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_activity_main, selectedFragment).commit();
        ((TextView)this.findViewById(R.id.nav_title)).setText(fragmentTitle);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        switchFragments(item.getItemId());
        return true;
    };
}