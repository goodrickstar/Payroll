package com.glass.payroll;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentNewLoadBinding;

import java.util.Calendar;
public class NewLoadFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener {
    private final Calendar calendar = Calendar.getInstance();
    private int mode = 0;
    private Load load = new Load();
    private boolean editing = false;
    private FragmentNewLoadBinding binding;
    private Settlement settlement;
    private MainViewModel model;

    public NewLoadFragment() {
        setCalendarToDayEdge(calendar, true);
        load.setStart(calendar.getTimeInMillis());
        setCalendarToDayEdge(calendar, false);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        load.setStop(calendar.getTimeInMillis());
    }

    public NewLoadFragment(Load load) {
        this.load = load;
        editing = true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewLoadBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        updateUi();
        binding.date.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
        binding.gpsA.setOnClickListener(this);
        binding.gpsB.setOnClickListener(this);
        binding.startLayout.setOnClickListener(this);
        binding.stopLayout.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);
        binding.finish.setOnClickListener(this);
        binding.cost.setFilters(Utils.inputFilter());
        binding.emptyMiles.setFilters(Utils.inputFilter());
        binding.loadedMiles.setFilters(Utils.inputFilter());
        binding.hazmat.setChecked(load.getHazmat());
        binding.reefer.setChecked(load.getReefer());
        binding.tonu.setChecked(load.getTonu());
        final CompoundButton.OnCheckedChangeListener listener = (compoundButton, b) -> Utils.vibrate(compoundButton);
        binding.hazmat.setOnCheckedChangeListener(listener);
        binding.reefer.setOnCheckedChangeListener(listener);
        binding.tonu.setOnCheckedChangeListener(listener);
        model.stats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                final double operatingCost = (stats.getAvgGross() - stats.getAvgBalance()) / stats.getAvgMiles();
                TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (!binding.emptyMiles.getText().toString().isEmpty() && !binding.loadedMiles.getText().toString().isEmpty()) {
                            int emptyMiles = Utils.parseInt(binding.emptyMiles.getText());
                            int loadedMiles = Utils.parseInt(binding.loadedMiles.getText());
                            int miles = emptyMiles + loadedMiles;
                            binding.totalMiles.setText("Total Miles: " + Utils.formatInt(miles));
                            if (!binding.cost.getText().toString().isEmpty()) {
                                int rate = Utils.parseInt(binding.cost.getText());
                                binding.profit.setText("* Estimated Profit: " + Utils.formatValueToCurrency(rate - (miles * operatingCost)));
                            }
                        }
                    }
                };
                binding.cost.addTextChangedListener(watcher);
                binding.emptyMiles.addTextChangedListener(watcher);
                binding.loadedMiles.addTextChangedListener(watcher);
                if (editing) {
                    binding.title.setText("Edit Load");
                    binding.finish.setText("Update");
                    binding.location.setText(load.getFrom());
                    binding.locationB.setText(load.getTo());
                    binding.cost.setText(String.valueOf(load.getRate()));
                    binding.emptyMiles.setText(String.valueOf(load.getEmpty()));
                    binding.loadedMiles.setText(String.valueOf(load.getLoaded()));
                    if (load.getWeight() != 0)
                        binding.weight.setText(String.valueOf(load.getWeight()));
                    binding.optionalNote.setText(load.getNote());
                } else {
                    model.location().observe(getViewLifecycleOwner(), locationString -> {
                        binding.location.setHint(locationString.getLocation());
                        binding.locationB.setHint(locationString.getLocation());
                    });
                }
            }
        });
        binding.weight.setFilters(new DigitsInputFilter[]{new DigitsInputFilter(3, 3, 200)});
        model.settlement().observe(getViewLifecycleOwner(), settlement -> NewLoadFragment.this.settlement = settlement);
        model.truck().observe(getViewLifecycleOwner(), truck -> binding.truckNumber.setText(String.valueOf(truck.getId())));
        model.trailer().observe(getViewLifecycleOwner(), trailer -> binding.trailerNumber.setText(String.valueOf(trailer.getId())));
    }

    private void checkEntries() {
        boolean error = false;
        if (TextUtils.isEmpty(binding.location.getText())) error = setError(binding.location);
        if (TextUtils.isEmpty(binding.locationB.getText())) error = setError(binding.locationB);
        if (TextUtils.isEmpty(binding.cost.getText())) error = setError(binding.cost);
        if (TextUtils.isEmpty(binding.emptyMiles.getText())) error = setError(binding.emptyMiles);
        if (TextUtils.isEmpty(binding.loadedMiles.getText())) error = setError(binding.loadedMiles);
        if (error) return;
        load.setRate(parseInt(binding.cost.getText()));
        load.setEmpty(parseInt(binding.emptyMiles.getText()));
        load.setLoaded(parseInt(binding.loadedMiles.getText()));
        load.setWeight(parseDouble(binding.weight.getText()));
        load.setFrom(binding.location.getText().toString().trim());
        load.setTo(binding.locationB.getText().toString().trim());
        load.setNote(binding.optionalNote.getText().toString().trim());
        load.setHazmat(binding.hazmat.isChecked());
        load.setReefer(binding.reefer.isChecked());
        load.setTonu(binding.tonu.isChecked());
        if (!editing)
            settlement.getLoads().add(load);
        else {
            for (int x = 0; x < settlement.getLoads().size(); x++) {
                if (settlement.getLoads().get(x).getStamp() == load.getStamp())
                    settlement.getLoads().set(x, load);
            }
        }
        model.add(Utils.sortLoads(Utils.calculate(settlement), Utils.getOrder(getContext(), "loads"), Utils.getSort(getContext(), "loads")));
        dismiss();
    }

    private boolean setError(EditText view) {
        view.setError("Required");
        return true;
    }

    private int parseInt(Editable editable) {
        try {
            return Integer.parseInt(editable.toString().trim());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private double parseDouble(Editable editable) {
        try {
            return Double.parseDouble(editable.toString().trim());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_NoActionBar_FullScreenDialog;
    }

    private void setCalendarToDayEdge(Calendar calendar, boolean beginning) {
        if (beginning) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.MILLISECOND, 1);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.MILLISECOND, 999);
        }
        calendar.set(Calendar.SECOND, 0);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Utils.vibrate(datePicker.getRootView());
        calendar.set(year, month, day);
        switch (mode) {
            case 1:
                setCalendarToDayEdge(calendar, true);
                load.setStart(calendar.getTimeInMillis());
                if (load.getStart() > load.getStop()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    load.setStop(calendar.getTimeInMillis());
                }
                break;
            case 2:
                setCalendarToDayEdge(calendar, false);
                load.setStop(calendar.getTimeInMillis());
                if (load.getStop() < load.getStart()) load.setStart(calendar.getTimeInMillis());
                break;
        }
        updateUi();
        mode = 0;
    }

    private void updateUi() {
        calendar.setTimeInMillis(load.getStart());
        binding.startView.setText(Utils.toShortDateSpelled(load.getStart()));
        binding.stopView.setText(Utils.toShortDateSpelled(load.getStop()));
        binding.weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        binding.progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        binding.thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        binding.nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        int days = Utils.calculateDifference(load.getStart(), load.getStop());
        switch (days) {
            case 0:
                binding.duration.setText("same day");
                break;
            case 1:
                binding.duration.setText(days + " day");
                break;
            default:
                binding.duration.setText(days + " days");
        }
    }

    @Override
    public void onClick(View view) {
        Utils.vibrate(view);
        calendar.setTimeInMillis(System.currentTimeMillis());
        DatePickerDialog datePickerDialog;
        switch (view.getId()) {
            case R.id.gpsA:
                Utils.gps(requireActivity());
                model.location().observe(getViewLifecycleOwner(), locationString -> binding.location.setText(locationString.getLocation()));
                break;
            case R.id.gpsB:
                Utils.gps(requireActivity());
                model.location().observe(getViewLifecycleOwner(), locationString -> binding.locationB.setText(locationString.getLocation()));
                break;
            case R.id.startLayout:
                Utils.hideKeyboard(requireContext(), view);
                mode = 1;
                calendar.setTimeInMillis(load.getStart());
                datePickerDialog = new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTimeInMillis(settlement.getStart());
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(settlement.getStop());
                datePickerDialog.show();
                break;
            case R.id.stopLayout:
                Utils.hideKeyboard(requireContext(), view);
                mode = 2;
                calendar.setTimeInMillis(load.getStop());
                datePickerDialog = new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(settlement.getStart());
                datePickerDialog.getDatePicker().setMaxDate(settlement.getStop());
                datePickerDialog.show();
                break;
            case R.id.cancel:
                Utils.hideKeyboard(requireContext(), view);
                this.dismiss();
                break;
            case R.id.finish:
                Utils.hideKeyboard(requireContext(), view);
                checkEntries();
                break;
        }
    }
}
