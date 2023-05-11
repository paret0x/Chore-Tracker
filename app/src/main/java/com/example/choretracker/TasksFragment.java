package com.example.choretracker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

class TasksFragmentListViewHolder extends RecyclerView.ViewHolder {
    public CheckBox checkboxView;
    public TextView choreTimeView;

    public TasksFragmentListViewHolder(@NonNull View itemView) {
        super(itemView);

        this.checkboxView = itemView.findViewById(R.id.fragment_active_chore_completed);
        this.choreTimeView = itemView.findViewById(R.id.fragment_active_chore_time);
    }
}

class TasksFragmentListViewAdapter extends RecyclerView.Adapter<TasksFragmentListViewHolder> {
    private int choreNameColor;
    private int choreDateColor;
    private final ChoreType type;

    public TasksFragmentListViewAdapter(ChoreType type) {
        this.type = type;

        if (type == ChoreType.OVERDUE) {
            choreNameColor = R.color.textColor;
            choreDateColor = R.color.redTextColor;
        } else if (type == ChoreType.ONGOING) {
            choreNameColor = R.color.textColor;
            choreDateColor = R.color.blueTextColor;
        } else if (type == ChoreType.COMPLETED) {
            choreNameColor = R.color.grayTextColor;
            choreDateColor = R.color.gray;
        }
    }

    @NonNull
    @Override
    public TasksFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_active_item, parent, false);
        return new TasksFragmentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TasksFragmentListViewHolder holder, int position) {
        Chore c = Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(type)).get(position);
        String choreName = c.choreName;

        String choreDateText = "";
        if (this.type != ChoreType.COMPLETED) {
            ChoreDiff diff = ChoreHelper.getInstance().getDiff(c);
            if (diff == null) {
                return;
            }

            if (diff.weeks > 0) {
                choreDateText = diff.weeks + "w";
            } else if (diff.days > 0) {
                choreDateText = diff.days + "d";
            } else {
                choreDateText = diff.hours + "h";
            }

            if (c.assignedTo.length() > 0) {
                choreDateText = "(" + c.assignedTo.charAt(0) + ")  " + choreDateText;
            }
        }

        holder.checkboxView.setText(choreName);
        holder.checkboxView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), choreNameColor));
        holder.checkboxView.setOnClickListener(new TasksFragment.TasksFragmentCheckboxListener(c.choreId));
        holder.choreTimeView.setText(choreDateText);
        holder.choreTimeView.setTextColor(ContextCompat.getColor(holder.choreTimeView.getContext(), choreDateColor));

        if (this.type == ChoreType.COMPLETED) {
            holder.checkboxView.setChecked(true);
            holder.checkboxView.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.choreTimeView.getContext(), R.color.gray)));
        } else {
            holder.checkboxView.setChecked(false);
            holder.checkboxView.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.choreTimeView.getContext(), R.color.checkboxColor)));
        }
    }

    @Override
    public int getItemCount() {
        return Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(type)).size();
    }
}

public class TasksFragment extends Fragment {
    protected static RecyclerView overdueView;
    protected static RecyclerView activeView;
    protected static RecyclerView completedView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ChoreHelper.getInstance().populateChoreHolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active, container, false);

        LinearLayout overdueParent = view.findViewById(R.id.fragment_active_overdue);
        ((TextView)overdueParent.findViewById(R.id.fragment_active_group_text)).setText("OVERDUE");
        overdueView = overdueParent.findViewById(R.id.fragment_active_group_item_view);
        overdueView.setAdapter(new TasksFragmentListViewAdapter(ChoreType.OVERDUE));
        overdueView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        LinearLayout activeParent = view.findViewById(R.id.fragment_active_ongoing);
        ((TextView)activeParent.findViewById(R.id.fragment_active_group_text)).setText("ONGOING");
        activeView = activeParent.findViewById(R.id.fragment_active_group_item_view);
        activeView.setAdapter(new TasksFragmentListViewAdapter(ChoreType.ONGOING));
        activeView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        LinearLayout completedParent = view.findViewById(R.id.fragment_active_completed);
        ((TextView)completedParent.findViewById(R.id.fragment_active_group_text)).setText("COMPLETED");
        completedView = completedParent.findViewById(R.id.fragment_active_group_item_view);
        completedView.setAdapter(new TasksFragmentListViewAdapter(ChoreType.COMPLETED));
        completedView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    public static class TasksFragmentCheckboxListener implements View.OnClickListener {
        private final int choreId;

        public TasksFragmentCheckboxListener(int i) {
            this.choreId = i;
        }

        @Override
        public void onClick(View view) {
            ChoreType type = ChoreHelper.getInstance().getChoreHolderType(this.choreId);
            int index = ChoreHelper.getInstance().getChoreHolderIndex(this.choreId);
            Chore c = Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(type)).get(index);

            c.isCompleted = !(type == ChoreType.COMPLETED);
            ChoreType newType = ChoreHelper.getInstance().getTypeOfChore(c);
            Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(type)).remove(c);
            Objects.requireNonNull(ChoreHolder.getInstance().choreLists.get(newType)).add(0, c);
            ChoreHelper.getInstance().updateChore(c);

            if (type == ChoreType.COMPLETED) {
                Objects.requireNonNull(completedView.getAdapter()).notifyItemRemoved(index);
            } else if (type == ChoreType.ONGOING) {
                Objects.requireNonNull(activeView.getAdapter()).notifyItemRemoved(index);
            } else if (type == ChoreType.OVERDUE) {
                Objects.requireNonNull(overdueView.getAdapter()).notifyItemRemoved(index);
            }

            if (newType == ChoreType.COMPLETED) {
                Objects.requireNonNull(completedView.getAdapter()).notifyItemInserted(0);
            } else if (newType == ChoreType.ONGOING) {
                Objects.requireNonNull(activeView.getAdapter()).notifyItemInserted(0);
            } else if (newType == ChoreType.OVERDUE) {
                Objects.requireNonNull(overdueView.getAdapter()).notifyItemInserted(0);
            }
        }
    }
}
