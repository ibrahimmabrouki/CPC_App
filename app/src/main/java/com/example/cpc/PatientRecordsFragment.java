package com.example.cpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class PatientRecordsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_records, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.rv_patients);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<Patient> patients = Arrays.asList(
                new Patient("Jana Kamel", "6002", "Follow-up"),
                new Patient("Ranim Ali", "6000", "Routine"),
                new Patient("Adam Itani", "5555", "Consultation"),
                new Patient("Abdullah Jrad", "7691", "Follow-up")
        );

        PatientAdapter adapter = new PatientAdapter(getContext(), patients);
        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
