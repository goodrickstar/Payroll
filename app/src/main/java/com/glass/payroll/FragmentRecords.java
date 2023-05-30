package com.glass.payroll;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class FragmentRecords extends Fragment {
    private final RecycleAdapter recyclerAdapter = new RecycleAdapter();
    private MI MI;
    private final Calendar calendar = Calendar.getInstance();

    public FragmentRecords() {
    }

    private void deleteSettlement(Long id) {
        if (MI != null) {
            MainActivity.executor.execute(() -> {
                MainActivity.records.daoData().deleteDataRecord(id);
                getActivity().runOnUiThread(() -> {
                    boolean reload = MainActivity.settlements.get(MI.indexSettlement(id)).getId() == (MainActivity.settlement.getId());
                    MainActivity.settlements.remove(MI.indexSettlement(id));
                    recyclerAdapter.notifyDataSetChanged();
                    MI.showSnack("Settlement Permanently Deleted", Snackbar.LENGTH_SHORT);
                    if (reload && !MainActivity.settlements.isEmpty()) {
                        MainActivity.settlement = MainActivity.settlements.get(0);
                    }
                    MI.calculate();
                    getContext().getSharedPreferences("settings", MODE_PRIVATE).edit().putString(MainActivity.user.getUid(), MainActivity.gson.toJson(MainActivity.settlements)).apply();
                });
            });


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        ExpandableListView RecordsList = v.findViewById(R.id.recycler);
        RecordsList.setAdapter(recyclerAdapter);
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
            return MainActivity.settlements.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return 1;
        }

        @Override
        public Settlement getGroup(int i) {
            return MainActivity.settlements.get(i);
        }

        @Override
        public Settlement getChild(int i, int i1) {
            return MainActivity.settlements.get(i);
        }

        @Override
        public long getGroupId(int i) {
            return MainActivity.settlements.get(i).getId();
        }

        @Override
        public long getChildId(int i, int i1) {
            return MainActivity.settlements.get(i).getId();
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
            if (settlement.getId() == (MainActivity.settlement.getId()))
                holder.average.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR) + " (current)");
            else
                holder.average.setText("Week " + calendar.get(Calendar.WEEK_OF_YEAR));
            holder.date.setText(Utils.range(settlement.getStart(), settlement.getStop()));
            holder.miles.setText(Utils.formatInt(settlement.getMiles()) + "m");
            holder.balance.setText(Utils.formatValueToCurrency((double) settlement.getBalance()).replace(".00", ""));
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
            holder.netRate.setText("Net CPM: " + Utils.formatValueToCurrency((double) settlement.getBalance() / settlement.getMiles()));
            holder.loads.setText("Loads: " + Utils.formatInt(settlement.getLoads().size()));
            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            Settlement settlement = (Settlement) view.getTag();
            if (MI == null) return;
            MI.vibrate();
            switch (view.getId()) {
                case R.id.delete:
                    deleteSettlement(settlement.getId());
                    break;
                case R.id.edit:
                    MainActivity.settlement = settlement;
                    MI.navigate(1);
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