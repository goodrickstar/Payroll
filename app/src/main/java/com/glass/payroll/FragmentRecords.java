package com.glass.payroll;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.glass.payroll.databinding.FragmentRecordsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentRecords extends Fragment {
    private MI MI;
    private final Calendar calendar = Calendar.getInstance();
    private FragmentRecordsBinding binding;
    private MainViewModel model;
    private List<Settlement> settlements = new ArrayList<>();
    private Settlement settlement = new Settlement();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentRecords() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecordsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        RecycleAdapter adapter = new RecycleAdapter();
        binding.recycler.setAdapter(adapter);
        model.settlement().observe(getViewLifecycleOwner(), settlement -> {
            FragmentRecords.this.settlement = settlement;
            model.getAllSettlements().observe(getViewLifecycleOwner(), settlements -> {
                FragmentRecords.this.settlements = settlements;
                adapter.notifyDataSetChanged();
            });
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    private class RecycleAdapter extends BaseExpandableListAdapter implements View.OnClickListener {

        @Override
        public int getGroupCount() {
            return settlements.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return 1;
        }

        @Override
        public Settlement getGroup(int i) {
            return settlements.get(i);
        }

        @Override
        public Settlement getChild(int i, int i1) {
            return settlements.get(i);
        }

        @Override
        public long getGroupId(int i) {
            return settlements.get(i).getId();
        }

        @Override
        public long getChildId(int i, int i1) {
            return settlements.get(i).getId();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            GroupViewHolder holder;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.record_row, viewGroup, false);
                holder = new GroupViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (GroupViewHolder) view.getTag();
            }
            Settlement settlement = getGroup(i);
            String weekString = "Week " + settlement.getWeek();
            if (settlement.getId() == (FragmentRecords.this.settlement.getId()))
                weekString = weekString + " (current)";
            holder.average.setText(weekString);
            holder.date.setText(Utils.range(settlement.getStart(), settlement.getStop()));
            holder.miles.setText(Utils.formatInt(settlement.getEmptyMiles() + settlement.getLoadedMiles()) + "m");
            holder.balance.setText("Bal: " + Utils.formatValueToCurrencyWhole(settlement.getBalance()));
            calendar.setTimeInMillis(settlement.getStart());
            //holder.average.setText(Utils.formatDoubleToCurrency((double) settlement.getBalance() / settlement.getMiles()) + " cpm");
            return view;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            ChildViewHolder holder;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.record_child, viewGroup, false);
                holder = new ChildViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ChildViewHolder) view.getTag();
            }
            Settlement settlement = getChild(i, i1);
            holder.delete.setTag(settlement);
            holder.edit.setTag(settlement);
            holder.delete.setOnClickListener(this);
            holder.edit.setOnClickListener(this);
            int grossRevenue = 0;
            int loadedMiles = 0;
            int emptyMiles = 0;
            for (Load load : settlement.getLoads()) {
                grossRevenue += load.getRate();
                loadedMiles += load.getLoaded();
                emptyMiles += load.getEmpty();
            }
            holder.grossRevenue.setText("Gross Revenue: $" + Utils.formatInt(grossRevenue));
            holder.loadedMiles.setText("Loaded Miles: " + Utils.formatInt(loadedMiles));
            holder.emptyMiles.setText("Empty Miles: " + Utils.formatInt(emptyMiles));
            holder.loadedRate.setText("Loaded Rate: " + Utils.formatValueToCurrency((double) grossRevenue / loadedMiles));
            holder.netRate.setText("Net CPM: " + Utils.formatValueToCurrency(settlement.getBalance() / (settlement.getEmptyMiles() + settlement.getLoadedMiles())));
            holder.loads.setText("Loads: " + Utils.formatInt(settlement.getLoads().size()));
            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }

        @Override
        public void onClick(View view) {
            Settlement settlement = (Settlement) view.getTag();
            MI.vibrate(view);
            switch (view.getId()) {
                case R.id.delete:
                    model.delete(settlement);
                    MI.showSnack("Settlement Permanently Deleted", Snackbar.LENGTH_SHORT);
                    break;
                case R.id.edit:
                    model.add(settlement);
                    MI.navigate(2);
                    break;
            }
        }


        class GroupViewHolder extends RecyclerView.ViewHolder {
            final TextView date;
            final TextView balance;
            final TextView miles;
            final TextView average;

            GroupViewHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
                balance = itemView.findViewById(R.id.balance);
                miles = itemView.findViewById(R.id.miles);
                average = itemView.findViewById(R.id.avg);
            }
        }

        class ChildViewHolder extends RecyclerView.ViewHolder {
            TextView delete, edit;
            TextView grossRevenue, loadedMiles, emptyMiles;
            TextView loadedRate, netRate, loads;

            ChildViewHolder(View v) {
                super(v);
                delete = v.findViewById(R.id.delete);
                edit = v.findViewById(R.id.edit);
                grossRevenue = v.findViewById(R.id.grossRevenue);
                loadedMiles = v.findViewById(R.id.loadedMiles);
                emptyMiles = v.findViewById(R.id.emptyMiles);
                loadedRate = v.findViewById(R.id.loadedRate);
                netRate = v.findViewById(R.id.netRate);
                loads = v.findViewById(R.id.loads);
            }
        }
    }
}