package com.glass.payroll;
import android.content.Context;
import android.graphics.Color;
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
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
public class FragmentOverview extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private Context context;
    private MI MI;
    private final ArrayList<Item> items = new ArrayList<>();
    private FragmentOverviewBinding binding;
    private MainViewModel model;
    private List<Long> keys;
    private Settlement settlement;

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
                Utils.vibrate(view);
                FragmentOdometer odometerFragment = new FragmentOdometer();
                odometerFragment.show(getParentFragmentManager(), "odometer");
            }
        });
        model.settlement().observe(getViewLifecycleOwner(), settlement -> {
            FragmentOverview.this.settlement = settlement;
            items.clear();
            Item item = new Item("Loads", R.id.loads);
            item.setTotal(settlement.getGross());
            item.setTotal2(settlement.getEmptyMiles() + settlement.getLoadedMiles());
            items.add(item);
            item = new Item("Fuel", R.id.fuel);
            item.setTotal(settlement.getFuelCost() + settlement.getDefCost());
            item.setTotal2(settlement.getDefGallons() + settlement.getDieselGallons());
            items.add(item);
            item = new Item("Fixed", R.id.fixed);
            item.setTotal(settlement.getFixedCost());
            items.add(item);
            item = new Item("Misc", R.id.miscellaneous);
            item.setTotal(settlement.getMiscCost());
            items.add(item);
            item = new Item("Payout", R.id.payout);
            item.setTotal(settlement.getPayoutCost());
            items.add(item);
            item = new Item("Maintenance", R.id.payout);
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
            if (truck != null) {
                binding.odomter.setText("Latest Odometer: " + Utils.formatInt(truck.getOdometer()));
                model.workOrders(truck).observe(getViewLifecycleOwner(), workOrders -> {
                    if (workOrders != null){
                        //int x = new Random().nextInt(workOrders.size());
                        int x = 0;
                        binding.workOrderUpdate.setVisibility(View.VISIBLE);
                        int due = workOrders.get(x).getReading() - truck.getOdometer();
                        if (due < 0) binding.workOrderUpdate.setTextColor(Color.RED);
                        binding.workOrderUpdate.setText(workOrders.get(x).getLabel() + " due in "+ Utils.formatInt(due)+" m");
                        binding.workOrderUpdate.setOnClickListener(view -> {
                            Utils.vibrate(view);
                            MI.navigate(R.id.maintenance);
                        });
                    } else {
                        binding.workOrderUpdate.setVisibility(View.INVISIBLE);
                    }

                });
            }
        });
        model.keys().observe(getViewLifecycleOwner(), keys -> FragmentOverview.this.keys = keys);
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
        if (MI != null)
            Utils.vibrate(view);
        int index = keys.indexOf(settlement.getId());
        switch (view.getId()) {
            case R.id.previous:
                if (index + 1 == keys.size()) {
                    MI.showSnack("Last Record", Snackbar.LENGTH_SHORT);
                    binding.previous.setOnClickListener(null);
                    binding.previous.setOnLongClickListener(null);
                    return;
                }
                binding.next.setOnClickListener(this);
                binding.next.setOnLongClickListener(this);
                model.setStampOnSettlement(keys.get(index + 1), Instant.now().getEpochSecond());
                break;
            case R.id.next:
                if (index == 0) {
                    MI.showSnack("Current Settlement", Snackbar.LENGTH_SHORT);
                    binding.next.setOnClickListener(null);
                    binding.next.setOnLongClickListener(null);
                    return;
                }
                binding.previous.setOnClickListener(this);
                binding.previous.setOnLongClickListener(this);
                model.setStampOnSettlement(keys.get(index - 1), Instant.now().getEpochSecond());
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (keys.isEmpty()) return true;
        Utils.vibrate(view);
        switch (view.getId()) {
            case R.id.previous:
                MI.showSnack("Last Record", Snackbar.LENGTH_SHORT);
                binding.previous.setOnClickListener(null);
                binding.previous.setOnLongClickListener(null);
                binding.next.setOnClickListener(this);
                binding.next.setOnLongClickListener(this);
                model.setStampOnSettlement(keys.get(keys.size() - 1), Instant.now().getEpochSecond());
                break;
            case R.id.next:
                MI.showSnack("Current Settlement", Snackbar.LENGTH_SHORT);
                binding.next.setOnClickListener(null);
                binding.next.setOnLongClickListener(null);
                binding.previous.setOnClickListener(this);
                binding.previous.setOnLongClickListener(this);
                model.setStampOnSettlement(keys.get(0), Instant.now().getEpochSecond());
                break;
        }
        return true;
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
            switch (item.getMenuId()) {
                case R.id.loads:
                    holder.icon.setImageResource(R.drawable.overview_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText(formatDouble(item.getTotal2(), "", " M"));
                    break;
                case R.id.fuel:
                    holder.icon.setImageResource(R.drawable.fuel_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText(formatDouble(item.getTotal2(), "", " G"));
                    break;
                case R.id.fixed:
                    holder.icon.setImageResource(R.drawable.fixed_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
                case R.id.miscellaneous:
                    holder.icon.setImageResource(R.drawable.miscellaneous_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
                case R.id.payout:
                    if (item.getLabel().equals("Payout"))
                        holder.icon.setImageResource(R.drawable.payout_w);
                    else holder.icon.setImageResource(R.drawable.maintenance_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
            }
            holder.container.setOnClickListener(view -> {
                if (MI != null) {
                    Utils.vibrate(view);
                    MI.navigate(item.getMenuId());
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