package com.glass.payroll;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.UpdateOdometerBinding;

public class FragmentOdometer extends DialogFragment implements View.OnClickListener {
    private MainViewModel model;
    private UpdateOdometerBinding binding;

    public FragmentOdometer() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UpdateOdometerBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.finish.setOnClickListener(this);
        binding.odometer.setFilters(Utils.inputFilter());
        model.truck().observe(getViewLifecycleOwner(), truck -> {
            binding.odometer.setText(String.valueOf(truck.getOdometer()));
            binding.odometer.setTag(truck);
            Utils.showKeyboard(getContext(), binding.odometer);
        });
    }

    @Override
    public void onClick(View view) {
        Utils.vibrate(view);
        Utils.hideKeyboard(requireContext(), view);
        final Truck truck = (Truck) binding.odometer.getTag();
        if (view.getId() == R.id.finish && truck != null) {
            if (!TextUtils.isEmpty(binding.odometer.getText())) {
                int odometer = Integer.parseInt(binding.odometer.getText().toString().trim());
                if (odometer != 0 && odometer != truck.getOdometer()) {
                    truck.setOdometer(odometer);
                    model.add(truck);
                }
            }
        }
        dismiss();
    }
}
