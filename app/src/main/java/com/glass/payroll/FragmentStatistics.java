package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
                statistic.setMiles((settlement.getEmptyMiles() + settlement.getLoadedMiles()));
                for (Fuel fuel : settlement.getFuel()) {
                    if (!fuel.getDef()) {
                        statistic.setFuelCost(statistic.getFuelCost() + fuel.getCost());
                        statistic.setGallons(statistic.getGallons() + fuel.getGallons());
                    }
                }
                int loadedMiles = 0;
                for (Load load : settlement.getLoads()) {
                    statistic.setGross(statistic.getGross() + load.getRate());
                    loadedMiles += load.getLoaded();
                }
                statistic.setLoadedRate(statistic.getGross() / loadedMiles);
                statistic.setFuelPrice(statistic.getFuelCost() / statistic.getGallons());
                statistic.setNetCpm(statistic.getBalance() / statistic.getMiles());
                statistic.setOperatingCpm((statistic.getGross() - statistic.getBalance()) / statistic.getMiles());
                statistics.add(statistic);
            }
            statistics.sort(Comparator.comparingLong(Statistic::getId));
            if (!statistics.isEmpty()) {
                ArrayList<AverageItem> averages = new ArrayList<>();
                double[] balances = new double[statistics.size()];
                int[] gross = new int[statistics.size()];
                int[] miles = new int[statistics.size()];
                double[] gallons = new double[statistics.size()];
                double[] fuelCosts = new double[statistics.size()];
                double[] loadedRate = new double[statistics.size()];
                for (int i = 0; i < statistics.size(); i++) {
                    Statistic statistic = statistics.get(i);
                    balances[i] = statistic.getBalance();
                    gross[i] = statistic.getGross();
                    miles[i] = statistic.getMiles();
                    gallons[i] = statistic.getGallons();
                    fuelCosts[i] = statistic.getFuelCost();
                    loadedRate[i] = statistic.getLoadedRate();
                }
                double avgBalance = Utils.avg(balances);
                int avgGross = Utils.avg(gross);
                int avgMiles = Utils.avg(miles);
                double avgGallons = Utils.avg(gallons);
                double fuelCost = Utils.avg(fuelCosts);
                double avgRate = Utils.avg(loadedRate);
                averages.add(new AverageItem("Miles", Utils.formatInt(avgMiles)));
                averages.add(new AverageItem("Loaded Rate", Utils.formatValueToCurrency(avgRate)));
                averages.add(new AverageItem("Gross Revenue", "$" + Utils.formatInt(avgGross)));
                averages.add(new AverageItem("Net Balance", "$" + Utils.formatInt((int) avgBalance)));
                averages.add(new AverageItem("Net CPM", Utils.formatValueToCurrency(avgBalance / avgMiles)));
                averages.add(new AverageItem("Operating CPM", Utils.formatValueToCurrency((avgGross - avgBalance) / avgMiles)));
                averages.add(new AverageItem("Fuel Cost", Utils.formatValueToCurrency(fuelCost)));
                averages.add(new AverageItem("Gallons", String.valueOf(Utils.formatDouble(avgGallons))));
                averages.add(new AverageItem("Fuel Price", Utils.formatValueToCurrency(fuelCost / avgGallons)));
                binding.averages.setAdapter(new RecycleAdapter(averages));
                binding.annual.setText("Estimated Annual Income $" + Utils.formatInt((int) avgBalance * 52));
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
            holder.title.setText(averages.get(position).getTitle());
            holder.content.setText(averages.get(position).getContent());
        }

        @Override
        public int getItemCount() {
            return averages.size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView title, content;

            viewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                content = itemView.findViewById(R.id.content);
            }
        }
    }

}