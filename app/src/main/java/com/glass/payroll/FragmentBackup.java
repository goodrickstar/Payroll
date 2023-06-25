package com.glass.payroll;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.glass.payroll.databinding.FragmentBackupBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class FragmentBackup extends Fragment {
    private MI MI;
    private FragmentBackupBinding binding;
    private MainViewModel model;
    final StorageReference ref = FirebaseStorage.getInstance().getReference().child("backups").child(MainActivity.user.getUid());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    public FragmentBackup() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MI = (MI) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBackupBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        checkUserBackups();
        binding.backupButton.setOnClickListener(view -> {
            Utils.vibrate(view);
            FragmentUpload upload = (FragmentUpload) getParentFragmentManager().findFragmentByTag("upload");
            if (upload != null) return;
            upload = new FragmentUpload();
            upload.setCancelable(false);
            upload.show(getParentFragmentManager(), "upload");
        });
        binding.restoreButton.setOnClickListener(view -> {
            Utils.vibrate(view);
            restoreDatabaseFromStorage();
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    private void restoreDatabaseFromStorage() {
        final long ONE_MEGABYTE = 1024 * 1024;
        ref.child("settlements.txt").getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            model.emptyTables();
            ArrayList<Settlement> settlements = Utils.returnSettlementArray(new String(bytes, StandardCharsets.UTF_8));
            model.add(settlements);
            model.add(settlements.get(0));
            ref.child("trucks.txt").getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(trucks -> {
                        model.addTrucks(Utils.returnTruckArray(new String(trucks, StandardCharsets.UTF_8)));
                        ref.child("trailers.txt").getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener(trailers -> {
                                    model.addTrailers(Utils.returnTrailerArray(new String(trailers, StandardCharsets.UTF_8)));
                                    if (MI != null){
                                        MI.showSnack("Restore Complete!", Snackbar.LENGTH_INDEFINITE);
                                        MI.navigate(R.id.overview);
                                    }
                                });
                    });
        });
    }

    private void checkUserBackups() {
        FirebaseStorage.getInstance().getReference().child("backups").child(MainActivity.user.getUid()).listAll()
                .addOnSuccessListener(listResult -> {
                    if (listResult.getItems().isEmpty()) {
                        if (MI != null) {
                            MI.showSnack("No backup found", Snackbar.LENGTH_SHORT);
                        }
                    }
                    binding.restoreButton.setEnabled(!listResult.getItems().isEmpty());
                })
                .addOnFailureListener(e -> Log.i("testing", e.getMessage()));
    }

}