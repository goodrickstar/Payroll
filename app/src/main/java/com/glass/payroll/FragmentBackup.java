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
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FragmentBackup extends Fragment {
    private MI MI;
    private FragmentBackupBinding binding;
    private MainViewModel model;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    }

    StorageReference ref = FirebaseStorage.getInstance().getReference().child("backups").child(MainActivity.user.getUid()).child(MainActivity.user.getUid() + ".txt");

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
            MI.vibrate();
            backupDatabaseToStorage();
        });
        binding.restoreButton.setOnClickListener(view -> {
            MI.vibrate();
            restoreDatabaseFromStorage();
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MI = null;
    }

    private void backupDatabaseToStorage() {
        MainActivity.executor.execute(() -> {
            List<Settlement> records = model.getAllSettlements();
            UploadTask uploadTask = ref.putStream(new ByteArrayInputStream(new Gson().toJson(records).getBytes()));
            uploadTask.addOnFailureListener(exception -> MI.showSnack(exception.getMessage(), Snackbar.LENGTH_INDEFINITE)).addOnSuccessListener(taskSnapshot -> MI.showSnack("Backup Complete!", Snackbar.LENGTH_INDEFINITE));
        });


    }

    private void restoreDatabaseFromStorage() {
        final long ONE_MEGABYTE = 1024 * 1024;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            model.restore(Utils.returnSettlementArray(new String(bytes, StandardCharsets.UTF_8)));
            MI.showSnack("Restore Complete!", Snackbar.LENGTH_INDEFINITE);
            checkUserBackups();
        }).addOnFailureListener(exception -> MI.showSnack(exception.getMessage(), Snackbar.LENGTH_INDEFINITE));
    }

    private void checkUserBackups() {
        FirebaseStorage.getInstance().getReference().child("backups").child(MainActivity.user.getUid()).listAll()
                .addOnSuccessListener(listResult -> {
                    if (listResult.getItems().isEmpty()) {
                        MI.showSnack("No backup found", Snackbar.LENGTH_SHORT);
                    }
                    binding.restoreButton.setEnabled(!listResult.getItems().isEmpty());
                })
                .addOnFailureListener(e -> Log.i("testing", e.getMessage()));
    }


}