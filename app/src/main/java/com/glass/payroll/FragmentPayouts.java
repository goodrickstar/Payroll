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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentFixedBinding;
import com.glass.payroll.databinding.FragmentPayoutsBinding;

public class FragmentPayouts extends Fragment {
    private FragmentPayoutsBinding binding;
    private MainViewModel model;
    private Settlement settlement;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }
    public FragmentPayouts() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPayoutsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        model.executor().execute(() -> {
            FragmentPayouts.this.settlement = model.getSettlement();
            getActivity().runOnUiThread(() -> {
                binding.pPercentSeekbar.setProgress(settlement.getPayout().getPPercent());
                binding.mPercentSeekbar.setProgress(settlement.getPayout().getMPercent());
                binding.pCpmSeekbar.setProgress(settlement.getPayout().getPCpm());
                binding.mCpmSeekbar.setProgress(settlement.getPayout().getMCpm());
                if (settlement.getPayout().getPPercent() < 10)
                    binding.pPercentView.setText("PCT: 0" + settlement.getPayout().getPPercent() + "%");
                else
                    binding.pPercentView.setText("PCT: " + settlement.getPayout().getPPercent() + "%");
                if (settlement.getPayout().getMPercent() < 10)
                    binding.mPercentView.setText("PCT: 0" + settlement.getPayout().getMPercent() + "%");
                else
                    binding.mPercentView.setText("PCT: " + settlement.getPayout().getMPercent() + "%");
                if (settlement.getPayout().getPCpm() < 10)
                    binding.pCpmView.setText("CPM: 0" + settlement.getPayout().getPCpm() + "¢");
                else
                    binding.pCpmView.setText("CPM: " + settlement.getPayout().getPCpm() + "¢");
                if (settlement.getPayout().getMCpm() < 10)
                    binding.mCpmView.setText("CPM: 0" + settlement.getPayout().getPCpm() + "¢");
                else
                    binding.mCpmView.setText("CPM: " + settlement.getPayout().getMCpm() + "¢");
                SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean user) {
                        if (!user) return;
                        switch (seekBar.getId()) {
                            case R.id.p_percent_seekbar:
                                if (progress < 10)
                                    binding.pPercentView.setText("PCT: 0" + progress + "%");
                                else
                                    binding.pPercentView.setText("PCT: " + progress + "%");
                                break;
                            case R.id.m_percent_seekbar:
                                if (progress < 10)
                                    binding.mPercentView.setText("PCT: 0" + progress + "%");
                                else
                                    binding.mPercentView.setText("PCT: " + progress + "%");
                                break;
                            case R.id.p_cpm_seekbar:
                                if (progress < 10)
                                    binding.pCpmView.setText("CPM: 0" + progress + "¢");
                                else
                                    binding.pCpmView.setText("CPM: " + progress + "¢");
                                break;
                            case R.id.m_cpm_seekbar:
                                if (progress < 10)
                                    binding.mCpmView.setText("CPM: 0" + progress + "¢");
                                else
                                    binding.mCpmView.setText("CPM: " + progress + "¢");
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
                                settlement.getPayout().setPPercent(seekBar.getProgress());
                                break;
                            case R.id.m_percent_seekbar:
                                settlement.getPayout().setMPercent(seekBar.getProgress());
                                break;
                            case R.id.p_cpm_seekbar:
                                settlement.getPayout().setPCpm(seekBar.getProgress());
                                break;
                            case R.id.m_cpm_seekbar:
                                settlement.getPayout().setMCpm(seekBar.getProgress());
                                break;
                        }
                        model.add(Utils.calculate(settlement));
                    }
                };
                binding.pPercentSeekbar.setOnSeekBarChangeListener(listener);
                binding.pCpmSeekbar.setOnSeekBarChangeListener(listener);
                binding.mCpmSeekbar.setOnSeekBarChangeListener(listener);
                binding.mPercentSeekbar.setOnSeekBarChangeListener(listener);
            });
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}