package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class FragmentOdometer extends DialogFragment implements View.OnClickListener {
    private EditText odometer_reading;
    private MI MI;
    private int odometer = 0;
    private MainViewModel model;
    public FragmentOdometer() {
    }
    private void checkEntries() {

        //TODO: Truck Odometer

        /*
        if (MI != null) {
            if (!TextUtils.isEmpty(odometer_reading.getText())) {
                odometer = Integer.parseInt(odometer_reading.getText().toString().trim());
                if (odometer != 0 && odometer != MainActivity.settlement.getOdometer()+ MainActivity.settlement.getMiles())
                    MainActivity.settlement.setMiles(odometer - MainActivity.settlement.getOdometer());
            }
            MI.saveSettlement(true);
            dismiss();
        }
         */
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        if (getArguments() != null) {
            odometer = getArguments().getInt("odometer");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_odometer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        TextView finish = v.findViewById(R.id.finish);
        finish.setOnClickListener(this);
        odometer_reading = v.findViewById(R.id.odometer_reading);
        InputFilter intFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        odometer_reading.setFilters(new InputFilter[]{intFilter});
        if (odometer != 0) odometer_reading.setText(String.valueOf(odometer));
        Utils.showKeyboard(getContext(), odometer_reading);
    }

    @Override
    public void onAttach(Context context) {
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
            }
        }
    }
}
