package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentNewMiscBinding;
import com.google.gson.Gson;

import java.util.Calendar;

public class NewMiscFragment extends DialogFragment implements View.OnClickListener {
    private MI MI;
    private Cost cost = new Cost();
    private boolean editing = false;
    private int index = 0;
    private FragmentNewMiscBinding binding;
    private Settlement settlement;
    private MainViewModel model;
    public NewMiscFragment() {
        // Required empty public constructor
    }
    private void checkEntries() {
        boolean error = false;
        if (TextUtils.isEmpty(binding.cost.getText())) error = setError(binding.cost);
        if (TextUtils.isEmpty(binding.label.getText())) error = setError(binding.label);
        if (error) return;
        cost.setCost(parseInt(binding.cost.getText()));
        if (!binding.location.getText().toString().trim().equals("Unknown"))
            cost.setLocation(binding.location.getText().toString().trim());
        cost.setLabel(binding.label.getText().toString().trim());
        if (!editing)
            settlement.getMiscellaneous().add(cost);
        else
            settlement.getMiscellaneous().set(index, cost);
        model.add(Utils.sortMiscellaneous(Utils.calculate(settlement), Utils.getOrder(getContext(), "miscellaneous"), Utils.getSort(getContext(), "miscellaneous")));
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
        if (getArguments() != null) {
            editing = true;
            cost = new Gson().fromJson(getArguments().getString("cost"), Cost.class);
            index = getArguments().getInt("index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewMiscBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.date.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
        binding.cancel.setOnClickListener(this);
        binding.finish.setOnClickListener(this);
        binding.gps.setOnClickListener(this);
        binding.cost.setFilters(Utils.inputFilter());
        if (editing) {
            binding.title.setText("Edit Miscellaneous");
            binding.finish.setText("Update");
            binding.label.setText(cost.getLabel());
            binding.location.setText(cost.getLocation());
            binding.cost.setText(String.valueOf(cost.getCost()));
            binding.date.setText(Utils.toShortDateSpelled(cost.getStamp()));
        }
        Calendar calendar = Calendar.getInstance();
        binding.weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        binding.progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        binding.thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        binding.nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        Utils.showKeyboard(getContext(), binding.label);
        model.settlement().observe(getViewLifecycleOwner(), settlement -> NewMiscFragment.this.settlement = settlement);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public void onClick(View view) {
        if (MI != null) {
            MI.vibrate();
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
