package com.glass.payroll;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.glass.payroll.databinding.FragmentNewSettlementBinding;

import java.util.Calendar;

public class NewSettlementFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener {
    private int mode = 0;
    private final Calendar calendar = Calendar.getInstance();
    private MI MI;
    private final Settlement settlement = new Settlement();
    private FragmentNewSettlementBinding binding;


    public NewSettlementFragment() {
        // Required empty public constructor
    }

    private void checkEntries() {
        if (MI != null) {
            MI.newSettlement(settlement, binding.checkBox.isChecked());
            NewSettlementFragment.this.dismiss();
        }
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewSettlementBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.label.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
        binding.startLayout.setOnClickListener(this);
        binding.stopLayout.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);
        binding.finish.setOnClickListener(this);
        if (settlement.getStart() != 0) {
            calendar.setTimeInMillis(settlement.getStart());
            int startOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_WEEK, startOfWeek);
            setCalendarToDayEdge(calendar, true);
            settlement.setStart(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, 6);
            setCalendarToDayEdge(calendar, false);
            settlement.setStop(calendar.getTimeInMillis());
            if (MainActivity.truck != null) binding.truck.setText(MainActivity.truck.getId());
            if (MainActivity.trailer != null) binding.trailer.setText(MainActivity.trailer.getId());
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            setCalendarToDayEdge(calendar, true);
            settlement.setStart(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, 6);
            setCalendarToDayEdge(calendar, false);
            settlement.setStop(calendar.getTimeInMillis());
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        updateUi();
        //if (settlement.getTruck().equals("")) Utils.showKeyboard(getContext(), binding.truck);
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
        if (MI != null) MI.vibrate();
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
            MI.vibrate();
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
