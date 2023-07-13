package com.glass.payroll;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentStatisticsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FragmentStatistics extends Fragment implements AdapterView.OnItemSelectedListener {
    private Context context;
    private FragmentStatisticsBinding binding;
    private MainViewModel model;
    final static int ID_YEAR = 1000;
    final static int ID_QUARTER = 2000;
    final static int ID_MONTH = 3000;
    final static int ID_WEEK = 4000;

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

        List<SpinnerOption> selectionOptions = new ArrayList<>();
        selectionOptions.add(new SpinnerOption(ID_YEAR, "YEAR"));
        selectionOptions.add(new SpinnerOption(ID_QUARTER, "QUARTER"));
        selectionOptions.add(new SpinnerOption(ID_MONTH, "MONTH"));
        selectionOptions.add(new SpinnerOption(ID_WEEK, "WEEK"));
        SpinAdapter selectionAdapter = new SpinAdapter(context, R.layout.spinner_view, selectionOptions);
        binding.selectionSpinner.setAdapter(selectionAdapter);
        binding.selectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                model.executor().execute(() -> {
                    List<Integer> data = model.getYears();
                    final Settlement trueMostRecentSettlement = model.getTrueMostRecentSettlement();
                    if (data != null) {
                        final int year = data.get(0);
                        List<SpinnerOption> dateOptions;
                        final SpinnerOption option = (SpinnerOption) view.getTag();
                        switch (option.getId()) {
                            case ID_YEAR:
                                dateOptions = convertSelectionToDateOptions(option, data);
                                break;
                            case ID_QUARTER:
                                dateOptions = convertSelectionToDateOptions(option, model.getQuarters(year));
                                break;
                            case ID_MONTH:
                                dateOptions = convertSelectionToDateOptions(option, model.getMonths(year));
                                break;
                            default: //week
                                dateOptions = convertSelectionToDateOptions(option, model.getWeeks(year));
                        }
                        requireActivity().runOnUiThread(() -> binding.dateSpinner.setAdapter(new DateAdapter(context, R.layout.spinner_view, dateOptions)));
                        binding.dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                model.executor().execute(() -> {
                                    final SpinnerOption dateOption = (SpinnerOption) view.getTag();
                                    List<Settlement> settlements;
                                    switch (dateOption.getId()) {
                                        case ID_YEAR:
                                            settlements = model.getSettlementsFromYear(Integer.parseInt(dateOption.getLabel()));
                                            break;
                                        case ID_QUARTER:
                                            settlements = model.getSettlementsFromQuarter(year, Integer.parseInt(dateOption.getLabel()));
                                            break;
                                        case ID_MONTH:
                                            settlements = model.getSettlementsFromMonth(year, Integer.parseInt(dateOption.getLabel()));
                                            break;
                                        default: //week
                                            settlements = model.getSettlementsFromWeek(year, Integer.parseInt(dateOption.getLabel()));
                                    }
                                    settlements.remove(trueMostRecentSettlement);
                                    requireActivity().runOnUiThread(() -> updateStatsBoard(Utils.calculateStats(settlements)));
                                });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        binding.selectionIcon.setOnClickListener(view -> {
            Utils.vibrate(view);
            binding.selectionSpinner.performClick();
        });
        binding.selectionIcon2.setOnClickListener(view -> {
            Utils.vibrate(view);
            binding.dateSpinner.performClick();
        });
        model.getAllSettlements().observe(getViewLifecycleOwner(), settlements -> {
            SettlementStats stats = Utils.calculateStats(settlements);
            binding.annual.setText("Est Annual Profit: $" + Utils.formatInt((int) stats.getAvgBalance() * 50) + " (50 Weeks)");
        });
    }

    private List<SpinnerOption> convertSelectionToDateOptions(SpinnerOption option, List<Integer> input) {
        List<SpinnerOption> options = new ArrayList<>();
        for (int i : input) {
            options.add(new SpinnerOption(option.getId(), String.valueOf(i)));
        }
        return options;
    }

    private void updateStatsBoard(SettlementStats stats) {
        ArrayList<AverageItem> averages = new ArrayList<>();
        if (stats == null) {
            averages.add(new AverageItem("", "As new settlements are created and updated, statistics and data will be calculated.", ""));
            binding.averages.setAdapter(new RecycleAdapter(averages));
            return;
        }
        averages.add(new AverageItem("Total Revenue", Utils.formatValueToCurrencyWhole(stats.getTotalGross())));
        averages.add(new AverageItem("Total Profit", Utils.formatValueToCurrencyWhole(stats.getTotalProfit())));
        averages.add(new AverageItem("Total Miles", Utils.formatDoubleWhole(stats.getTotalMiles())));
        averages.add(new AverageItem("Total Fuel", Utils.formatValueToCurrencyWhole(stats.getTotalFuel())));
        averages.add(new AverageItem("Avg Gross", Utils.formatValueToCurrencyWhole(stats.getAvgGross())));
        averages.add(new AverageItem("Avg Net", Utils.formatValueToCurrencyWhole(stats.getAvgBalance())));
        averages.add(new AverageItem("Avg Net CPM", Utils.formatValueToCurrency(stats.getAvgBalance() / stats.getAvgMiles())));
        averages.add(new AverageItem("Avg Operating CPM", Utils.formatValueToCurrency((stats.getAvgGross() - stats.getAvgBalance()) / stats.getAvgMiles())));
        averages.add(new AverageItem("Avg Fuel Mileage", Utils.formatDouble(stats.getTotalMiles() / stats.getTotalDieselGallons(), 3) + " mpg"));
        if (stats.getTotalDefGallons() > 0) {
            averages.add(new AverageItem("Avg Fuel Cost", Utils.formatValueToCurrency(stats.getAvgDieselTotal() + stats.getAvgDefTotal())));
            averages.add(new AverageItem("     Diesel", Utils.formatValueToCurrency(stats.getAvgDieselTotal())));
            averages.add(new AverageItem("     DEF", Utils.formatValueToCurrency(stats.getAvgDefTotal())));
            averages.add(new AverageItem("Avg Diesel & DEF", Utils.formatDouble(stats.getAvgDieselGallons(), 2) + Utils.formatDouble(stats.getAvgDefGallons(), 2) + " g"));
            averages.add(new AverageItem("     Diesel Price", Utils.formatValueToCurrency(stats.getAvgDieselTotal() / stats.getAvgDieselGallons()) + " g"));
            averages.add(new AverageItem("     Diesel Gallons", Utils.formatDouble(stats.getAvgDieselGallons(), 2) + " g"));
            averages.add(new AverageItem("     DEF Price", Utils.formatValueToCurrency(stats.getAvgDefTotal() / stats.getAvgDefGallons()) + " g"));
            averages.add(new AverageItem("     DEF Gallons", Utils.formatDouble(stats.getAvgDefGallons(), 2) + " g"));
        } else {
            averages.add(new AverageItem("Avg Diesel Cost", Utils.formatValueToCurrency(stats.getAvgDieselTotal())));
            averages.add(new AverageItem("     Diesel Price", Utils.formatValueToCurrency(stats.getAvgDieselTotal() / stats.getAvgDieselGallons()) + " g"));
            averages.add(new AverageItem("     Diesel Gallons", Utils.formatDouble(stats.getAvgDieselGallons(), 2) + " g"));
        }
        averages.add(new AverageItem("Avg Miles", Utils.formatDoubleWhole(stats.getAvgMiles()) + " m"));
        averages.add(new AverageItem("     Empty", Utils.formatDoubleWhole(stats.getAvgEmptyMiles()) + " m"));
        averages.add(new AverageItem("     Loaded", Utils.formatDoubleWhole(stats.getAvgLoadedMiles()) + " m"));
        averages.add(new AverageItem("Avg Loaded Rates", Utils.formatValueToCurrency(stats.getAvgLoadedRate())));
        averages.add(new AverageItem("     General", Utils.formatValueToCurrency(stats.getAvgGeneralRate())));
        if (stats.getAvgHazmatRate() != 0)
            averages.add(new AverageItem("     Hazmat", Utils.formatValueToCurrency(stats.getAvgHazmatRate())));
        if (stats.getAvgReeferRate() != 0)
            averages.add(new AverageItem("     Reefer", Utils.formatValueToCurrency(stats.getAvgReeferRate())));
        if (stats.getAvgHazmatAndReeferRate() != 0)
            averages.add(new AverageItem("     Avg Ref-Haz", Utils.formatValueToCurrency(stats.getAvgHazmatAndReeferRate())));
        binding.averages.setAdapter(new RecycleAdapter(averages));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

    public static class SpinAdapter extends ArrayAdapter<SpinnerOption> {
        // Your sent context
        private final Context context;
        // Your custom values for the spinner (User)
        private final List<SpinnerOption> spinnerOptions;

        public SpinAdapter(Context context, int textViewResourceId, List<SpinnerOption> values) {
            super(context, textViewResourceId, values);
            this.context = context;
            this.spinnerOptions = values;
        }

        @Override
        public int getCount() {
            return spinnerOptions.size();
        }

        @Override
        public SpinnerOption getItem(int position) {
            return spinnerOptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.spinner_view, null);
            TextView label = v.findViewById(R.id.item_label);
            label.setText(spinnerOptions.get(position).getLabel());
            v.setTag(spinnerOptions.get(position));
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.spinner_view, null);
            TextView label = v.findViewById(R.id.item_label);
            label.setText(spinnerOptions.get(position).getLabel());
            v.setTag(spinnerOptions.get(position));
            return v;
        }
    }

    public static class DateAdapter extends ArrayAdapter<SpinnerOption> {
        private final Context context;
        private final List<SpinnerOption> spinnerOptions;

        public DateAdapter(Context context, int textViewResourceId, List<SpinnerOption> values) {
            super(context, textViewResourceId, values);
            this.context = context;
            this.spinnerOptions = values;
        }

        @Override
        public int getCount() {
            return spinnerOptions.size();
        }

        @Override
        public SpinnerOption getItem(int position) {
            return spinnerOptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.spinner_view, null);
            TextView label = v.findViewById(R.id.item_label);
            SpinnerOption option = spinnerOptions.get(position);
            switch (option.getId()) {
                case ID_QUARTER:
                    label.setText("Q" + option.getLabel());
                    break;
                case ID_MONTH:
                    label.setText(Utils.returnMonthName(Integer.parseInt(option.getLabel())));
                    break;
                case ID_WEEK:
                    label.setText("w" + option.getLabel());
                    break;
                default:
                    label.setText(option.getLabel());
            }
            v.setTag(option);
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.spinner_view, null);
            TextView label = v.findViewById(R.id.item_label);
            SpinnerOption option = spinnerOptions.get(position);
            switch (option.getId()) {
                case ID_QUARTER:
                    label.setText("Q" + option.getLabel());
                    break;
                case ID_MONTH:
                    label.setText(Utils.returnMonthName(Integer.parseInt(option.getLabel())));
                    break;
                case ID_WEEK:
                    label.setText("w" + option.getLabel());
                    break;
                default:
                    label.setText(option.getLabel());
            }
            v.setTag(option);
            return v;
        }
    }



    /*
    .replaceAll("[^0-9]", "")
     */

    static class SpinnerOption {
        int id;
        String label;

        public SpinnerOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}