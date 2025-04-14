package com.example.cpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientRecordsFragment extends Fragment implements RefreshableFragment{

    private RecyclerView recyclerView;
    private PatientAdapter adapter;
    private final String BASE_URL = "http://10.21.186.199/clinic";
    private final int doctorId = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_records, container, false);

        recyclerView = rootView.findViewById(R.id.rv_patients);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchPatientsFromBackend();

        return rootView;
    }

    private void fetchPatientsFromBackend() {
        String url = BASE_URL + "/getPatients.php?doctor_id=" + doctorId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<Patient> patients = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String name = obj.getString("name");
                            String id = obj.getString("id");
                            String reason = obj.getString("reason");
                            patients.add(new Patient(name, id, reason));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter = new PatientAdapter(getContext(), patients, BASE_URL, doctorId);
                    recyclerView.setAdapter(adapter);
                },
                error -> {
                    Toast.makeText(getContext(), "Failed to fetch patients", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }
    @Override
    public void onRefresh() {
        fetchPatientsFromBackend();
    }
}
