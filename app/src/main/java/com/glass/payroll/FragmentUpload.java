package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentUploadBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.util.List;

public class FragmentUpload extends DialogFragment {
    private MI MI;
    private MainViewModel model;
    private FragmentUploadBinding binding;
    private boolean uploading = false;

    private boolean done = false;

    final StorageReference ref = FirebaseStorage.getInstance().getReference().child("backups").child(MainActivity.user.getUid());

    public FragmentUpload() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        binding.finish.setText("Start Upload");
        binding.finish.setOnClickListener(view -> {
            MI.vibrate();
            dismiss();
        });
        binding.finish.setText("Hide");
        binding.imageView3.setVisibility(View.INVISIBLE);
        binding.progressBar2.setVisibility(View.VISIBLE);
        binding.info4.setVisibility(View.VISIBLE);
        binding.info4.setText("In Progress");
        if (!uploading && !done) {
            uploading = true;
            model.executor().execute(() -> {
                final List<Settlement> settlements = model.getSettlements();
                final List<Trailer> trailers = model.getTrailers();
                final List<Truck> trucks = model.getTrucks();
                final int[] x = {0};
                OnSuccessListener<UploadTask.TaskSnapshot> successListener = taskSnapshot -> {
                    x[0]++;
                    binding.progressBar3.setProgress(x[0]);
                    switch (taskSnapshot.getStorage().getName()){
                        case "settlements.txt":
                            binding.info1.setText("Settlements Uploaded");
                            binding.info1.setVisibility(View.VISIBLE);
                            break;
                        case "trucks.txt":
                            binding.info2.setText("Trucks Uploaded");
                            binding.info2.setVisibility(View.VISIBLE);
                            break;
                        case "trailers.txt":
                            binding.info3.setText("Trailers Uploaded");
                            binding.info3.setVisibility(View.VISIBLE);
                            break;
                    }
                    if (x[0]==3) {
                        binding.progressBar2.setVisibility(View.INVISIBLE);
                        binding.imageView3.setVisibility(View.VISIBLE);
                        binding.info4.setText("Back Up Complete!");
                        binding.finish.setText("Close");
                        uploading = false;
                        done = true;
                    }
                };
                ref.child("settlements.txt").putStream(new ByteArrayInputStream(new Gson().toJson(settlements).getBytes())).addOnSuccessListener(successListener);
                ref.child("trucks.txt").putStream(new ByteArrayInputStream(new Gson().toJson(trucks).getBytes())).addOnSuccessListener(successListener);
                ref.child("trailers.txt").putStream(new ByteArrayInputStream(new Gson().toJson(trailers).getBytes())).addOnSuccessListener(successListener);
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

}
