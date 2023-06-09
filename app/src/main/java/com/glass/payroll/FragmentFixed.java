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

import com.glass.payroll.databinding.FragmentFixedBinding;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
public class FragmentFixed extends Fragment implements View.OnClickListener {
    private Context context;
    private MI MI;
    private FragmentFixedBinding binding;
    private MainViewModel model;
    private Settlement settlement;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentFixed() {
    }

    private void calculate() {
        if (settlement.getFixedCost() == 0) binding.total.setText(getString(R.string.fixed_note));
        else
            binding.total.setText(getString(R.string.total) + " " + Utils.formatValueToCurrency(settlement.getFixedCost(), true));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFixedBinding.inflate(inflater);
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
        binding.order.setChecked(Utils.getOrder(context, "fixed"));
        binding.sort.setChecked(Utils.getSort(context, "fixed"));
        binding.order.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) button.setText(getString(R.string.asc));
            else button.setText(getString(R.string.desc));
            Utils.vibrate(button);
            model.add(Utils.sortFixed(settlement, binding.order.isChecked(), binding.sort.isChecked()));
            Utils.setOrder(context, "fixed", checked);
        });
        binding.sort.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) binding.sortView.setText(getString(R.string.entry));
            else binding.sortView.setText(getString(R.string.amount));
            Utils.vibrate(button);
            model.add(Utils.sortFixed(settlement, binding.order.isChecked(), binding.sort.isChecked()));
            Utils.setSort(context, "fixed", checked);
        });
        if (!binding.order.isChecked()) binding.order.setText(getString(R.string.asc));
        else binding.order.setText(getString(R.string.desc));
        if (!binding.sort.isChecked()) binding.sortView.setText(getString(R.string.entry));
        else binding.sortView.setText(getString(R.string.amount));
        model.settlement().observe(getViewLifecycleOwner(), settlement -> {
            FragmentFixed.this.settlement = settlement;
            calculate();
            recyclerAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        MI = (MI) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    @Override
    public void onClick(View view) {
        Utils.vibrate(view);
        NewFixedFragment fi = (NewFixedFragment) getParentFragmentManager().findFragmentByTag("newFixed");
        if (fi == null) {
            fi = new NewFixedFragment();
            fi.show(getParentFragmentManager(), "newFixed");
        }
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> implements View.OnClickListener {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.fixed_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            Cost cost = settlement.getFixed().get(position);
            holder.cost.setTextColor(Color.RED);
            holder.label.setText(cost.getLabel());
            holder.cost.setText(Utils.formatValueToCurrency(cost.getCost(), true));
            holder.itemView.setTag(cost);
            holder.itemView.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return settlement.getFixed().size();
        }

        @Override
        public void onClick(View view) {
            Cost cost = (Cost) view.getTag();
            Utils.vibrate(view);
            NewFixedFragment fi = (NewFixedFragment) getParentFragmentManager().findFragmentByTag("newFixed");
            if (fi == null) {
                fi = new NewFixedFragment(cost);
                fi.show(getParentFragmentManager(), "newFixed");
            }
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView label;
            final TextView cost;

            viewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                cost = itemView.findViewById(R.id.cost);
            }
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private Drawable icon;

        public SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT);
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
                final Cost cost = settlement.getFixed().get(position);
                settlement.getFixed().remove(position);
                Snackbar snackbar = Snackbar.make(binding.coordinator, "Item Deleted", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", view -> {
                    if (MI != null) {
                        Utils.vibrate(viewHolder.itemView);
                        settlement.getFixed().add(position, cost);
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
            }
        }

        @Override
        public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            if (dX < 0) {
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