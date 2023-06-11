package com.glass.payroll;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentEquipmentBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FragmentEquipment extends Fragment implements View.OnClickListener {
    private Context context;
    private MI MI;
    private FragmentEquipmentBinding binding;
    private MainViewModel model;
    private List<Truck> trucks = new ArrayList<>();
    private List<Trailer> trailers = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentEquipment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEquipmentBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        final TruckAdapter truckAdapter = new TruckAdapter();
        binding.truckRecycler.setHasFixedSize(true);
        binding.truckRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.truckRecycler.setAdapter(truckAdapter);
        final TrailerAdapter trailerAdapter = new TrailerAdapter();
        binding.trailerRecycler.setHasFixedSize(true);
        binding.trailerRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.trailerRecycler.setAdapter(trailerAdapter);
        binding.addTruck.setOnClickListener(this);
        binding.addTrailer.setOnClickListener(this);
        model.trucks().observe(getViewLifecycleOwner(), trucks -> {
            FragmentEquipment.this.trucks = trucks;
            truckAdapter.notifyDataSetChanged();
        });
        model.trailers().observe(getViewLifecycleOwner(), trailers -> {
            FragmentEquipment.this.trailers = trailers;
            trailerAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        MI = (MI) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    @Override
    public void onClick(View view) {
        MI.vibrate(view);
        switch (view.getId()) {
            case R.id.add_truck:
                model.add(new Truck(MainActivity.user.getUid(), "1946"));
                if (trucks.size() == 0) MI.handleGrouping();
                break;
            case R.id.add_trailer:
                model.add(new Trailer(MainActivity.user.getUid(), "531767"));
                break;
        }
    }

    private class TruckAdapter extends RecyclerView.Adapter<TruckAdapter.viewHolder> {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.fixed_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            Truck truck = trucks.get(position);
            String label = String.valueOf(truck.getId());
            if (position == 0) label = label + " (current)";
            holder.label.setText(label);
            holder.cost.setText(truck.getOdometer() + "m");
        }

        @Override
        public int getItemCount() {
            return trucks.size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView label;
            final TextView cost;

            viewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                cost = itemView.findViewById(R.id.cost);
            }
        }
    }

    private class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.viewHolder> {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.fixed_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            final Trailer trailer = trailers.get(position);
            String label = String.valueOf(trailer.getId());
            if (position == 0) label = label + " (current)";
            holder.label.setText(label);
        }

        @Override
        public int getItemCount() {
            return trailers.size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView label;
            final TextView cost;

            viewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                cost = itemView.findViewById(R.id.cost);
            }
        }
    }

}