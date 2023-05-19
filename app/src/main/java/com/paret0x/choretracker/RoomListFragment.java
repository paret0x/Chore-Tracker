package com.paret0x.choretracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

class RoomListFragmentListViewHolder extends RecyclerView.ViewHolder {
    public TextView roomNameView;
    public ImageView roomEditIcon;

    public RoomListFragmentListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.roomNameView = itemView.findViewById(R.id.fragment_roomlist_item_text);
        this.roomEditIcon = itemView.findViewById(R.id.fragment_roomlist_item_edit);
    }
}

class RoomListFragmentListViewAdapter extends RecyclerView.Adapter<RoomListFragmentListViewHolder> {
    @NonNull
    @Override
    public RoomListFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_roomlist_item, parent, false);
        return new RoomListFragmentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomListFragmentListViewHolder holder, int position) {
        HomeRoom r = Utilities.getInstance().getRoomAt(position);
        holder.roomNameView.setText(r.roomName);
        holder.roomEditIcon.setOnClickListener(view1 -> {
            RoomListFragmentEditDialog dialog = new RoomListFragmentEditDialog(view1.getContext());
            dialog.setExistingRoom(r);
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return Utilities.getInstance().getNumRooms();
    }
}

class RoomListFragmentEditDialog extends Dialog {
    protected HomeRoom existingRoom = null;

    public RoomListFragmentEditDialog(@NonNull Context context) {
        super(context);
    }

    public void setExistingRoom(HomeRoom room) {
        this.existingRoom = room;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_roomlist_addeditdialog);

        TextView title = findViewById(R.id.fragment_roomlist_editdialog_title);
        EditText roomName = findViewById(R.id.fragment_roomlist_editdialog_name);

        if (existingRoom == null) {
            title.setText("Add Room");
            roomName.setText("");
        } else {
            title.setText("Edit Room");
            roomName.setText(existingRoom.roomName);
        }

        Button saveButton = findViewById(R.id.fragment_roomlist_editdialog_save);
        Button deleteButton = findViewById(R.id.fragment_roomlist_editdialog_delete);
        Button cancelButton = findViewById(R.id.fragment_roomlist_editdialog_cancel);

        saveButton.setOnClickListener(view -> {
            String newName = roomName.getText().toString();
            if (newName.isEmpty()) {
                return;
            }

            RecyclerView roomsView = RoomListFragment.getRecyclerView();

            if (existingRoom == null) {
                int index = Utilities.getInstance().getNumRooms();
                Utilities.getInstance().addRoom(newName);
                Objects.requireNonNull(roomsView.getAdapter()).notifyItemInserted(index);
            } else {
                existingRoom.roomName = newName;
                Utilities.getInstance().updateRoom(existingRoom);
                int index = Utilities.getInstance().getRoomIndex(existingRoom);
                Objects.requireNonNull(roomsView.getAdapter()).notifyItemChanged(index);
            }

            dismiss();
        });

        if (existingRoom == null) {
            deleteButton.setTextColor(ContextCompat.getColor(getContext(), R.color.grayTextColor));
        } else {
            deleteButton.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Are you sure you want to delete room \"" + existingRoom.roomName + "\"?");

                RecyclerView roomsView = RoomListFragment.getRecyclerView();

                builder.setPositiveButton("YES", (dialogInterface, i) -> {
                    int index = Utilities.getInstance().getRoomIndex(existingRoom);
                    Utilities.getInstance().deleteRoom(existingRoom);
                    Objects.requireNonNull(roomsView.getAdapter()).notifyItemRemoved(index);
                    dialogInterface.dismiss();
                    dismiss();
                });

                builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog alert = builder.create();
                alert.show();
            });
        }
        cancelButton.setOnClickListener(view -> dismiss());
    }
}

public class RoomListFragment extends Fragment {
    protected static RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomlist, container, false);

        recyclerView = view.findViewById(R.id.fragment_roomlist_group_view);
        recyclerView.setAdapter(new RoomListFragmentListViewAdapter());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        MaterialButton addButton = view.findViewById(R.id.fragment_roomlist_addicon);
        addButton.setOnClickListener(view1 -> {
            RoomListFragmentEditDialog dialog = new RoomListFragmentEditDialog(view1.getContext());
            dialog.show();
        });

        return view;
    }

    public static RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
