package com.paret0x.choretracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class SettingsFragmentChoreListViewHolder extends RecyclerView.ViewHolder {
    public TextView choreNameView;
    public ImageView choreEditIcon;

    public SettingsFragmentChoreListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.choreNameView = itemView.findViewById(R.id.fragment_settings_chore_name);
        this.choreEditIcon = itemView.findViewById(R.id.fragment_settings_chore_edit);
    }
}

class SettingsFragmentChoreListViewAdapter extends RecyclerView.Adapter<SettingsFragmentChoreListViewHolder> {
    private final ArrayList<Chore> choresInRoom;

    public SettingsFragmentChoreListViewAdapter(ArrayList<Chore> chores) {
        this.choresInRoom = chores;
    }

    @NonNull
    @Override
    public SettingsFragmentChoreListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_settings_item, parent, false);
        return new SettingsFragmentChoreListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsFragmentChoreListViewHolder holder, int position) {
        Chore c = choresInRoom.get(position);
        holder.choreNameView.setText(c.choreName);
        holder.choreEditIcon.setOnClickListener(view1 -> SettingsFragment.openEditChore(c));
    }

    @Override
    public int getItemCount() {
        return choresInRoom.size();
    }
}

class SettingsFragmentRoomListViewHolder extends RecyclerView.ViewHolder {
    public TextView roomNameView;
    public ImageView roomEditIcon;
    public RecyclerView roomChoresGroup;
    public Context roomContext;

    public SettingsFragmentRoomListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.roomNameView = itemView.findViewById(R.id.fragment_settings_roomgroup_text);
        this.roomEditIcon = itemView.findViewById(R.id.fragment_settings_roomgroup_edit);
        this.roomChoresGroup = itemView.findViewById(R.id.fragment_settings_roomgroup_item_view);
        this.roomContext = itemView.getContext();
    }
}

class SettingsFragmentRoomListViewAdapter extends RecyclerView.Adapter<SettingsFragmentRoomListViewHolder> {
    @NonNull
    @Override
    public SettingsFragmentRoomListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_settings_group, parent, false);
        return new SettingsFragmentRoomListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsFragmentRoomListViewHolder holder, int position) {
        HomeRoom r = Utilities.getInstance().getRoomAt(position);
        holder.roomNameView.setText(r.roomName);
        holder.roomEditIcon.setOnClickListener(view1 -> SettingsFragment.openEditRoom(r));

        ArrayList<Chore> choresInRoom = Utilities.getInstance().getChoresByRoom(r.roomId);
        holder.roomChoresGroup.setAdapter(new SettingsFragmentChoreListViewAdapter(choresInRoom));
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.roomContext, LinearLayoutManager.VERTICAL, false);
        holder.roomChoresGroup.setLayoutManager(layoutManager);
    }

    @Override
    public int getItemCount() {
        return Utilities.getInstance().getNumRooms();
    }
}

public class SettingsFragment extends Fragment {
    private static FragmentChanger fragmentChanger;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Utilities.getInstance().updateChoresByRoom();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        fragmentChanger = (FragmentChanger)getActivity();

        RecyclerView recyclerView = view.findViewById(R.id.fragment_settings_roomgroup_view);
        recyclerView.setAdapter(new SettingsFragmentRoomListViewAdapter());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        Button addRoomButton = view.findViewById(R.id.fragment_settings_addroombutton);
        addRoomButton.setOnClickListener(view1 -> SettingsFragment.openEditRoom(null));

        Button addChoreButton = view.findViewById(R.id.fragment_settings_addchorebutton);
        addChoreButton.setOnClickListener(view1 -> SettingsFragment.openEditChore(null));

        return view;
    }

    public static void openEditRoom(HomeRoom existingRoom) {
        fragmentChanger.openEditRoom(existingRoom);
    }

    public static void openEditChore(Chore existingChore) {
        fragmentChanger.openEditChore(existingChore);
    }
}
