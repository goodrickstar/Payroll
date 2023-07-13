package com.glass.payroll;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentMaintenanceBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;
public class FragmentMaintenance extends Fragment implements View.OnClickListener {
    private Context context;
    private MI MI;
    private FragmentMaintenanceBinding binding;
    private MainViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentMaintenance() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMaintenanceBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        model.trucks().observe(getViewLifecycleOwner(), trucks -> {
            if (trucks != null) {
                SpinAdapter adapter = new SpinAdapter(context, R.layout.spinner_view, trucks);
                binding.spinner.setAdapter(adapter);
                binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        final Truck truck = adapter.getItem(i);
                        binding.addButton.setTag(truck);
                        model.workOrders(truck).observe(getViewLifecycleOwner(), workOrders -> binding.recycler.setAdapter(new MaintenanceAdapter(workOrders, truck)));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                binding.spinnerIcon.setOnClickListener(view -> {
                    Utils.vibrate(view);
                    binding.spinner.performClick();
                });
                binding.addButton.setOnClickListener(this);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        MI = (MI) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    @Override
    public void onClick(View view) {
        Utils.vibrate(view);
        Truck truck = (Truck) view.getTag();
        WorkOrder workOrder = new WorkOrder(MainActivity.user.getUid(), truck.getId(), 500000, "Oil Change (T6)", 60000);
        model.add(workOrder);
    }

    private class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.viewHolder> implements View.OnClickListener {
        final List<WorkOrder> workOrders;
        final Truck truck;

        public MaintenanceAdapter(List<WorkOrder> workOrders, Truck truck) {
            this.workOrders = workOrders;
            this.truck = truck;
        }

        @NotNull
        @Override
        public MaintenanceAdapter.viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new MaintenanceAdapter.viewHolder(getLayoutInflater().inflate(R.layout.work_order_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull viewHolder holder, int position) {
            WorkOrder workOrder = workOrders.get(position);
            holder.label.setText(workOrder.getLabel());
            holder.dueIn.setText(Utils.formatInt(workOrder.getReading() - truck.getOdometer()) + " m");
            holder.target.setText(Utils.formatInt(workOrder.getReading()) + " m");
            holder.itemView.setTag(workOrder);
            holder.itemView.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return workOrders.size();
        }

        @Override
        public void onClick(View view) {
            Utils.vibrate(view);
            final WorkOrder workOrder = (WorkOrder) view.getTag();
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    workOrder.setReading(workOrder.getReading() + workOrder.getInterval());
                    model.add(workOrder);
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Mark " + workOrder.getLabel() + " as completed?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView label;
            final TextView dueIn;
            final TextView target;

            viewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.order_label_view);
                dueIn = itemView.findViewById(R.id.due_in_view);
                target = itemView.findViewById(R.id.target_view);
            }
        }
    }
    public static class SpinAdapter extends ArrayAdapter<Truck> {
        // Your sent context
        private final Context context;
        // Your custom values for the spinner (User)
        private final List<Truck> trucks;

        public SpinAdapter(Context context, int textViewResourceId, List<Truck> values) {
            super(context, textViewResourceId, values);
            this.context = context;
            this.trucks = values;
        }

        @Override
        public int getCount() {
            return trucks.size();
        }

        @Override
        public Truck getItem(int position) {
            return trucks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // And the "magic" goes here
        // This is for the "passive" state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.spinner_view, null);
            TextView textView = v.findViewById(R.id.item_label);
            textView.setText(trucks.get(position).getId());
            return v;
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.spinner_view, null);
            TextView textView = v.findViewById(R.id.item_label);
            textView.setText(trucks.get(position).getId());
            return v;
        }
    }
}