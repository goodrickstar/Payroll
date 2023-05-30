package com.glass.payroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentLoadsBinding;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

public class FragmentLoads extends Fragment implements View.OnClickListener {
    private Context context;
    private MI MI;
    private FragmentLoadsBinding binding;
    private Settlement settlement;

    private MainViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentLoads() {
    }

    private void calculate() {
        int miles = 0;
        int totalCost = 0;
        for (Load x : settlement.getLoads()) {
            totalCost += x.getRate();
            miles += x.getEmpty();
            miles += x.getLoaded();
        }
        if (totalCost != 0)
            binding.total.setText("Total: $" + formatInt(totalCost) + " (" + formatInt(miles) + " miles @ " + Utils.formatValueToCurrency((double) totalCost / miles) + ")");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoadsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        final RecycleAdapter recyclerAdapter = new RecycleAdapter();
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.recycler.setAdapter(recyclerAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(binding.recycler);
        binding.addButton.setOnClickListener(this);
        SwitchCompat order = v.findViewById(R.id.order);
        SwitchCompat sort = v.findViewById(R.id.sort);
        TextView sortView = v.findViewById(R.id.sort_view);
        order.setChecked(Utils.getOrder(context, "loads"));
        sort.setChecked(Utils.getSort(context, "loads"));
        order.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) button.setText(getString(R.string.asc));
            else button.setText(getString(R.string.desc));
            MI.vibrate();
            model.add(Utils.sortLoads(settlement, order.isChecked(), sort.isChecked()));
            Utils.setOrder(context, "loads", checked);
        });
        sort.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) sortView.setText(getString(R.string.date));
            else sortView.setText(getString(R.string.revenue));
            MI.vibrate();
            model.add(Utils.sortLoads(settlement, order.isChecked(), sort.isChecked()));
            Utils.setSort(context, "loads", checked);
        });
        if (!order.isChecked()) order.setText(getString(R.string.asc));
        else order.setText(getString(R.string.desc));
        if (!sort.isChecked()) sortView.setText(getString(R.string.date));
        else sortView.setText(getString(R.string.revenue));
        model.settlementLiveData().observe(getViewLifecycleOwner(), settlement -> {
            FragmentLoads.this.settlement = settlement;
            recyclerAdapter.notifyDataSetChanged();
            calculate();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        MI = (MI) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    @Override
    public void onClick(View view) {
        if (MI != null) {
            MI.newLoad(null, 0);
            MI.vibrate();
        }
    }

    private String formatInt(int count) {
        return NumberFormat.getNumberInstance(Locale.US).format(count);
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.load_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder holder, int position) {
            Load row = settlement.getLoads().get(position);
            holder.location.setText(row.getFrom() + " - " + row.getTo());
            holder.rate.setText("$" + row.getRate());
            holder.from.setText(Utils.range(row.getStart(), row.getStop()));
            int miles = row.getEmpty() + row.getLoaded();
            holder.miles.setText(formatInt(miles) + " miles @ " + Utils.formatValueToCurrency((double) row.getRate() / miles));
            holder.note.setText(row.getNote());
            if (TextUtils.isEmpty(row.getNote())) holder.note.setVisibility(View.GONE);
            else holder.note.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return settlement.getLoads().size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView from;
            final TextView miles;
            final TextView note;
            final TextView rate;
            final TextView location;

            viewHolder(View itemView) {
                super(itemView);
                location = itemView.findViewById(R.id.location);
                from = itemView.findViewById(R.id.date);
                rate = itemView.findViewById(R.id.cost);
                miles = itemView.findViewById(R.id.gallons);
                note = itemView.findViewById(R.id.optional_note);
            }
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private Drawable icon;
        //private final ColorDrawable background;

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            //background = new ColorDrawable(getResources().getColor(R.color.swipeBackground));
            icon = ContextCompat.getDrawable(context, R.drawable.edit);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final Load load = settlement.getLoads().get(position);
            if (MI != null) {
                MI.vibrate();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        settlement.getLoads().remove(position);
                        Snackbar snackbar = Snackbar.make(binding.coordinator, "Item Deleted", Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", view -> {
                            if (MI != null) {
                                MI.vibrate();
                                settlement.getLoads().add(position, load);
                                calculate();
                                model.add(settlement);
                            }
                        });
                        View v = snackbar.getView();
                        v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        TextView textView = v.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.show();
                        calculate();
                        model.add(settlement);
                        break;
                    case ItemTouchHelper.RIGHT:
                        MI.newLoad(load, position);
                        break;
                }
            }
        }

        @Override
        public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            //int backgroundCornerOffset = 0;
            if (dX > 0) { // Swiping to the right
                //background = new ColorDrawable(Color.BLUE);
                icon = ContextCompat.getDrawable(context, R.drawable.edit);
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 3;
                int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                int iconLeft = itemView.getLeft() + iconMargin;
                if (iconRight > dX) icon.setBounds(0, 0, 0, 0);
                else
                    icon.setBounds(iconLeft, itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2, iconRight, (itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2) + icon.getIntrinsicHeight());
                //background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left
                //background = new ColorDrawable(Color.RED);
                icon = ContextCompat.getDrawable(context, R.drawable.delete);
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 3;
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconWidth = icon.getIntrinsicWidth() + iconMargin;
                int iconRight = itemView.getRight() - iconMargin;
                if (-iconWidth < dX) icon.setBounds(0, 0, 0, 0);
                else
                    icon.setBounds(iconLeft, itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2, iconRight, (itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2) + icon.getIntrinsicHeight());
                //background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
                //background.setBounds(0, 0, 0, 0);
                icon.setBounds(0, 0, 0, 0);
            }
            //background.draw(c);
            icon.draw(c);
        }
    }
}