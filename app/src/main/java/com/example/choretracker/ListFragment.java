package com.example.choretracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Objects;

class ListFragmentListViewHolder extends RecyclerView.ViewHolder {
    public TextView nameView;
    public TextView weightView;
    public ImageView deleteView;
    public ImageView editView;

    public ListFragmentListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.nameView = itemView.findViewById(R.id.fragment_chorelist_chore_name);
        this.weightView = itemView.findViewById(R.id.fragment_chorelist_chore_weight);
        this.deleteView = itemView.findViewById(R.id.fragment_chorelist_chore_delete);
        this.editView = itemView.findViewById(R.id.fragment_chorelist_chore_edit);
    }
}

class ListFragmentListViewAdapter extends RecyclerView.Adapter<ListFragmentListViewHolder> {
    @NonNull
    @Override
    public ListFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chorelist_item, parent, false);
        return new ListFragmentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListFragmentListViewHolder holder, int position) {
        Chore c = ChoreHelper.getInstance().getChoreAt(position);
        String weightText = "(" + c.choreWeight + ")";
        holder.nameView.setText(c.choreName);
        holder.weightView.setText(weightText);
        holder.deleteView.setOnClickListener(new ListFragment.ListFragmentDeleteClickListener(c.choreId));
        holder.editView.setOnClickListener(new ListFragment.ListFragmentEditClickListener(c.choreId));
    }

    @Override
    public int getItemCount() {
        return ChoreHelper.getInstance().getNumChores();
    }
}

class DividerItemDecorator extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;

    public DividerItemDecorator(Drawable divider) {
        mDivider = divider;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int dividerLeft = parent.getPaddingLeft();
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i <= childCount - 2; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mDivider.draw(canvas);
        }
    }
}

class ListFragmentEditDialog extends Dialog implements android.view.View.OnClickListener {
    private int choreId;
    private RecyclerView rView;

    private EditText choreNameField;

    public ListFragmentEditDialog(@NonNull Context context, int id, RecyclerView recyclerView) {
        super(context);
        this.choreId = id;
        this.rView = recyclerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_chorelist_editdialog);

        Chore c = ChoreHelper.getInstance().getChoreById(choreId);

        TextView editText = findViewById(R.id.fragment_chorelist_edit_text);
        editText.setText("Edit chore");

        TextView nameText = findViewById(R.id.fragment_chorelist_editname_text);
        nameText.setText("Chore Name");

        choreNameField = findViewById(R.id.fragment_chorelist_editname_field);
        choreNameField.setText(c.choreName);

        TextView yesView = findViewById(R.id.fragment_chorelist_yes);
        yesView.setText("Save");
        yesView.setOnClickListener(this);

        TextView noView = findViewById(R.id.fragment_chorelist_no);
        noView.setText("Cancel");
        noView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fragment_chorelist_yes) {
            int index = ChoreHelper.getInstance().getChoreIndex(choreId);
            Chore c = ChoreHelper.getInstance().getChoreAt(index);

            String newName = choreNameField.getText().toString();
            if (newName.length() < 3) {
                return;
            }

            c.choreName = newName;
            ChoreHelper.getInstance().updateChore(c);
            Objects.requireNonNull(rView.getAdapter()).notifyItemChanged(index);
        }
        dismiss();
    }
}

public class ListFragment extends Fragment {
    protected static RecyclerView recyclerView;
    protected static EditText textBox;
    protected static ImageView addIcon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chorelist, container, false);

        recyclerView = view.findViewById(R.id.fragment_chorelist_item_view);
        recyclerView.setAdapter(new ListFragmentListViewAdapter());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecorator(ContextCompat.getDrawable(getContext(), R.drawable.divider)));

        addIcon = view.findViewById(R.id.fragment_chorelist_new_chore_add);
        addIcon.setOnClickListener(new ListFragmentAddClickListener());

        textBox = view.findViewById(R.id.fragment_chorelist_new_chore_name);
        textBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addIcon.performClick();
                return true;
            }
            return false;
        });

        return view;
    }

    public static class ListFragmentEditClickListener implements View.OnClickListener {
        private final int choreId;

        public ListFragmentEditClickListener(int id) {
            this.choreId = id;
        }

        @Override
        public void onClick(@NonNull View view) {
            ListFragmentEditDialog dialog = new ListFragmentEditDialog(recyclerView.getContext(), choreId, recyclerView);
            dialog.show();
        }
    }

    public static class ListFragmentAddClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String choreName = textBox.getText().toString();

            if (choreName.length() < 3) {
                return;
            }

            int index = ChoreHelper.getInstance().getNumChores();
            ChoreHelper.getInstance().addChore(choreName);
            Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(index);
            textBox.setText("");
        }
    }

    public static class ListFragmentDeleteClickListener implements View.OnClickListener {
        private final int choreId;

        public ListFragmentDeleteClickListener(int id) {
            this.choreId = id;
        }

        @Override
        public void onClick(@NonNull View view) {
            int index = ChoreHelper.getInstance().getChoreIndex(choreId);
            Chore c = ChoreHelper.getInstance().getChoreAt(index);
            String choreName = c.choreName;

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Are you sure you want to delete chore \"" + choreName + "\"?");

            builder.setPositiveButton("YES", (dialogInterface, i) -> {
                ChoreHelper.getInstance().deleteChore(c);
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRemoved(index);
                dialogInterface.dismiss();
            });

            builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
