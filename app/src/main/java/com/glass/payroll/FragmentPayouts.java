package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentPayouts extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private MI MI;
    private TextView p_percent_view, m_percent_view, p_cpm_view, m_cpm_view;

    public FragmentPayouts() {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean user) {
        if (!user) return;
        switch (seekBar.getId()) {
            case R.id.p_percent_seekbar:
                if (progress < 10)
                    p_percent_view.setText("PCT: 0" + progress + "%");
                else
                    p_percent_view.setText("PCT: " + progress + "%");
                break;
            case R.id.m_percent_seekbar:
                if (progress < 10)
                    m_percent_view.setText("PCT: 0" + progress + "%");
                else
                    m_percent_view.setText("PCT: " + progress + "%");
                break;
            case R.id.p_cpm_seekbar:
                if (progress < 10)
                    p_cpm_view.setText("CPM: 0" + progress + "¢");
                else
                    p_cpm_view.setText("CPM: " + progress + "¢");
                break;
            case R.id.m_cpm_seekbar:
                if (progress < 10)
                    m_cpm_view.setText("CPM: 0" + progress + "¢");
                else
                    m_cpm_view.setText("CPM: " + progress + "¢");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.p_percent_seekbar:
                MainActivity.settlement.getPayout().setPPercent(seekBar.getProgress());
                break;
            case R.id.m_percent_seekbar:
                MainActivity.settlement.getPayout().setMPercent(seekBar.getProgress());
                break;
            case R.id.p_cpm_seekbar:
                MainActivity.settlement.getPayout().setPCpm(seekBar.getProgress());
                break;
            case R.id.m_cpm_seekbar:
                MainActivity.settlement.getPayout().setMCpm(seekBar.getProgress());
                break;
        }
        if (MI != null) MI.saveSettlement(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        SeekBar p_percent_seekbar = v.findViewById(R.id.p_percent_seekbar);
        p_percent_view = v.findViewById(R.id.p_percent_view);
        p_percent_seekbar.setOnSeekBarChangeListener(this);
        //
        SeekBar p_cpm_seekbar = v.findViewById(R.id.p_cpm_seekbar);
        p_cpm_view = v.findViewById(R.id.p_cpm_view);
        p_cpm_seekbar.setOnSeekBarChangeListener(this);
        //
        SeekBar m_cpm_seekbar = v.findViewById(R.id.m_cpm_seekbar);
        m_cpm_view = v.findViewById(R.id.m_cpm_view);
        m_cpm_seekbar.setOnSeekBarChangeListener(this);
        //
        SeekBar m_percent_seekbar = v.findViewById(R.id.m_percent_seekbar);
        m_percent_view = v.findViewById(R.id.m_percent_view);
        m_percent_seekbar.setOnSeekBarChangeListener(this);
        p_percent_seekbar.setProgress(MainActivity.settlement.getPayout().getPPercent());
        m_percent_seekbar.setProgress(MainActivity.settlement.getPayout().getMPercent());
        p_cpm_seekbar.setProgress(MainActivity.settlement.getPayout().getPCpm());
        m_cpm_seekbar.setProgress(MainActivity.settlement.getPayout().getMCpm());
        if (MainActivity.settlement.getPayout().getPPercent() < 10)
            p_percent_view.setText("PCT: 0" + MainActivity.settlement.getPayout().getPPercent() + "%");
        else
            p_percent_view.setText("PCT: " + MainActivity.settlement.getPayout().getPPercent() + "%");
        if (MainActivity.settlement.getPayout().getMPercent() < 10)
            m_percent_view.setText("PCT: 0" + MainActivity.settlement.getPayout().getMPercent() + "%");
        else
            m_percent_view.setText("PCT: " + MainActivity.settlement.getPayout().getMPercent() + "%");
        if (MainActivity.settlement.getPayout().getPCpm() < 10)
            p_cpm_view.setText("CPM: 0" + MainActivity.settlement.getPayout().getPCpm() + "¢");
        else
            p_cpm_view.setText("CPM: " + MainActivity.settlement.getPayout().getPCpm() + "¢");
        if (MainActivity.settlement.getPayout().getMCpm() < 10)
            m_cpm_view.setText("CPM: 0" + MainActivity.settlement.getPayout().getPCpm() + "¢");
        else
            m_cpm_view.setText("CPM: " + MainActivity.settlement.getPayout().getMCpm() + "¢");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }
}