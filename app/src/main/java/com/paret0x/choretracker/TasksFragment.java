package com.paret0x.choretracker;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final int choreNameColor;
    private final int choreDateColor;
    private final ChoreStatus status;

    public TasksFragmentListViewAdapter(ChoreStatus status) {
        this.status = status;

        choreNameColor = R.color.itemTextColor;

        if (status == ChoreStatus.ONGOING) {
            choreDateColor = R.color.headerTextColor;
        } else if (status == ChoreStatus.OVERDUE) {
            choreDateColor = R.color.redTextColor;
        } else {
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

        TimeDiff diff = Utilities.getInstance().getTimeDiffForChore(c);
        if (diff == null) {
            return;
        }

        String choreDateText = "";
        if (diff.months > 0) {
            choreDateText = diff.months + "m ";
        }

        if (diff.weeks > 0) {
            choreDateText += diff.weeks + "w ";
        }

        if (diff.days > 0) {
            choreDateText += diff.days + "d";
        }

        if ((diff.months == 0) && (diff.weeks == 0) && (diff.days == 0)) {
            choreDateText = "0d";
        }

        holder.choreNameView.setText(choreName);
        holder.choreNameView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), choreNameColor));
        holder.choreCompleteView.setOnClickListener(view -> {
            TasksFragment.TasksFragmentConfirmDialog dialog = new TasksFragment.TasksFragmentConfirmDialog(holder.choreNameView.getContext(), c);
            dialog.show();
        });
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
    public boolean wasDatabaseReadyOnLoad;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active, container, false);

        wasDatabaseReadyOnLoad = Utilities.getInstance().hasDatabaseLoaded;
        Log.e(this.getClass().getSimpleName(), "Has database loaded at time of create view? " + wasDatabaseReadyOnLoad);
        Utilities.getInstance().updateChoreStatuses();

        LinearLayout overdueParent = view.findViewById(R.id.fragment_active_overdue);
        ((TextView)overdueParent.findViewById(R.id.fragment_active_group_text)).setText(R.string.overdue);
        overdueView = overdueParent.findViewById(R.id.fragment_active_group_item_view);
        overdueView.setAdapter(new TasksFragmentListViewAdapter(ChoreStatus.OVERDUE));
        overdueView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        LinearLayout activeParent = view.findViewById(R.id.fragment_active_ongoing);
        ((TextView)activeParent.findViewById(R.id.fragment_active_group_text)).setText(R.string.to_do);
        activeView = activeParent.findViewById(R.id.fragment_active_group_item_view);
        activeView.setAdapter(new TasksFragmentListViewAdapter(ChoreStatus.ONGOING));
        activeView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        LinearLayout completedParent = view.findViewById(R.id.fragment_active_completed);
        ((TextView)completedParent.findViewById(R.id.fragment_active_group_text)).setText(R.string.done);
        completedView = completedParent.findViewById(R.id.fragment_active_group_item_view);
        completedView.setAdapter(new TasksFragmentListViewAdapter(ChoreStatus.COMPLETED));
        completedView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (!wasDatabaseReadyOnLoad) {
            reloadViews();
        }
        return view;
    }

    public void reloadViews() {
        Log.i(this.getClass().getSimpleName(), "Scheduling reload");
        Handler handler = new Handler();
        handler.postDelayed(() -> requireActivity().runOnUiThread(() -> {
            Log.i(this.getClass().getSimpleName(), "Reloading views");
            Utilities.getInstance().updateChoreStatuses();
            ArrayList<Chore> overdueChores = Utilities.getInstance().getChoresByStatus(ChoreStatus.OVERDUE);
            ArrayList<Chore> activeChores = Utilities.getInstance().getChoresByStatus(ChoreStatus.ONGOING);
            ArrayList<Chore> completedChores = Utilities.getInstance().getChoresByStatus(ChoreStatus.COMPLETED);

            for (int i = 0; i < overdueChores.size(); i++) {
                Objects.requireNonNull(overdueView.getAdapter()).notifyItemInserted(i);
            }

            for (int i = 0; i < activeChores.size(); i++) {
                Objects.requireNonNull(activeView.getAdapter()).notifyItemInserted(i);
            }

            for (int i = 0; i < completedChores.size(); i++) {
                Objects.requireNonNull(completedView.getAdapter()).notifyItemInserted(i);
            }
        }), 200L);
    }

    protected static class TasksFragmentConfirmDialog {
        private final Chore chore;
        private final Context context;

        public TasksFragmentConfirmDialog(Context context, Chore c) {
            this.chore = c;
            this.context = context;
        }

        public void show() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Has the task \"" + chore.choreName + "\" been completed?");

            builder.setPositiveButton("YES", (dialogInterface, i) -> {
                this.chore.dateLastDone = LocalDate.now().toString();

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
                dialogInterface.dismiss();
            });

            builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
