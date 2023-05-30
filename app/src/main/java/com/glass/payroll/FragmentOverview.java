package com.glass.payroll;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FragmentOverview extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private Context context;
    private MI MI;
    private final RecyclerView.Adapter recyclerAdapter = new RecycleAdapter();
    private final ArrayList<Item> items = new ArrayList<>();
    private TextView dates, odometer, weekView, thisYear, nextYear;
    private ProgressBar progressBar;
    private ImageView prev, next;

    public FragmentOverview() {
    }

    public void onDataChanged() {
        calculate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        RecyclerView recyclerView = v.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        else recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(recyclerAdapter);
        weekView = v.findViewById(R.id.weekView);
        thisYear = v.findViewById(R.id.thisYear);
        nextYear = v.findViewById(R.id.nextYear);
        progressBar = v.findViewById(R.id.progressBar);
        dates = v.findViewById(R.id.dates);
        odometer = v.findViewById(R.id.odomter);
        prev = v.findViewById(R.id.previous);
        next = v.findViewById(R.id.next);
        prev.setOnClickListener(this);
        prev.setOnLongClickListener(this);
        next.setOnClickListener(this);
        next.setOnLongClickListener(this);
        odometer.setOnClickListener(view -> {
            if (MI != null) {
                MI.vibrate();
                MI.updateOdometer();
            }
        });
    }

    private void setWeek(long start) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
    }

    private void calculate() {
        items.clear();
        Item item = new Item("Loads", 2);
        for (Load load : MainActivity.settlement.getLoads()) {
            item.setTotal(item.getTotal() + load.getRate());
        }
        item.setTotal2(MainActivity.settlement.getMiles());
        items.add(item);
        item = new Item("Fuel", 3);
        for (Fuel fuel : MainActivity.settlement.getFuel()) {
            item.setTotal(item.getTotal() + fuel.getCost());
            item.setTotal2(item.getTotal2() + fuel.getGallons());
        }
        items.add(item);
        item = new Item("Fixed", 4);
        for (Cost cost : MainActivity.settlement.getFixed()) {
            item.setTotal(item.getTotal() + cost.getCost());
        }
        items.add(item);
        item = new Item("Misc", 5);
        for (Cost cost : MainActivity.settlement.getMiscellaneous()) {
            item.setTotal(item.getTotal() + cost.getCost());
        }
        items.add(item);
        item = new Item("Payout", 6);
        item.setTotal((MainActivity.settlement.getMiles() * MainActivity.settlement.getPayout().getPCpm()) / 100);
        item.setTotal(item.getTotal() + ((items.get(0).getTotal() * MainActivity.settlement.getPayout().getPPercent()) / 100));
        items.add(item);
        item = new Item("Maintenance", 6);
        item.setTotal((MainActivity.settlement.getMiles() * MainActivity.settlement.getPayout().getMCpm()) / 100);
        item.setTotal(item.getTotal() + ((items.get(0).getTotal() * MainActivity.settlement.getPayout().getMPercent()) / 100));
        items.add(item);
        recyclerAdapter.notifyDataSetChanged();
        dates.setText(Utils.toShortDateSpelled(MainActivity.settlement.getStart()) + " - " + Utils.toShortDateSpelled(MainActivity.settlement.getStop()));
        odometer.setText("Latest Odometer: " + Utils.formatInt(MainActivity.settlement.getOdometer() + MainActivity.settlement.getMiles()) + " ");
        setWeek(MainActivity.settlement.getStart());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        MI = (MI) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        calculate();
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
            int index;
            switch (view.getId()) {
                case R.id.previous:
                    index = MI.indexSettlement(MainActivity.settlement.getId());
                    if (index + 1 == MainActivity.settlements.size()) {
                        MI.showSnack("Last Record", Snackbar.LENGTH_SHORT);
                        prev.setOnClickListener(null);
                        prev.setOnLongClickListener(null);
                        return;
                    }
                    MainActivity.settlement = MainActivity.settlements.get(index + 1);
                    next.setOnClickListener(this);
                    next.setOnLongClickListener(this);
                    break;
                case R.id.next:
                    index = MI.indexSettlement(MainActivity.settlement.getId());
                    if (index == 0) {
                        MI.showSnack("Current Settlement", Snackbar.LENGTH_SHORT);
                        next.setOnClickListener(null);
                        next.setOnLongClickListener(null);
                        return;
                    }
                    MainActivity.settlement = MainActivity.settlements.get(index - 1);
                    prev.setOnClickListener(this);
                    prev.setOnLongClickListener(this);
                    break;
            }
            calculate();
            MI.calculate();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (MainActivity.settlements.isEmpty()) return false;
        if (MI != null) {
            MI.vibrate();
            switch (view.getId()) {
                case R.id.previous:
                    MI.showSnack("Last Record", Snackbar.LENGTH_SHORT);
                    MainActivity.settlement = MainActivity.settlements.get(MainActivity.settlements.size() - 1);
                    prev.setOnClickListener(null);
                    prev.setOnLongClickListener(null);
                    next.setOnClickListener(this);
                    next.setOnLongClickListener(this);
                    break;
                case R.id.next:
                    MI.showSnack("Current Settlement", Snackbar.LENGTH_SHORT);
                    MainActivity.settlement = MainActivity.settlements.get(0);
                    next.setOnClickListener(null);
                    next.setOnLongClickListener(null);
                    prev.setOnClickListener(this);
                    prev.setOnLongClickListener(this);
                    break;
            }
            calculate();
            MI.calculate();
        }
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
                case 2:
                    holder.icon.setImageResource(R.drawable.overview_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText(formatDouble(item.getTotal2(), "", " M"));
                    break;
                case 3:
                    holder.icon.setImageResource(R.drawable.fuel_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText(formatDouble(item.getTotal2(), "", " G"));
                    break;
                case 4:
                    holder.icon.setImageResource(R.drawable.fixed_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
                case 5:
                    holder.icon.setImageResource(R.drawable.miscellaneous_w);
                    holder.labelOne.setText(formatDouble(item.getTotal(), "$", ""));
                    holder.labelTwo.setText("");
                    break;
                case 6:
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