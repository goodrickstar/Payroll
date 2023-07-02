package com.glass.payroll;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
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
            ArrayList<AverageItem> averages = new ArrayList<>();
            if (settlements.size() < 2) {
                averages.add(new AverageItem("", "As new settlements are created and updated, statistics and data will be calculated.", ""));
                binding.averages.setAdapter(new RecycleAdapter(averages));
            } else {
                model.getMostRecentSettlementId().observe(getViewLifecycleOwner(), recentId -> {
                    settlements.removeIf(settlement -> settlement.getId() == recentId);
                    final SettlementStats stats = Utils.calculateStats(settlements);
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
                    averages.add(new AverageItem("Loaded Rates", Utils.formatValueToCurrency(stats.getAvgLoadedRate())));
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
                    binding.averages.setAdapter(new RecycleAdapter(averages));
                    model.add(stats);
                });
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