package com.glass.payroll;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentNewSettlementBinding;

import java.util.Calendar;

public class NewSettlementFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener {
    private final Calendar calendar = Calendar.getInstance();
    private final Settlement settlement = new Settlement();
    private int mode = 0;
    private MI MI;
    private FragmentNewSettlementBinding binding;

    private MainViewModel model;

    public NewSettlementFragment() {
    }

    private void checkEntries() {
        if (MI != null) {
            MI.newSettlement(settlement, binding.checkBox.isChecked());
            NewSettlementFragment.this.dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.floating);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        binding = FragmentNewSettlementBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.label.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
        binding.startLayout.setOnClickListener(this);
        binding.stopLayout.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);
        binding.finish.setOnClickListener(this);
        model.getMostRecentEndingDate().observe(getViewLifecycleOwner(), mostRecent -> {
            calendar.setTimeInMillis(mostRecent);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            setCalendarToDayEdge(calendar, true);
            settlement.setStart(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, 6);
            setCalendarToDayEdge(calendar, false);
            settlement.setStop(calendar.getTimeInMillis());
            calendar.setTimeInMillis(System.currentTimeMillis());
            setCalendarToDayEdge(calendar, true);
            updateUi();
        });
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (MI != null) Utils.vibrate(datePicker.getRootView());
        calendar.set(year, month, day);
        switch (mode) {
            case 1:
                setCalendarToDayEdge(calendar, true);
                settlement.setStart(calendar.getTimeInMillis());
                if (settlement.getStart() > settlement.getStop()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    settlement.setStop(calendar.getTimeInMillis());
                }
                break;
            case 2:
                setCalendarToDayEdge(calendar, false);
                settlement.setStop(calendar.getTimeInMillis());
                if (settlement.getStop() < settlement.getStart())
                    settlement.setStart(calendar.getTimeInMillis());
                break;
        }
        updateUi();
        mode = 0;
    }

    private void updateUi() {
        calendar.setTimeInMillis(settlement.getStart());
        binding.startView.setText(Utils.toShortDateSpelled(settlement.getStart()));
        binding.stopView.setText(Utils.toShortDateSpelled(settlement.getStop()));
        binding.weekView.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
        binding.progressBar.setProgress(calendar.get(Calendar.WEEK_OF_YEAR));
        binding.thisYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendar.add(Calendar.YEAR, 1);
        binding.nextYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
    }

    @Override
    public void onClick(View view) {
        if (MI != null) {
            Utils.vibrate(view);
            MI.hideKeyboard(view);
        }
        calendar.setTimeInMillis(System.currentTimeMillis());
        DatePickerDialog datePickerDialog;
        switch (view.getId()) {
            case R.id.startLayout:
                mode = 1;
                calendar.setTimeInMillis(settlement.getStart());
                datePickerDialog = new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.stopLayout:
                mode = 2;
                calendar.setTimeInMillis(settlement.getStop());
                datePickerDialog = new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.cancel:
                this.dismiss();
                break;
            case R.id.finish:
                checkEntries();
                break;
        }
    }
}
