package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentOverviewBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FragmentOverview extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private Context context;
    private MI MI;
    private final ArrayList<Item> items = new ArrayList<>();
    private FragmentOverviewBinding binding;
    private MainViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentOverview() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOverviewBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        final RecycleAdapter recyclerAdapter = new RecycleAdapter();
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new GridLayoutManager(context, 2));
        binding.recycler.setAdapter(recyclerAdapter);
        binding.previous.setOnClickListener(this);
        binding.previous.setOnLongClickListener(this);
        binding.next.setOnClickListener(this);
        binding.next.setOnLongClickListener(this);
        binding.odomter.setOnClickListener(view -> {
            if (MI != null) {
                MI.vibrate();
                MI.updateOdometer();
            }
        });
        model.settlement().observe(getViewLifecycleOwner(), settlement -> {
            items.clear();
            Item item = new Item("Loads", 3);
            item.setTotal(settlement.getGross());
            item.setTotal2(settlement.getEmptyMiles() + settlement.getLoadedMiles());
            items.add(item);
            item = new Item("Fuel", 4);
            item.setTotal(settlement.getFuelCost() + settlement.getDefCost());
            item.setTotal2(settlement.getDefGallons() + settlement.getDieselGallons());
            items.add(item);
            item = new Item("Fixed", 5);
            item.setTotal(settlement.getFixedCost());
            items.add(item);
            item = new Item("Misc", 6);
            item.setTotal(settlement.getMiscCost());
            items.add(item);
            item = new Item("Payout", 7);
            item.setTotal(settlement.getPayoutCost());
            items.add(item);
            item = new Item("Maintenance", 7);
            item.setTotal(settlement.getMaintenanceCost());
            items.add(item);
            recyclerAdapter.notifyDataSetChanged();
            binding.dates.setText(Utils.toShortDateSpelled(settlement.getStart()) + " - " + Utils.toShortDateSpelled(settlement.getStop()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(settlement.getStart());
            binding.weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
            binding.progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
            binding.thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
            calendar.add(Calendar.YEAR, 1);
            binding.nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        });
        model.truck().observe(getViewLifecycleOwner(), truck -> {
            if (truck != null) binding.odomter.setText("Latest Odometer: " + Utils.formatInt(truck.getOdometer()));
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

    private String formatDouble(double count, String prefix, String suffix) {
        return prefix + NumberFormat.getNumberInstance(Locale.US).format(count) + suffix;
    }

    @Override
    public void onClick(View view) {
        if (MI != null) {
            MI.vibrate();
            //TODO: switch settlements
         /*
            int index;
            switch (view.getId()) {
                case R.id.previous:
                    index = MI.indexSettlement(settlement.getId());
                    if (index + 1 == settlements.size()) {
                        MI.showSnack("Last Record", Snackbar.LENGTH_SHORT);
                        prev.setOnClickListener(null);
                        prev.setOnLongClickListener(null);
                        return;
                    }
                    settlement = settlements.get(index + 1);
                    next.setOnClickListener(this);
                    next.setOnLongClickListener(this);
                    break;
                case R.id.next:
                    index = MI.indexSettlement(settlement.getId());
                    if (index == 0) {
                        MI.showSnack("Current Settlement", Snackbar.LENGTH_SHORT);
                        next.setOnClickListener(null);
                        next.setOnLongClickListener(null);
                        return;
                    }
                    settlement = settlements.get(index - 1);
                    prev.setOnClickListener(this);
                    prev.setOnLongClickListener(this);
                    break;
            }
            calculate();
            MI.calculate();
          */
        }
    }

    @Override
    public boolean onLongClick(View view) {
       /*
        if (settlements.isEmpty()) return false;
        if (MI != null) {
            MI.vibrate();
            switch (view.getId()) {
                case R.id.previous:
                    MI.showSnack("Last Record", Snackbar.LENGTH_SHORT);
                    settlement = settlements.get(settlements.size() - 1);
                    prev.setOnClickListener(null);
                    prev.setOnLongClickListener(null);
                    next.setOnClickListener(this);
                    next.setOnLongClickListener(this);
                    break;
                case R.id.next:
                    MI.showSnack("Current Settlement", Snackbar.LENGTH_SHORT);
                    settlement = settlements.get(0);
                    next.setOnClickListener(null);
                    next.setOnLongClickListener(null);
                    prev.setOnClickListener(this);
                    prev.setOnLongClickListener(this);
                    break;
            }
            calculate();
            MI.calculate();
        }
        */
        return false;
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> implements View.OnClickListener {
        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.overview_row_2, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(viewHolder holder, int position) {
            final Item item = items.get(position);
            holder.title.setText(item.getLabel());
            switch (item.getIcon()) {
                case 3:
                    holder.icon.setImageResource(R.drawable.overview_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText(formatDouble(item.getTotal2(), "", " M"));
                    break;
                case 4:
                    holder.icon.setImageResource(R.drawable.fuel_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText(formatDouble(item.getTotal2(), "", " G"));
                    break;
                case 5:
                    holder.icon.setImageResource(R.drawable.fixed_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
                case 6:
                    holder.icon.setImageResource(R.drawable.miscellaneous_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
                case 7:
                    if (item.getLabel().contains("Maintenance")) {
                        holder.icon.setImageResource(R.drawable.maintenance_w);
                    } else {
                        holder.icon.setImageResource(R.drawable.payout_w);
                    }
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
            }
            holder.container.setOnClickListener(view -> {
                if (MI != null) {
                    MI.vibrate();
                    MI.navigate(item.getIcon());
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onClick(View view) {
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final RelativeLayout container;
            final TextView labelOne;
            final TextView labelTwo;
            final TextView title;
            final ImageView icon;

            viewHolder(View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.container);
                labelOne = itemView.findViewById(R.id.labelOne);
                labelTwo = itemView.findViewById(R.id.labelTwo);
                title = itemView.findViewById(R.id.title);
                icon = itemView.findViewById(R.id.icon);
            }
        }
    }
}