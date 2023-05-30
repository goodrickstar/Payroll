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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentIntro extends Fragment {
    private final ArrayList<String> tips = new ArrayList<>();
    private Context context;

    public FragmentIntro() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tips.add("Start by creating a new settlement");
        tips.add("Setup a carrier PAYOUT to be paid to your carrier or factoring company");
        tips.add("Setup the amount paid towards a MAINTENANCE account or leasing contract");
        tips.add("Setup any FIXED EXPENSES that accrue each settlement period");
        tips.add("That's it, setup is complete! These will be automatically transfered to later settlements");
        tips.add("Throughout each settlement period you will add LOADS that will be payable and FUEL purchases billable to that period");
        tips.add("Wash-outs, scale tickets, repair bills, cash advances, tolls, and any other occasional costs can be added as MISCELLANEOUS");
        tips.add("Mileage is calculated via odometer readings and not the estimated miles when adding load entries, so update odometer readings often");
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        RecycleAdapter recycleAdapter = new RecycleAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(recycleAdapter);
        recycleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.viewHolder> {
        @NotNull
        @Override
        public RecycleAdapter.viewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new RecycleAdapter.viewHolder(getLayoutInflater().inflate(R.layout.tip_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull RecycleAdapter.viewHolder holder, int position) {
            holder.tip.setText(tips.get(position));
        }

        @Override
        public int getItemCount() {
            return tips.size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            final TextView tip;

            viewHolder(View itemView) {
                super(itemView);
                tip = itemView.findViewById(R.id.tip);
            }
        }
    }
}
