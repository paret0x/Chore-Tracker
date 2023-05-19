package com.paret0x.choretracker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

class ChoreListFragmentChoreListViewHolder extends RecyclerView.ViewHolder {

    public ChoreListFragmentChoreListViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}

class ChoreListFragmentChoreListViewAdapter extends RecyclerView.Adapter<RoomListFragmentListViewHolder> {

    @NonNull
    @Override
    public RoomListFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RoomListFragmentListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

class ChoreListFragmentRoomListViewHolder extends RecyclerView.ViewHolder {

    public ChoreListFragmentRoomListViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}

class ChoreListFragmentRoomListViewAdapter extends RecyclerView.Adapter<RoomListFragmentListViewHolder> {

    @NonNull
    @Override
    public RoomListFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_roomgroup, parent, false);
        return new RoomListFragmentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomListFragmentListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return Utilities.getInstance().getNumRooms();
    }
}

class ChoreListFragmentEditDialog extends Dialog {
    protected Chore existingChore = null;

    public ChoreListFragmentEditDialog(@NonNull Context context) {
        super(context);
    }

    public void setExistingChore(Chore c) {
        this.existingChore = c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_roomlist_addeditdialog);


    }
}

public class ChoreListFragment extends Fragment {
    protected static RecyclerView roomsRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        /*
        roomsRecyclerView = view.findViewById(R.id.fragment_chorelist_roomgroup_view);
        roomsRecyclerView.setAdapter(new ChoreListFragmentRoomListViewAdapter());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        roomsRecyclerView.setLayoutManager(layoutManager);

        MaterialButton addButton = view.findViewById(R.id.fragment_chorelist_addicon);
        addButton.setOnClickListener(view1 -> {
            ChoreListFragmentEditDialog dialog = new ChoreListFragmentEditDialog(view1.getContext());
            dialog.show();
        });
        */
        return view;
    }
}
