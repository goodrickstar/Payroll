package com.glass.payroll;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentNewFixedBinding;

import java.util.Calendar;

public class NewFixedFragment extends DialogFragment implements View.OnClickListener {
    private Cost cost = new Cost();
    private boolean editing = false;
    private FragmentNewFixedBinding binding;
    private Settlement settlement;
    private MainViewModel model;
    public NewFixedFragment() {
    }

    public NewFixedFragment(Cost cost) {
        this.cost = cost;
        editing = true;
    }

    private void checkEntries() {
        boolean error = false;
        if (TextUtils.isEmpty(binding.cost.getText())) error = setError(binding.cost);
        if (TextUtils.isEmpty(binding.label.getText())) error = setError(binding.label);
        if (error) return;
        cost.setCost(Utils.parseDouble(binding.cost.getText()));
        cost.setLabel(binding.label.getText().toString().trim());
        if (!editing) {
            settlement.getFixed().add(cost);
        } else {
            for (int x = 0; x < settlement.getFixed().size(); x++) {
                if (settlement.getFixed().get(x).getStamp() == cost.getStamp())
                    settlement.getFixed().set(x, cost);
            }
        }
        model.add(Utils.sortFixed(Utils.calculate(settlement), Utils.getOrder(getContext(), "fixed"), Utils.getSort(getContext(), "fixed")));
        this.dismiss();
    }

    private boolean setError(EditText view) {
        view.setError("Required");
        return true;
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
        binding = FragmentNewFixedBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.date.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
        binding.cancel.setOnClickListener(this);
        binding.finish.setOnClickListener(this);
        binding.cost.setFilters(new DigitsInputFilter[]{new DigitsInputFilter(4, 2, 10000)});
        if (editing) {
            binding.title.setText("Edit Fixed Expenses");
            binding.finish.setText("Update");
            if (!cost.getLabel().isEmpty()) binding.label.setText(cost.getLabel());
            if (cost.getCost() != 0) binding.cost.setText(String.valueOf(cost.getCost()));
            binding.date.setText(Utils.toShortDateSpelled(cost.getStamp()));
        }
        Calendar calendar = Calendar.getInstance();
        binding.weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        binding.progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        binding.thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        binding.nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        Utils.showKeyboard(getContext(), binding.label);
        model.settlement().observe(getViewLifecycleOwner(), settlement -> NewFixedFragment.this.settlement = settlement);
    }

    @Override
    public void onClick(View view) {
        Utils.vibrate(view);
        Utils.hideKeyboard(requireContext(), view);
        switch (view.getId()) {
            case R.id.cancel:
                this.dismiss();
                break;
            case R.id.finish:
                checkEntries();
                break;
        }
    }
}
