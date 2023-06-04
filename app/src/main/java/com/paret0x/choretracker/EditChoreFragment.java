package com.paret0x.choretracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Date;

public class EditChoreFragment extends Fragment {
    protected Chore existingChore = null;
    private static FragmentChanger fragmentChanger;

    private EditText nameField;
    private Spinner roomChoices;
    private NumberPicker monthFrequency;
    private NumberPicker weekFrequency;
    private NumberPicker dayFrequency;
    private EditText dateLastDoneText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editchore, container, false);

        fragmentChanger = (FragmentChanger)getActivity();

        nameField = view.findViewById(R.id.fragment_editchore_name_edit);
        if (existingChore != null) {
            nameField.setText(existingChore.choreName);
        }

        roomChoices = view.findViewById(R.id.fragment_editchore_room_dropdown);
        ArrayList<String> roomNames = Utilities.getInstance().getRoomNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roomNames);
        roomChoices.setAdapter(adapter);

        if ((existingChore != null) && (existingChore.roomId != Utilities.getInstance().unassignedRoomId)) {
            String roomName = Utilities.getInstance().getRoomNameById(existingChore.roomId);
            if (roomName != null) {
                roomChoices.setSelection(roomNames.indexOf(roomName));
            }
        }

        monthFrequency = view.findViewById(R.id.fragment_editchore_freq_months);
        monthFrequency.setMinValue(0);
        monthFrequency.setMaxValue(12);

        weekFrequency = view.findViewById(R.id.fragment_editchore_freq_weeks);
        weekFrequency.setMinValue(0);
        weekFrequency.setMaxValue(4);

        dayFrequency = view.findViewById(R.id.fragment_editchore_freq_days);
        dayFrequency.setMinValue(0);
        dayFrequency.setMaxValue(7);

        if ((existingChore != null) && (existingChore.frequency != 0L)) {
            TimeDiff frequency = Utilities.getInstance().getTimeDiff(existingChore.frequency);
            monthFrequency.setValue(frequency.months);
            weekFrequency.setValue(frequency.weeks);
            dayFrequency.setValue(frequency.days);
        }

        dateLastDoneText = view.findViewById(R.id.fragment_editchore_date_edit);

        ImageView calendarButton = view.findViewById(R.id.fragment_editchore_date_button);
        calendarButton.setOnClickListener(calendarButtonListener);

        if (existingChore != null) {
            dateLastDoneText.setText(existingChore.dateLastDone);
        }

        Button saveButton = view.findViewById(R.id.fragment_editchore_save);
        Button deleteButton = view.findViewById(R.id.fragment_editchore_delete);

        saveButton.setOnClickListener(saveButtonListener);

        if (existingChore == null) {
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
                Toast toast = Toast.makeText(getContext(), "Chore missing name", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            String newRoom = roomChoices.getSelectedItem().toString();
            int newRoomId = Utilities.getInstance().getRoomIdByName(newRoom);

            int months = monthFrequency.getValue();
            int weeks = weekFrequency.getValue();
            int days = dayFrequency.getValue();
            long newFrequency = (months * 30L) + (weeks * 7L) + days;
            if (newFrequency == 0L) {
                Toast toast = Toast.makeText(getContext(), "Chore missing frequency", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            String dateLastDoneStr = dateLastDoneText.getText().toString();
            LocalDate dateLastDone;
            try {
                dateLastDone = LocalDate.parse(dateLastDoneStr);
            } catch (DateTimeParseException e) {
                Toast toast = Toast.makeText(getContext(), "Chore missing last done date", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (existingChore == null) {
                int choreId = Utilities.getInstance().getNextChoreId();
                existingChore = new Chore(choreId, newName, newRoomId, newFrequency, dateLastDone.toString());
                Utilities.getInstance().addChore(existingChore);
            } else {
                existingChore.choreName = newName;
                existingChore.roomId = newRoomId;
                existingChore.frequency = newFrequency;
                existingChore.dateLastDone = dateLastDone.toString();

                Utilities.getInstance().updateChore(existingChore);
            }

            Toast toast = Toast.makeText(EditChoreFragment.this.getContext(), "Saved", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    private final View.OnClickListener deleteButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String choreName = existingChore.choreName;

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Are you sure you want to delete chore \"" + choreName + "\"?");

            builder.setPositiveButton("YES", (dialogInterface, i) -> {
                Utilities.getInstance().deleteChore(existingChore);
                dialogInterface.dismiss();

                Toast toast = Toast.makeText(getContext(), "Deleted chore " + choreName, Toast.LENGTH_SHORT);
                toast.show();

                fragmentChanger.openSettings();
            });

            builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private final View.OnClickListener calendarButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LocalDate selectedDate;
            if (existingChore == null) {
                selectedDate = LocalDate.now();
            } else {
                selectedDate = LocalDate.parse(existingChore.dateLastDone);
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),  (view1, year, monthOfYear, dayOfMonth) -> {
                LocalDate newDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                dateLastDoneText.setText(newDate.toString());
            }, selectedDate.getYear(), selectedDate.getMonthValue(), selectedDate.getDayOfMonth());
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialog.getDatePicker().updateDate(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            datePickerDialog.show();
        }
    };

    public void setExistingChore(Chore chore) {
        this.existingChore = chore;
    }
}
