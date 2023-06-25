package com.glass.payroll;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentStatisticsBinding;
import com.google.android.gms.common.stats.StatsUtils;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FragmentStatistics extends Fragment {
    private Context context;
    private FragmentStatisticsBinding binding;
    private MainViewModel model;

    public FragmentStatistics() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.averages.setHasFixedSize(true);
        binding.averages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        List<String> dataSetList = new ArrayList<>();
        dataSetList.add("Gross Revenue");
        dataSetList.add("Net Revenue");
        dataSetList.add("Total Miles");
        dataSetList.add("Net CPM");
        dataSetList.add("Fuel Price");
        ArrayAdapter<String> dataSetAdapter = new ArrayAdapter<>(context, R.layout.custom_spinner_item, dataSetList);
        dataSetAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        model.getAllSettlements().observe(getViewLifecycleOwner(), settlements -> {
            List<Statistic> statistics = new ArrayList<>();
            for (Settlement settlement : settlements) {
                Statistic statistic = new Statistic();
                statistic.setId(settlement.getId());
                statistic.setBalance(settlement.getBalance());
                statistic.setGross(settlement.getGross());
                statistic.setMiles(Utils.miles(settlement));
                statistic.setEmptyMiles(settlement.getEmptyMiles());
                statistic.setLoadedMiles(settlement.getLoadedMiles());
                statistic.setLoadedRate(settlement.getGross() / settlement.getLoadedMiles());
                statistic.setGallons(settlement.getDieselGallons());
                statistic.setFuelCost(settlement.getFuelCost());
                statistic.setFuelPrice(settlement.getFuelCost() / settlement.getDieselGallons());
                statistic.setNetCpm(settlement.getBalance() / Utils.miles(settlement));
                statistic.setOperatingCpm((settlement.getGross() - settlement.getBalance()) / Utils.miles(settlement));
                statistics.add(statistic);
            }
            statistics.sort(Comparator.comparingLong(Statistic::getId));
            if (!statistics.isEmpty()) {
                ArrayList<AverageItem> averages = new ArrayList<>();
                double[] balances = new double[statistics.size()];
                double[] gross = new double[statistics.size()];
                int[] miles = new int[statistics.size()];
                int[] emptyMiles = new int[statistics.size()];
                int[] loadedMiles = new int[statistics.size()];
                double[] gallons = new double[statistics.size()];
                double[] loadedRate = new double[statistics.size()];
                double[] fuelCost = new double[statistics.size()];
                for (int i = 0; i < statistics.size(); i++) {
                    Statistic statistic = statistics.get(i);
                    balances[i] = statistic.getBalance();
                    gross[i] = statistic.getGross();
                    miles[i] = statistic.getMiles();
                    gallons[i] = statistic.getGallons();
                    loadedRate[i] = statistic.getLoadedRate();
                    emptyMiles[i] = statistic.getEmptyMiles();
                    loadedMiles[i] = statistic.getLoadedMiles();
                    fuelCost[i] = statistic.getFuelCost();
                }
                double totalGross = Arrays.stream(gross).sum();
                double totalFuel = Arrays.stream(fuelCost).sum();
                double totalMiles = Arrays.stream(miles).sum();
                double totalProfit = Arrays.stream(balances).sum();
                double avgBalance = Arrays.stream(balances).average().orElse(0.0);
                double avgGross = Arrays.stream(gross).average().orElse(0.0);
                double avgMiles = Arrays.stream(miles).average().orElse(0.0);
                double avgEmptyMiles = Arrays.stream(emptyMiles).average().orElse(0.0);
                double avgLoadedMiles = Arrays.stream(loadedMiles).average().orElse(0.0);
                double avgGallons = Arrays.stream(gallons).average().orElse(0.0);
                double avgFuelCost = Arrays.stream(fuelCost).average().orElse(0.0);
                double avgRate = Arrays.stream(loadedRate).average().orElse(0.0);
                averages.add(new AverageItem("YTD", statistics.size() + " RECORDS", "AVERAGE"));
                averages.add(new AverageItem("Gross Revenue", "$" + Utils.formatDoubleWhole(avgGross)));
                averages.add(new AverageItem("Net Balance", "$" + Utils.formatInt((int) avgBalance)));
                averages.add(new AverageItem("Net CPM", Utils.formatValueToCurrency(avgBalance / avgMiles)));
                averages.add(new AverageItem("All Miles", Utils.formatDouble(avgMiles) + " m"));
                averages.add(new AverageItem("Empty Miles", Utils.formatDouble(avgEmptyMiles) + " m"));
                averages.add(new AverageItem("Loaded Miles", Utils.formatDouble(avgLoadedMiles) + " m"));
                averages.add(new AverageItem("Loaded Rate", Utils.formatValueToCurrency(avgRate)));
                averages.add(new AverageItem("Operating CPM", Utils.formatValueToCurrency((avgGross - avgBalance) / avgMiles)));
                averages.add(new AverageItem("Fuel Cost", Utils.formatValueToCurrency(avgFuelCost)));
                averages.add(new AverageItem("Gallons", Utils.formatDoubleWhole(avgGallons) + " g"));
                averages.add(new AverageItem("Fuel Price", Utils.formatValueToCurrency(avgFuelCost / avgGallons) + " g"));
                binding.averages.setAdapter(new RecycleAdapter(averages));
                binding.annual.setText("Total Miles: " + Utils.formatDoubleWhole(totalMiles) + "  |  " + "Total Fuel: " + Utils.formatValueToCurrency(totalFuel));
                binding.annual.setText(Utils.addLine(binding.annual) + "Total Revenue: $" + Utils.formatDoubleWhole(totalGross) + "  |  " + "Total Net: " + Utils.formatValueToCurrency(totalProfit, true));
                binding.annual.setText(Utils.addLine(binding.annual) + "Estimated Annual Profit: $" + Utils.formatInt((int) avgBalance * 50) + " (50 Weeks)");
            }
        });
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> {
        ArrayList<AverageItem> averages;

        public RecycleAdapter(ArrayList<AverageItem> averages) {
            this.averages = averages;
        }

        @NotNull
        @Override
        public RecycleAdapter.viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new RecycleAdapter.viewHolder(getLayoutInflater().inflate(R.layout.expandable_row_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleAdapter.viewHolder holder, int position) {
            if (position == 0) {
                holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.title.setTypeface(null, Typeface.BOLD);
                holder.content.setPaintFlags(holder.content.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.extra.setPaintFlags(holder.content.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            } else {
                holder.title.setTypeface(Typeface.DEFAULT);
                holder.title.setTypeface(null, Typeface.NORMAL);
                holder.content.setTypeface(Typeface.DEFAULT);
                holder.extra.setTypeface(Typeface.DEFAULT);
            }
            holder.title.setText(averages.get(position).getTitle());
            holder.content.setText(averages.get(position).getContent());
            holder.extra.setText(averages.get(position).getExtra());

        }

        @Override
        public int getItemCount() {
            return averages.size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView title, content, extra;

            viewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                content = itemView.findViewById(R.id.content);
                extra = itemView.findViewById(R.id.extra);
            }
        }
    }

}