package com.paret0x.choretracker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

class TasksFragmentListViewHolder extends RecyclerView.ViewHolder {
    public TextView choreNameView;
    public ImageView choreCompleteView;
    public TextView choreTimeView;

    public TasksFragmentListViewHolder(@NonNull View itemView) {
        super(itemView);

        this.choreNameView = itemView.findViewById(R.id.fragment_active_chore_name);
        this.choreCompleteView = itemView.findViewById(R.id.fragment_active_chore_finish);
        this.choreTimeView = itemView.findViewById(R.id.fragment_active_chore_time);
    }
}

class TasksFragmentListViewAdapter extends RecyclerView.Adapter<TasksFragmentListViewHolder> {
    private int choreNameColor;
    private int choreDateColor;
    private final ChoreStatus status;

    public TasksFragmentListViewAdapter(ChoreStatus status) {
        this.status = status;

        if (status == ChoreStatus.OVERDUE) {
            choreNameColor = R.color.textColor;
            choreDateColor = R.color.redTextColor;
        } else if (status == ChoreStatus.ONGOING) {
            choreNameColor = R.color.textColor;
            choreDateColor = R.color.blueTextColor;
        } else if (status == ChoreStatus.COMPLETED) {
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
        Chore c = Utilities.getInstance().getChoreByStatusIndex(this.status, position);
        String choreName = c.choreName;

        String choreDateText = "";
        TimeDiff diff = Utilities.getInstance().getTimeDiff(c);
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

        holder.choreNameView.setText(choreName);
        holder.choreNameView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), choreNameColor));
        holder.choreCompleteView.setOnClickListener(new TasksFragment.TasksFragmentConfirmClickListener(c));
        holder.choreTimeView.setText(choreDateText);
        holder.choreTimeView.setTextColor(ContextCompat.getColor(holder.choreTimeView.getContext(), choreDateColor));
    }

    @Override
    public int getItemCount() {
        return Utilities.getInstance().getNumOfChoresByStatus(status);
    }
}

public class TasksFragment extends Fragment {
    protected static RecyclerView overdueView;
    protected static RecyclerView activeView;
    protected static RecyclerView completedView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Utilities.getInstance().updateChoreStatuses();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active, container, false);

        LinearLayout overdueParent = view.findViewById(R.id.fragment_active_overdue);
        ((TextView)overdueParent.findViewById(R.id.fragment_active_group_text)).setText("OVERDUE");
        overdueView = overdueParent.findViewById(R.id.fragment_active_group_item_view);
        overdueView.setAdapter(new TasksFragmentListViewAdapter(ChoreStatus.OVERDUE));
        overdueView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        LinearLayout activeParent = view.findViewById(R.id.fragment_active_ongoing);
        ((TextView)activeParent.findViewById(R.id.fragment_active_group_text)).setText("ONGOING");
        activeView = activeParent.findViewById(R.id.fragment_active_group_item_view);
        activeView.setAdapter(new TasksFragmentListViewAdapter(ChoreStatus.ONGOING));
        activeView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        LinearLayout completedParent = view.findViewById(R.id.fragment_active_completed);
        ((TextView)completedParent.findViewById(R.id.fragment_active_group_text)).setText("COMPLETED");
        completedView = completedParent.findViewById(R.id.fragment_active_group_item_view);
        completedView.setAdapter(new TasksFragmentListViewAdapter(ChoreStatus.COMPLETED));
        completedView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    public static class TasksFragmentConfirmClickListener implements View.OnClickListener {
        private final Chore chore;

        public TasksFragmentConfirmClickListener(Chore c) {
            this.chore = c;
        }

        @Override
        public void onClick(View view) {
            TasksFragmentConfirmDialog dialog = new TasksFragmentConfirmDialog(overdueView.getContext(), this.chore);
            dialog.show();
        }
    }

    protected static class TasksFragmentConfirmDialog extends Dialog implements android.view.View.OnClickListener {
        private final Chore chore;

        public TasksFragmentConfirmDialog(@NonNull Context context, Chore c) {
            super(context);
            this.chore = c;
        }

        @Override
        @SuppressLint("SimpleDateFormat")
        public void onClick(View view) {
            SimpleDateFormat formatter = new SimpleDateFormat();
            Date currentDate = Calendar.getInstance().getTime();
            this.chore.dateLastDone = formatter.format(currentDate);

            ChoreStatus status = Utilities.getInstance().getChoreStatus(this.chore.choreId);
            int oldIndex = Utilities.getInstance().getChoreStatusIndex(this.chore.choreId);
            Utilities.getInstance().moveChoreByStatus(this.chore, status, ChoreStatus.COMPLETED);
            int newIndex = Utilities.getInstance().getChoreStatusIndex(this.chore.choreId);

            if (status == ChoreStatus.COMPLETED) {
                Objects.requireNonNull(completedView.getAdapter()).notifyItemRemoved(oldIndex);
            } else if (status == ChoreStatus.ONGOING) {
                Objects.requireNonNull(activeView.getAdapter()).notifyItemRemoved(oldIndex);
            } else if (status == ChoreStatus.OVERDUE) {
                Objects.requireNonNull(overdueView.getAdapter()).notifyItemRemoved(oldIndex);
            }

            Objects.requireNonNull(completedView.getAdapter()).notifyItemInserted(newIndex);
        }
    }
}
