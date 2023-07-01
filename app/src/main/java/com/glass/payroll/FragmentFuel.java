package com.glass.payroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentFuelBinding;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

public class FragmentFuel extends Fragment implements View.OnClickListener {
    private Context context;
    private MI MI;
    private Settlement settlement;
    private FragmentFuelBinding binding;
    private MainViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentFuel() {
    }

    private void calculate() {
        int totalCost = 0;
        int totalGallons = 0;
        int defCost = 0;
        for (Fuel x : settlement.getFuel()) {
            if (!x.getDef()) {
                totalCost += x.getCost();
                totalGallons += x.getGallons();
            } else defCost += x.getCost();
        }
        if (totalCost != 0)
            binding.total.setText("Fuel $" + formatInt(totalCost) + " (" + formatInt(totalGallons) + " gal @ " + Utils.formatValueToCurrency((double) totalCost / totalGallons) + ")");
        else binding.total.setText("Include fuel and DEF purchases");
        if (defCost != 0) binding.cost.setText("DEF $" + formatInt(defCost));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFuelBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        final RecycleAdapter recyclerAdapter = new RecycleAdapter();
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.recycler.setAdapter(recyclerAdapter);
        new ItemTouchHelper(new SwipeToDeleteCallback(recyclerAdapter)).attachToRecyclerView(binding.recycler);
        binding.addButton.setOnClickListener(this);
        binding.order.setChecked(Utils.getOrder(context, "fuel"));
        binding.sort.setChecked(Utils.getSort(context, "fuel"));
        binding.order.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) button.setText(getString(R.string.asc));
            else button.setText(getString(R.string.desc));
            model.add(Utils.sortFuel(settlement, binding.order.isChecked(), binding.sort.isChecked()));
            Utils.vibrate(button);
            Utils.setOrder(context, "fuel", checked);
        });
        binding.sort.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) binding.sortView.setText(getString(R.string.date));
            else binding.sortView.setText(getString(R.string.amount));
            model.add(Utils.sortFuel(settlement, binding.order.isChecked(), binding.sort.isChecked()));
            Utils.vibrate(button);
            Utils.setSort(context, "fuel", checked);
        });
        if (!binding.order.isChecked()) binding.order.setText(getString(R.string.asc));
        else binding.order.setText(getString(R.string.desc));
        if (!binding.sort.isChecked()) binding.sortView.setText(getString(R.string.date));
        else binding.sortView.setText(getString(R.string.amount));
        model.settlement().observe(getViewLifecycleOwner(), settlement -> {
            FragmentFuel.this.settlement = settlement;
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
        if (view.getId() == R.id.add_button && MI != null) {
            MI.newFuel(null, 0);
            Utils.vibrate(view);
        }
    }

    private String formatInt(int count) {
        return NumberFormat.getNumberInstance(Locale.US).format(count);
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.fuel_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            Fuel fuel = settlement.getFuel().get(position);
            holder.date.setText(Utils.toShortDateSpelledWithTime(fuel.getStamp()));
            if (!fuel.getDef()) holder.cost.setText(Utils.formatValueToCurrency(fuel.getCost()));
            else holder.cost.setText("DEF   " +Utils.formatValueToCurrency(fuel.getCost()));
            holder.location.setText(fuel.getLocation());
            holder.gallons.setText(Utils.formatDouble(fuel.getGallons(), 2) + " gal @ $" + fuel.getFuelPrice());
            holder.note.setText(fuel.getNote());
            if (fuel.getNote().equals("")) holder.note.setVisibility(View.GONE);
            else holder.note.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return settlement.getFuel().size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView date;
            final TextView cost;
            final TextView gallons;
            final TextView location;
            final TextView note;

            viewHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
                cost = itemView.findViewById(R.id.cost);
                gallons = itemView.findViewById(R.id.gallons);
                location = itemView.findViewById(R.id.location);
                note = itemView.findViewById(R.id.optional_note);
            }
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final RecycleAdapter adapter;
        private Drawable icon;

        SwipeToDeleteCallback(RecycleAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
            icon = ContextCompat.getDrawable(context, R.drawable.delete);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            final int position = viewHolder.getAdapterPosition();
            final Fuel fuel = settlement.getFuel().get(position);
            if (MI != null) {
                Utils.vibrate(viewHolder.itemView);
                switch (i) {
                    case ItemTouchHelper.LEFT: //delete
                        settlement.getFuel().remove(position);
                        Snackbar snackbar = Snackbar.make(binding.coordinator, "Item Deleted", Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", view -> {
                            if (MI != null) {
                                Utils.vibrate(viewHolder.itemView);
                                settlement.getFuel().add(position, fuel);
                                calculate();
                                model.add(Utils.calculate(settlement));
                            }
                        });
                        View v = snackbar.getView();
                        v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        TextView textView = v.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.show();
                        calculate();
                        model.add(Utils.calculate(settlement));
                        break;
                    case ItemTouchHelper.RIGHT: //edit
                        MI.newFuel(fuel, position);
                        adapter.notifyItemChanged(position);
                        break;
                }
            }
        }

        @Override
        public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            if (dX > 0) {
                icon = ContextCompat.getDrawable(context, R.drawable.edit);
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 3;
                int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                int iconLeft = itemView.getLeft() + iconMargin;
                if (iconRight > dX) icon.setBounds(0, 0, 0, 0);
                else
                    icon.setBounds(iconLeft, itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2, iconRight, (itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2) + icon.getIntrinsicHeight());
            } else if (dX < 0) {
                icon = ContextCompat.getDrawable(context, R.drawable.delete);
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 3;
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconWidth = icon.getIntrinsicWidth() + iconMargin;
                int iconRight = itemView.getRight() - iconMargin;
                if (-iconWidth < dX) icon.setBounds(0, 0, 0, 0);
                else
                    icon.setBounds(iconLeft, itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2, iconRight, (itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2) + icon.getIntrinsicHeight());
           } else {
                icon.setBounds(0, 0, 0, 0);
            }
            icon.draw(c);
        }
    }
}