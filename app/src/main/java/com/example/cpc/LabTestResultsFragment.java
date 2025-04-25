package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LabTestResultsFragment extends Fragment implements RefreshableFragment {

    private final String BASE_URL = "http://10.21.134.17/clinic";
    private String technicianId;
    private RecyclerView labResultsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lab_test_results, container, false);
        labResultsRecyclerView = view.findViewById(R.id.rv_lab_results);
        labResultsRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            technicianId = getArguments().getString("user_id", "");
        }

        fetchLabTests();
        return view;
    }

    private void fetchLabTests() {
        String url = BASE_URL + "/getPendingLabTests.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<LabTestRecord> records = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            records.add(new LabTestRecord(
                                    obj.getInt("record_id"),
                                    obj.getInt("patient_id"),
                                    obj.getInt("doctor_id"),
                                    obj.getString("content")
                            ));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    LabTestResultsAdapter adapter = new LabTestResultsAdapter(getContext(), records);
                    labResultsRecyclerView.setAdapter(adapter);
                },
                error -> Toast.makeText(getContext(), "Error loading lab tests", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }


    private void checkIfGenerated(int recordId, ResultGeneratedCallback callback) {
        String url = BASE_URL + "/checkGeneratedResult.php?record_id=" + recordId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> callback.onCheck(response.length() > 0),
                error -> callback.onCheck(false));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void generateResult(int recordId, int patientId, int doctorId, String originalContent) {
        String result = originalContent + "\n\nResult: (Pending content...)";
        String url = BASE_URL + "/generateLabResult.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        Toast.makeText(getContext(), "Result generated!", Toast.LENGTH_SHORT).show();
                        fetchLabTests();
                    } else {
                        Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("record_id", String.valueOf(recordId));
                params.put("patient_id", String.valueOf(patientId));
                params.put("doctor_id", String.valueOf(doctorId));
                params.put("content", result);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void submitResult(int recordId) {
        String url = BASE_URL + "/submitLabResult.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        Toast.makeText(getContext(), "Result submitted!", Toast.LENGTH_SHORT).show();
                        fetchLabTests();
                    } else {
                        Toast.makeText(getContext(), "Submit failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("record_id", String.valueOf(recordId));
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    interface ResultGeneratedCallback {
        void onCheck(boolean isGenerated);
    }

    @Override
    public void onRefresh() {
        fetchLabTests();
    }
}
