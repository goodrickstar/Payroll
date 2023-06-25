package com.glass.payroll;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentEquipmentBinding;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        Utils.vibrate(view);
        switch (view.getId()) {
            case R.id.add_truck:
                model.add(new Truck(MainActivity.user.getUid(), String.valueOf(new Random().nextInt(20000)+1000)));
                if (trucks.size() == 0) MI.handleGrouping();
                break;
            case R.id.add_trailer:
                model.add(new Trailer(MainActivity.user.getUid(), String.valueOf(new Random().nextInt(20000)+1000)));
                break;
        }
    }

    private class TruckAdapter extends RecyclerView.Adapter<viewHolder> implements View.OnClickListener{
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.equipment_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            Truck truck = trucks.get(position);
            if (MainActivity.truck != null){
                if (truck.getId().equals(MainActivity.truck.getId())) holder.button.setVisibility(View.VISIBLE);
                else holder.button.setVisibility(View.INVISIBLE);
            } else holder.button.setVisibility(View.INVISIBLE);
            holder.id.setText(truck.getId());
            holder.extra.setText(Utils.formatInt(truck.getOdometer()) + " m");
            holder.itemView.setTag(truck);
            holder.itemView.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return trucks.size();
        }

        @Override
        public void onClick(View view) {
            Utils.vibrate(view);
            final Truck truck = (Truck) view.getTag();
            if (MainActivity.truck != null){
                if (!truck.getId().equals(MainActivity.truck.getId())) {
                    model.add(truck);
                    if (MI != null) MI.showSnack("Tractor set to " + truck.getId(), Snackbar.LENGTH_LONG);
                }
            }
        }

    }

    private class TrailerAdapter extends RecyclerView.Adapter<viewHolder> implements View.OnClickListener {
        @NotNull
        @Override
        public viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.equipment_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            final Trailer trailer = trailers.get(position);
            if (MainActivity.trailer != null){
                if (trailer.getId().equals(MainActivity.trailer.getId())) holder.button.setVisibility(View.VISIBLE);
                else holder.button.setVisibility(View.INVISIBLE);
            } else holder.button.setVisibility(View.INVISIBLE);
            holder.id.setText(trailer.getId());
            holder.extra.setText(trailer.getCarrier());
            holder.itemView.setTag(trailer);
            holder.itemView.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return trailers.size();
        }


        @Override
        public void onClick(View view) {
            Utils.vibrate(view);
            final Trailer trailer = (Trailer) view.getTag();
            if (MainActivity.trailer != null){
                if (!trailer.getId().equals(MainActivity.trailer.getId())) {
                    model.add(trailer);
                    if (MI != null) MI.showSnack("Trailer set to " + trailer.getId(), Snackbar.LENGTH_LONG);
                }
            }
        }
    }


    static class viewHolder extends RecyclerView.ViewHolder {
        final TextView id;
        final TextView extra;

        final ImageView button;

        viewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.equipment_id);
            button = itemView.findViewById(R.id.equipment_button);
            extra = itemView.findViewById(R.id.equipment_extra);
        }
    }
}