package com.glass.payroll;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentNewFuelBinding;
import com.google.gson.Gson;

import java.util.Calendar;
public class NewFuelFragment extends DialogFragment implements View.OnClickListener {
    private MI MI;
    private Fuel fuel = new Fuel();
    private boolean editing = false;
    private int index = 0;
    private Settlement settlement;
    private MainViewModel model;
    private FragmentNewFuelBinding binding;

    public NewFuelFragment() {
        // Required empty public constructor
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
        fuel.setFuelPrice(parseDouble(binding.fuelPrice.getText()));
        fuel.setGallons(parseDouble(binding.gallons.getText()));
        fuel.setCost(fuel.getFuelPrice() * fuel.getGallons());
        fuel.setLocation(binding.location.getText().toString().trim());
        fuel.setNote(binding.optionalNote.getText().toString().trim());
        fuel.setTruck(binding.truckNumber2.getText().toString());
        if (!editing) {
            settlement.getFuel().add(fuel);
        } else {
            settlement.getFuel().set(index, fuel);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        if (getArguments() != null) {
            editing = true;
            fuel = new Gson().fromJson(getArguments().getString("fuel"), Fuel.class);
            index = getArguments().getInt("index");
        }
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
        TextView weekView = v.findViewById(R.id.weekView);
        TextView thisYear = v.findViewById(R.id.thisYear);
        TextView nextYear = v.findViewById(R.id.nextYear);
        ProgressBar progressBar = v.findViewById(R.id.progressBar);
        TextView title = v.findViewById(R.id.title);
        TextView cancel = v.findViewById(R.id.cancel);
        TextView finish = v.findViewById(R.id.finish);
        TextView date = v.findViewById(R.id.date);
        ImageView gps = v.findViewById(R.id.gps);
        final TextView info = v.findViewById(R.id.info);
        CheckBox def = v.findViewById(R.id.defBox);
        date.setText(Utils.toShortDateSpelled(fuel.getStamp()));
        cancel.setOnClickListener(this);
        finish.setOnClickListener(this);
        gps.setOnClickListener(this);
        binding.fuelPrice.setFilters(new DigitsInputFilter[]{new DigitsInputFilter(1, 2, 10)});
        binding.gallons.setFilters(new DigitsInputFilter[]{new DigitsInputFilter(3, 2, 300)});
        binding.odometer.setFilters(Utils.inputFilter());
        if (editing) {
            title.setText("Edit Fuel Entry");
            finish.setText("Update");
            binding.location.setText(fuel.getLocation());
            if (fuel.getCost() != 0)
                binding.totalFuelCostTv.setText(Utils.formatValueToCurrency(fuel.getCost(), true));
            if (fuel.getFuelPrice() != 0)
                binding.fuelPrice.setText(Utils.formatValueToCurrency(fuel.getFuelPrice(), false));
            if (fuel.getGallons() != 0)
                binding.gallons.setText(Utils.formatValueToCurrency(fuel.getGallons(), false));
            if (fuel.getOdometer() != 0)
                binding.odometer.setText(String.valueOf(fuel.getOdometer()));
            if (!fuel.getNote().isEmpty()) binding.optionalNote.setText(fuel.getNote());
            def.setChecked(fuel.getDef());
            binding.odometer.setText(String.valueOf(fuel.getOdometer()));
            binding.truckNumber2.setText(fuel.getTruck());
        } else {
            if (MainActivity.truck != null) {
                binding.odometer.setText(String.valueOf(MainActivity.truck.getOdometer()));
                binding.truckNumber2.setText(String.valueOf(MainActivity.truck.getId()));
            }
            if (MI != null) {
                if (MI.locationPermission()) {
                    if (!MI.returnLocation().isEmpty())
                        binding.location.setHint(MI.returnLocation());
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        def.setOnCheckedChangeListener((compoundButton, checked) -> {
            fuel.setDef(checked);
            if (checked) info.setVisibility(View.VISIBLE);
            else info.setVisibility(View.GONE);
        });
        if (fuel.getDef()) info.setVisibility(View.VISIBLE);
        else info.setVisibility(View.GONE);
        Utils.showKeyboard(getContext(), binding.fuelPrice);
        EditText.OnEditorActionListener actionListener = (textView, actionId, keyEvent) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_NEXT:
                    double fuelPrice = parseDouble(binding.fuelPrice.getText());
                    double gallons = parseDouble(binding.gallons.getText());
                    if (fuelPrice != 0 && gallons != 0) {
                        binding.totalFuelCostTv.setText(Utils.formatValueToCurrency(gallons * fuelPrice, true));
                    }
                    break;
                case EditorInfo.IME_ACTION_DONE:
                    if (textView.getId() == R.id.location)
                        binding.location.setText(MI.returnLocation());
                    break;
            }
            return false;
        };
        binding.fuelPrice.setOnEditorActionListener(actionListener);
        binding.gallons.setOnEditorActionListener(actionListener);
        binding.odometer.setOnEditorActionListener(actionListener);
        binding.location.setOnEditorActionListener(actionListener);
        model.settlement().observe(getViewLifecycleOwner(), settlement -> NewFuelFragment.this.settlement = settlement);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public void onClick(View view) {
        if (MI != null) {
            MI.vibrate(view);
            MI.hideKeyboard(view);
            switch (view.getId()) {
                case R.id.cancel:
                    this.dismiss();
                    break;
                case R.id.finish:
                    checkEntries();
                    break;
                case R.id.gps:
                    binding.location.setText(MI.returnLocation());
                    break;
            }
        }
    }
}
