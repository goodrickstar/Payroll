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

import com.glass.payroll.databinding.FragmentMiscellaneousBinding;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

public class FragmentMiscellaneous extends Fragment implements View.OnClickListener {
    private Context context;
    private MI MI;
    private FragmentMiscellaneousBinding binding;
    private Settlement settlement;
    private MainViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    private void calculate() {
        if (settlement.getMiscCost() != 0)
            binding.total.setText(getString(R.string.total) + Utils.formatValueToCurrency(settlement.getMiscCost(), true));
        else
            binding.total.setText(getString(R.string.misc_note));
    }

    public FragmentMiscellaneous() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMiscellaneousBinding.inflate(inflater);
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
        binding.order.setChecked(Utils.getOrder(context, "miscellaneous"));
        binding.sort.setChecked(Utils.getSort(context, "miscellaneous"));
        binding.order.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) button.setText(getString(R.string.asc));
            else button.setText(getString(R.string.desc));
            Utils.vibrate(button);
            model.add(Utils.sortMiscellaneous(settlement, binding.order.isChecked(), binding.sort.isChecked()));
            Utils.setOrder(context, "miscellaneous", checked);
        });
        //SORT
        binding.sort.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) binding.sortView.setText(getString(R.string.date));
            else binding.sortView.setText(getString(R.string.amount));
            Utils.vibrate(button);
            model.add(Utils.sortMiscellaneous(settlement, binding.order.isChecked(), binding.sort.isChecked()));
            Utils.setSort(context, "miscellaneous", checked);
        });
        if (!binding.order.isChecked()) binding.order.setText(getString(R.string.asc));
        else binding.order.setText(getString(R.string.desc));
        if (!binding.sort.isChecked()) binding.sortView.setText(getString(R.string.date));
        else binding.sortView.setText(getString(R.string.amount));
        model.settlement().observe(getViewLifecycleOwner(), settlement -> {
            FragmentMiscellaneous.this.settlement = settlement;
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
            MI.newMisc(null, 0);
            Utils.vibrate(view);
        }
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.cost_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            Cost cost = settlement.getMiscellaneous().get(position);
            holder.location.setText(cost.getLabel());
            holder.cost.setText(Utils.formatValueToCurrency(cost.getCost(), true));
            holder.date.setText(Utils.toShortDateSpelled(cost.getStamp()));
            holder.gallons.setText(cost.getLocation());
        }

        @Override
        public int getItemCount() {
            return settlement.getMiscellaneous().size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView date;
            final TextView cost;
            final TextView gallons;
            final TextView location;

            viewHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
                cost = itemView.findViewById(R.id.cost);
                gallons = itemView.findViewById(R.id.gallons);
                location = itemView.findViewById(R.id.location);
            }
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        //private final ColorDrawable background;
        private Drawable icon;
        private final RecycleAdapter adapter;

        public SwipeToDeleteCallback(RecycleAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
            icon = ContextCompat.getDrawable(context, R.drawable.edit);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (MI != null) {
                Utils.vibrate(viewHolder.itemView);
                final int position = viewHolder.getAdapterPosition();
                final Cost cost = settlement.getMiscellaneous().get(position);
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        settlement.getMiscellaneous().remove(position);
                        Snackbar snackbar = Snackbar.make(binding.coordinator, "Item Deleted", Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", view -> {
                            settlement.getMiscellaneous().add(position, cost);
                            calculate();
                            model.add(Utils.calculate(settlement));
                            Utils.vibrate(viewHolder.itemView);
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
                    case ItemTouchHelper.RIGHT:
                        adapter.notifyItemChanged(position);
                        MI.newMisc(cost, position);
                        break;
                }
            }
        }

        @Override
        public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            //int backgroundCornerOffset = 0;
            if (dX > 0) { // Swiping stop the right
                //background = new ColorDrawable(Color.BLUE);
                icon = ContextCompat.getDrawable(context, R.drawable.edit);
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 3;
                int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                int iconLeft = itemView.getLeft() + iconMargin;
                if (iconRight > dX) icon.setBounds(0, 0, 0, 0);
                else
                    icon.setBounds(iconLeft, itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2, iconRight, (itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2) + icon.getIntrinsicHeight());
                //background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            } else if (dX < 0) { // Swiping stop the left
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