package com.example.choretracker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

class UpdateFragmentListViewHolder extends RecyclerView.ViewHolder {
    public CheckBox checkboxView;

    public TextView weightText;
    public Spinner weightField;
    public TextView assignText;
    public Spinner assignField;

    public UpdateFragmentListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.checkboxView = itemView.findViewById(R.id.fragment_update_chore_added);

        if (itemView.findViewById(R.id.fragment_update_editweight_text) != null) {
            this.weightText = itemView.findViewById(R.id.fragment_update_editweight_text);
            this.weightField = itemView.findViewById(R.id.fragment_update_editweight_field);
            this.assignText = itemView.findViewById(R.id.fragment_update_editassign_text);
            this.assignField = itemView.findViewById(R.id.fragment_update_editassign_field);
        }
    }
}

class UpdateFragmentListAdapter extends RecyclerView.Adapter<UpdateFragmentListViewHolder> {
    private final boolean isActiveList;

    public UpdateFragmentListAdapter(boolean isActive) {
        this.isActiveList = isActive;
    }

    @NonNull
    @Override
    public UpdateFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (this.isActiveList) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_update_item_active, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_update_item_inactive, parent, false);
        }

        return new UpdateFragmentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateFragmentListViewHolder holder, int position) {
        Chore c;
        if (this.isActiveList) {
            c = OngoingHolder.getInstance().ongoingList.get(position);

            holder.checkboxView.setChecked(true);
            holder.weightText.setText("Weight:");
            holder.assignText.setText("Assigned to:");

            String[] weightItems = new String[]{"XS", "S", "M", "L", "XL"};
            holder.weightField.setAdapter(new ArrayAdapter<>(holder.weightField.getContext(), android.R.layout.simple_spinner_item, weightItems));
            holder.weightField.setSelection(Arrays.asList(weightItems).indexOf(c.choreWeight));
            holder.weightField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    c.choreWeight = weightItems[i];
                    ChoreHelper.getInstance().updateChore(c);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            String[] assignItems = new String[]{"Unassigned", "User1", "User2"};
            holder.assignField.setAdapter(new ArrayAdapter<>(holder.assignField.getContext(), android.R.layout.simple_spinner_dropdown_item, assignItems));
            int index = Arrays.asList(assignItems).indexOf(c.assignedTo);
            if (index == -1) {
                index = 0;
            }
            holder.assignField.setSelection(index);
            holder.assignField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String newAssign = "";
                    if (i > 0) {
                        newAssign = assignItems[i];
                    }
                    c.assignedTo = newAssign;
                    ChoreHelper.getInstance().updateChore(c);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

        } else {
            c = OngoingHolder.getInstance().inactiveList.get(position);
            holder.checkboxView.setChecked(false);
        }
        String choreName = c.choreName;

        holder.checkboxView.setText(choreName);
        holder.checkboxView.setOnClickListener(new UpdateFragment.UpdateFragmentCheckboxClickListener(c.choreId));
    }

    @Override
    public int getItemCount() {
        if (this.isActiveList) {
            return OngoingHolder.getInstance().ongoingList.size();
        } else {
            return OngoingHolder.getInstance().inactiveList.size();
        }
    }
}

public class UpdateFragment extends Fragment {
    protected static RecyclerView goingIn;
    protected static RecyclerView goingOut;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ChoreHelper.getInstance().populateOngoingHolder();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update, container, false);

        LinearLayout linearIn = view.findViewById(R.id.fragment_update_yes);
        ((TextView)linearIn.findViewById(R.id.fragment_update_group_text)).setText("Going in");
        goingIn = linearIn.findViewById(R.id.fragment_update_group_item_view);
        goingIn.setAdapter(new UpdateFragmentListAdapter(true));
        goingIn.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        goingIn.addItemDecoration(new DividerItemDecorator(ContextCompat.getDrawable(getContext(), R.drawable.divider)));

        LinearLayout linearOut = view.findViewById(R.id.fragment_update_no);
        ((TextView)linearOut.findViewById(R.id.fragment_update_group_text)).setText("Going out");
        goingOut = linearOut.findViewById(R.id.fragment_update_group_item_view);
        goingOut.setAdapter(new UpdateFragmentListAdapter(false));
        goingOut.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        ((Button)view.findViewById(R.id.fragment_update_clear)).setOnClickListener(new UpdateFragmentClearListener());
        ((Button)view.findViewById(R.id.fragment_update_save)).setOnClickListener(new UpdateFragmentSaveListener());

        return view;
    }

    public static class UpdateFragmentClearListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int ongoingSize = OngoingHolder.getInstance().ongoingList.size();
            int inactiveSize = OngoingHolder.getInstance().inactiveList.size();
            OngoingHolder.getInstance().inactiveList.addAll(OngoingHolder.getInstance().ongoingList);
            OngoingHolder.getInstance().ongoingList.clear();

            Objects.requireNonNull(goingIn.getAdapter()).notifyItemRangeRemoved(0, ongoingSize);
            Objects.requireNonNull(goingOut.getAdapter()).notifyItemRangeInserted(inactiveSize, OngoingHolder.getInstance().inactiveList.size());
        }
    }

    public static class UpdateFragmentSaveListener implements View.OnClickListener {
        @Override
        public void onClick(@NonNull View view) {
            for (Chore c : OngoingHolder.getInstance().ongoingList) {
                if (Objects.equals(c.dateTimeOpened, "")) {
                    SimpleDateFormat formatter = new SimpleDateFormat();
                    Date currentDate = Calendar.getInstance().getTime();
                    c.dateTimeOpened = formatter.format(currentDate);
                }
                c.isCompleted = false;
                ChoreHelper.getInstance().updateChore(c);
            }

            for (Chore c : OngoingHolder.getInstance().inactiveList) {
                c.dateTimeOpened = "";
                c.isCompleted = false;
                ChoreHelper.getInstance().updateChore(c);
            }

            Toast.makeText(view.getContext(), "Saving chores", Toast.LENGTH_SHORT).show();
        }
    }

    public static class UpdateFragmentCheckboxClickListener implements View.OnClickListener {
        private final int choreId;

        public UpdateFragmentCheckboxClickListener(int i) {
            this.choreId = i;
        }

        @Override
        public void onClick(View view) {
            int index = ChoreHelper.getInstance().getOngoingHolderIndex(this.choreId);
            Chore c = ChoreHelper.getInstance().getChoreById(this.choreId);

            if (OngoingHolder.getInstance().ongoingList.contains(c)) {
                OngoingHolder.getInstance().ongoingList.remove(c);
                OngoingHolder.getInstance().inactiveList.add(0,c);
                Objects.requireNonNull(goingIn.getAdapter()).notifyItemRemoved(index);
                Objects.requireNonNull(goingOut.getAdapter()).notifyItemInserted(0);
            } else {
                OngoingHolder.getInstance().inactiveList.remove(c);
                OngoingHolder.getInstance().ongoingList.add(0,c);
                Objects.requireNonNull(goingOut.getAdapter()).notifyItemRemoved(index);
                Objects.requireNonNull(goingIn.getAdapter()).notifyItemInserted(0);
            }
        }
    }
}
