package com.glass.payroll;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentStatisticsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        model.getAllSettlements().observe(getViewLifecycleOwner(), settlements -> {
            settlements.remove(0);
            List<Statistic> statistics = new ArrayList<>();
            ArrayList<Double> allRates = new ArrayList<>();
            for (Settlement settlement : settlements) {
                Statistic statistic = new Statistic();
                statistic.setId(settlement.getId());
                statistic.setBalance(settlement.getBalance());
                statistic.setGross(settlement.getGross());
                statistic.setMiles(Utils.miles(settlement));
                statistic.setEmptyMiles(settlement.getEmptyMiles());
                statistic.setLoadedMiles(settlement.getLoadedMiles());
                statistic.setDieselGallons(settlement.getDieselGallons());
                statistic.setDieselCost(settlement.getFuelCost());
                statistic.setDefCost(settlement.getDefCost());
                statistic.setDefGallons(settlement.getDefGallons());
                statistic.setFuelPrice(settlement.getFuelCost() / settlement.getDieselGallons());
                statistic.setNetCpm(settlement.getBalance() / Utils.miles(settlement));
                statistic.setOperatingCpm((settlement.getGross() - settlement.getBalance()) / Utils.miles(settlement));
                ArrayList<Double> generalRates = new ArrayList<>();
                ArrayList<Double> hazmatRates = new ArrayList<>();
                ArrayList<Double> reeferRates = new ArrayList<>();
                ArrayList<Double> hazmatAndReeferRates = new ArrayList<>();
                for (Load load : settlement.getLoads()) {
                    if (!load.getTonu()){
                        double rate = Utils.loadedRate(load);
                        if (rate != 0) allRates.add(rate);
                        if (rate != 0 && !load.getTonu()) {
                            if (!load.getHazmat() && !load.getReefer()) {
                                generalRates.add(rate);
                            } else if (load.getHazmat() && !load.getReefer()) {
                                hazmatRates.add(rate);
                            } else if (!load.getHazmat() && load.getReefer()) {
                                reeferRates.add(rate);
                            } else if (load.getHazmat() && load.getReefer()) {
                                hazmatAndReeferRates.add(rate);
                            }
                        }
                    }
                }
                statistic.setGeneralFreightRate(Utils.avgDouble(generalRates));
                statistic.setHazmatRate(Utils.avgDouble(hazmatRates));
                statistic.setReeferRate(Utils.avgDouble(reeferRates));
                statistic.setHazmatAndReeferRate(Utils.avgDouble(hazmatAndReeferRates));
                statistics.add(statistic);
            }
            if (!statistics.isEmpty()) {
                ArrayList<AverageItem> averages = new ArrayList<>();
                ArrayList<Integer> miles = new ArrayList<>();
                ArrayList<Integer> emptyMiles = new ArrayList<>();
                ArrayList<Integer> loadedMiles = new ArrayList<>();
                ArrayList<Double> balances = new ArrayList<>();
                ArrayList<Double> gross = new ArrayList<>();
                ArrayList<Double> dieselGallons = new ArrayList<>();
                ArrayList<Double> generalLoadedRate = new ArrayList<>();
                ArrayList<Double> dieselCost = new ArrayList<>();
                ArrayList<Double> defGallons = new ArrayList<>();
                ArrayList<Double> defCost = new ArrayList<>();
                ArrayList<Double> hazmatLoadedRate = new ArrayList<>();
                ArrayList<Double> reeferLoadedRate = new ArrayList<>();
                ArrayList<Double> hazmatAndReeferLoadedRate = new ArrayList<>();
                for (int i = 0; i < statistics.size(); i++) {
                    Statistic statistic = statistics.get(i);
                    if (statistic.getBalance() != 0) balances.add(statistic.getBalance());
                    if (statistic.getGross() != 0) gross.add(statistic.getGross());
                    if (statistic.getMiles() != 0) miles.add(statistic.getMiles());
                    if (statistic.getGeneralFreightRate() != 0)
                        generalLoadedRate.add(statistic.getGeneralFreightRate());
                    if (statistic.getEmptyMiles() != 0) emptyMiles.add(statistic.getEmptyMiles());
                    if (statistic.getLoadedMiles() != 0)
                        loadedMiles.add(statistic.getLoadedMiles());
                    if (statistic.getDieselGallons() != 0)
                        dieselGallons.add(statistic.getDieselGallons());
                    if (statistic.getDieselCost() != 0) dieselCost.add(statistic.getDieselCost());
                    if (statistic.getDefCost() != 0) defCost.add(statistic.getDefCost());
                    if (statistic.getDefGallons() != 0) defGallons.add(statistic.getDefGallons());
                    if (statistic.getHazmatRate() != 0)
                        hazmatLoadedRate.add(statistic.getHazmatRate());
                    if (statistic.getReeferRate() != 0)
                        reeferLoadedRate.add(statistic.getReeferRate());
                    if (statistic.getHazmatAndReeferRate() != 0)
                        hazmatAndReeferLoadedRate.add(statistic.getHazmatAndReeferRate());
                }
                SettlementStats stats = new SettlementStats(MainActivity.user.getUid());
                stats.setTotalGross(Utils.sumDouble(gross));
                stats.setTotalFuel(Utils.sumDouble(dieselCost) + Utils.sumDouble(defCost));
                stats.setTotalMiles(Utils.sumInt(miles));
                stats.setTotalProfit(Utils.sumDouble(balances));
                stats.setAvgBalance(Utils.avgDouble(balances));
                stats.setAvgGross(Utils.avgDouble(gross));
                stats.setAvgMiles(Utils.avgInt(miles));
                stats.setAvgEmptyMiles(Utils.avgInt(emptyMiles));
                stats.setAvgLoadedMiles(Utils.avgInt(loadedMiles));
                Log.i("stats", "Avg: " + (double) stats.getAvgGross()/ stats.getAvgLoadedMiles());
                stats.setAvgDieselTotal(Utils.avgDouble(dieselCost));
                stats.setAvgDefTotal(Utils.avgDouble(defCost));
                stats.setTotalDieselGallons(Utils.sumDouble(dieselGallons));
                stats.setAvgDieselGallons(Utils.avgDouble(dieselGallons));
                stats.setAvgGeneralRate(Utils.avgDouble(generalLoadedRate));
                stats.setTotalDefGallons(Utils.sumDouble(defGallons));
                stats.setAvgDefGallons(Utils.avgDouble(defGallons));
                stats.setAvgHazmatRate(Utils.avgDouble(hazmatLoadedRate));
                stats.setAvgReeferRate(Utils.avgDouble(reeferLoadedRate));
                stats.setAvgHazmatAndReeferRate(Utils.avgDouble(hazmatAndReeferLoadedRate));
                model.add(stats);
                averages.add(new AverageItem("YTD", " RECORDS: " + settlements.size(), "AVERAGE"));
                averages.add(new AverageItem("Gross Revenue", Utils.formatValueToCurrencyWhole(stats.getAvgGross())));
                averages.add(new AverageItem("Net Balance", Utils.formatValueToCurrencyWhole(stats.getAvgBalance())));
                averages.add(new AverageItem("Net CPM", Utils.formatValueToCurrency(stats.getAvgBalance() / stats.getAvgMiles())));
                averages.add(new AverageItem("Operating CPM", Utils.formatValueToCurrency((stats.getAvgGross() - stats.getAvgBalance()) / stats.getAvgMiles())));
                averages.add(new AverageItem("Diesel Price", Utils.formatValueToCurrency(stats.getAvgDieselTotal() / stats.getAvgDieselGallons()) + " g"));
                averages.add(new AverageItem("DEF Price", Utils.formatValueToCurrency(stats.getAvgDefTotal() / stats.getAvgDefGallons()) + " g"));
                averages.add(new AverageItem("Fuel Mileage", Utils.formatDouble(stats.getTotalMiles() / stats.getTotalDieselGallons(), 3) + " mpg"));
                averages.add(new AverageItem("All Miles", Utils.formatDoubleWhole(stats.getAvgMiles()) + " m"));
                averages.add(new AverageItem("     Empty", Utils.formatDoubleWhole(stats.getAvgEmptyMiles()) + " m"));
                averages.add(new AverageItem("     Loaded", Utils.formatDoubleWhole(stats.getAvgLoadedMiles()) + " m"));
                averages.add(new AverageItem("Loaded Rates", Utils.formatValueToCurrency(Utils.avgDouble(allRates))));
                averages.add(new AverageItem("     General", Utils.formatValueToCurrency(stats.getAvgGeneralRate())));
                if (stats.getAvgHazmatRate() != 0)
                    averages.add(new AverageItem("     Hazmat", Utils.formatValueToCurrency(stats.getAvgHazmatRate())));
                if (stats.getAvgReeferRate() != 0)
                    averages.add(new AverageItem("     Reefer", Utils.formatValueToCurrency(stats.getAvgReeferRate())));
                if (stats.getAvgHazmatAndReeferRate() != 0)
                    averages.add(new AverageItem("     Ref-Haz", Utils.formatValueToCurrency(stats.getAvgHazmatAndReeferRate())));
                averages.add(new AverageItem("Fuel Cost", Utils.formatValueToCurrency(stats.getAvgDieselTotal() + stats.getAvgDefTotal())));
                averages.add(new AverageItem("     Diesel", Utils.formatValueToCurrency(stats.getAvgDieselTotal())));
                averages.add(new AverageItem("     DEF", Utils.formatValueToCurrency(stats.getAvgDefTotal())));
                averages.add(new AverageItem("Diesel & DEF", Utils.formatDouble(stats.getAvgDieselGallons(), 2) + Utils.formatDouble(stats.getAvgDefGallons(), 2) + " g"));
                averages.add(new AverageItem("     Diesel Gallons", Utils.formatDouble(stats.getAvgDieselGallons(), 2) + " g"));
                averages.add(new AverageItem("     DEF Gallons", Utils.formatDouble(stats.getAvgDefGallons(), 2) + " g"));
                binding.averages.setAdapter(new RecycleAdapter(averages));
                binding.annual.setText("Total Miles: " + Utils.formatDoubleWhole(stats.getTotalMiles()) + "  |  " + "Total Fuel: " + Utils.formatValueToCurrencyWhole(stats.getTotalFuel()));
                binding.annual.setText(Utils.addLine(binding.annual) + "Total Revenue: " + Utils.formatValueToCurrencyWhole(stats.getTotalGross()) + "  |  " + "Total Net: " + Utils.formatValueToCurrencyWhole(stats.getTotalProfit()));
                binding.annual.setText(Utils.addLine(binding.annual) + "Estimated Annual Profit: $" + Utils.formatInt((int) stats.getAvgBalance() * 50) + " (50 Weeks)");
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