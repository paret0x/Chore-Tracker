package com.paret0x.choretracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class EditRoomFragment extends Fragment {
    protected HomeRoom existingRoom = null;
    private static FragmentChanger fragmentChanger;
    private EditText nameField;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editroom, container, false);

        fragmentChanger = (FragmentChanger)getActivity();

        nameField = view.findViewById(R.id.fragment_editroom_name_edit);
        if (existingRoom != null) {
            nameField.setText(existingRoom.roomName);
        }

        Button saveButton = view.findViewById(R.id.fragment_editroom_save);
        Button deleteButton = view.findViewById(R.id.fragment_editroom_delete);

        saveButton.setOnClickListener(saveButtonListener);

        if (existingRoom == null) {
            deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        } else {
            deleteButton.setOnClickListener(deleteButtonListener);
        }

        return view;
    }

    private final View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String newName = nameField.getText().toString();
            if (newName.isEmpty()) {
                Toast toast = Toast.makeText(getContext(), "Room missing name", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (existingRoom == null) {
                Utilities.getInstance().addRoom(newName);
            } else {
                existingRoom.roomName = newName;
                Utilities.getInstance().updateRoom(existingRoom);
            }

            Toast toast = Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    private final View.OnClickListener deleteButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String roomName = existingRoom.roomName;

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Are you sure you want to delete room \"" + roomName + "\"?");

            builder.setPositiveButton("YES", (dialogInterface, i) -> {
                Utilities.getInstance().deleteRoom(existingRoom);
                dialogInterface.dismiss();

                Toast toast = Toast.makeText(getContext(), "Deleted room " + roomName, Toast.LENGTH_SHORT);
                toast.show();

                fragmentChanger.openSettings();
            });

            builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    public void setExistingRoom(HomeRoom room) {
        this.existingRoom = room;
    }
}
