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

public class FragmentWelcome extends Fragment {
    private Context context;
    private final ArrayList<String> tips = new ArrayList<>();
    public FragmentWelcome() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tips.add("Created for owner operators and lease operators to track, store, and evaluate their performance");
        tips.add("Track and log revenue and expenses with one easy interface");
        tips.add("Access the same settlements across multiple devices");
        tips.add("Settlements are backed up by Payroll servers allowing access to records at a later date");
        tips.add("Log in with any Google account to get started");

        return inflater.inflate(R.layout.fragment_welcome, container, false);
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
    public void onAttach(@NonNull Context context) {
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
