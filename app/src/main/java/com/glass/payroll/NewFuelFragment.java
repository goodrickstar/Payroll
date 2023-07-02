package com.glass.payroll;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentNewFuelBinding;
import com.google.gson.Gson;

import java.util.Calendar;
public class NewFuelFragment extends DialogFragment implements View.OnClickListener {
    private MI MI;
    private final Fuel fuel;
    private boolean editing = false;
    private Settlement settlement;
    private MainViewModel model;
    private FragmentNewFuelBinding binding;

    public NewFuelFragment() {
        fuel = new Fuel();
    }

    public NewFuelFragment(Fuel fuel) {
        this.fuel = fuel;
        editing = true;
    }

    private void checkEntries() {
        boolean error = false;
        if (TextUtils.isEmpty(binding.location.getText())) error = setError(binding.location);
        if (TextUtils.isEmpty(binding.fuelPrice.getText())) error = setError(binding.fuelPrice);
        if (TextUtils.isEmpty(binding.gallons.getText())) error = setError(binding.gallons);
        if (TextUtils.isEmpty(binding.odometer.getText()))
            error = setError(binding.odometer);
        if (error) return;
        int odometer = parseInt(binding.odometer.getText());
        fuel.setOdometer(odometer);
        fuel.setFuelPrice(Utils.parseDouble(binding.fuelPrice.getText()));
        fuel.setGallons(Utils.parseDouble(binding.gallons.getText()));
        fuel.setCost(fuel.getFuelPrice() * fuel.getGallons());
        fuel.setLocation(binding.location.getText().toString().trim());
        fuel.setNote(binding.optionalNote.getText().toString().trim());
        fuel.setTruck(binding.truckNumber2.getText().toString());
        if (!editing) {
            settlement.getFuel().add(fuel);
        } else {
            for (int x = 0; x < settlement.getFuel().size(); x++) {
                if (settlement.getFuel().get(x).getStamp() == fuel.getStamp())
                    settlement.getFuel().set(x, fuel);
            }
        }
        model.add(Utils.sortFuel(Utils.calculate(settlement), Utils.getOrder(getContext(), "fuel"), Utils.getSort(getContext(), "fuel")));
        if (MainActivity.truck != null && !editing)
            model.add(MainActivity.truck);
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

    @Override
    public int getTheme() {
        return R.style.AppTheme_NoActionBar_FullScreenDialog;
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
        binding = FragmentNewFuelBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        Log.i("test", new Gson().toJson(fuel));
        binding.date.setText(Utils.toShortDateSpelled(fuel.getStamp()));
        binding.cancel.setOnClickListener(this);
        binding.finish.setOnClickListener(this);
        binding.gps.setOnClickListener(this);
        binding.fuelPrice.setFilters(new DigitsInputFilter[]{new DigitsInputFilter(1, 3, 10)});
        binding.gallons.setFilters(new DigitsInputFilter[]{new DigitsInputFilter(3, 3, 300)});
        binding.odometer.setFilters(Utils.inputFilter());
        final TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!binding.fuelPrice.getText().toString().isEmpty() && !binding.gallons.getText().toString().isEmpty()) {
                    double price = Utils.parseDouble(binding.fuelPrice.getText());
                    double gallons = Utils.parseDouble(binding.gallons.getText());
                    if (price != 0 && gallons != 0) {
                        binding.totalFuelCostTv.setText(Utils.formatValueToCurrency(gallons * price, true));
                    }
                }
            }
        };
        binding.fuelPrice.addTextChangedListener(watcher);
        binding.gallons.addTextChangedListener(watcher);
        Log.i("test", "Editing " + editing);
        if (editing) {
            binding.title.setText("Edit Fuel Entry");
            binding.finish.setText("Update");
            binding.location.setText(fuel.getLocation());
            if (fuel.getCost() != 0)
                binding.totalFuelCostTv.setText(Utils.formatValueToCurrency(fuel.getCost(), true));
            if (fuel.getFuelPrice() != 0)
                binding.fuelPrice.setText(String.valueOf(fuel.getFuelPrice()));
            if (fuel.getGallons() != 0)
                binding.gallons.setText(Utils.formatValueToCurrency(fuel.getGallons(), false));
            if (fuel.getOdometer() != 0)
                binding.odometer.setText(String.valueOf(fuel.getOdometer()));
            if (!fuel.getNote().isEmpty()) binding.optionalNote.setText(fuel.getNote());
            binding.defBox.setChecked(fuel.getDef());
            binding.odometer.setText(String.valueOf(fuel.getOdometer()));
            binding.truckNumber2.setText(fuel.getTruck());
        } else {
            if (MainActivity.truck != null) {
                binding.odometer.setText(String.valueOf(MainActivity.truck.getOdometer()));
                binding.truckNumber2.setText(String.valueOf(MainActivity.truck.getId()));
            }
            model.location().observe(getViewLifecycleOwner(), locationString -> binding.location.setHint(locationString.getLocation()));
        }
        Calendar calendar = Calendar.getInstance();
        binding.weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        binding.progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        binding.thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        binding.nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        binding.defBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            fuel.setDef(checked);
            if (checked) binding.info.setVisibility(View.VISIBLE);
            else binding.info.setVisibility(View.GONE);
        });
        if (fuel.getDef()) binding.info.setVisibility(View.VISIBLE);
        else binding.info.setVisibility(View.GONE);
        Utils.showKeyboard(getContext(), binding.fuelPrice);
        binding.location.setOnEditorActionListener((textView, i, keyEvent) -> {
            model.location().observe(getViewLifecycleOwner(), locationString -> binding.location.setText(locationString.getLocation()));
            return false;
        });
        model.settlement().observe(getViewLifecycleOwner(), settlement -> NewFuelFragment.this.settlement = settlement);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public void onClick(View view) {
        Utils.vibrate(view);
        switch (view.getId()) {
            case R.id.cancel:
                Utils.hideKeyboard(requireContext(), view);
                this.dismiss();
                break;
            case R.id.finish:
                Utils.hideKeyboard(requireContext(), view);
                checkEntries();
                break;
            case R.id.gps:
                Utils.gps(requireActivity());
                model.location().observe(getViewLifecycleOwner(), locationString -> binding.location.setText(locationString.getLocation()));
                break;
        }
    }
}
