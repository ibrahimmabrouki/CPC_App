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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class PharmacistPrescriptionFragment extends Fragment implements RefreshableFragment {

    private final String BASE_URL = "http://10.21.148.28/clinic";
    private String pharmacistId = "";
    private LinearLayout prescriptionContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescriptions_ph, container, false);
        prescriptionContainer = view.findViewById(R.id.prescriptionContainer);

        if (getArguments() != null) {
            pharmacistId = getArguments().getString("user_id", "");
        }

        fetchPrescriptions();
        return view;
    }

    private void fetchPrescriptions() {
        String url = BASE_URL + "/getPendingPrescriptions.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    prescriptionContainer.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject pres = response.getJSONObject(i);
                            int recordId = pres.getInt("record_id");
                            int patientId = pres.getInt("patient_id");

                            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_prescription_ph, prescriptionContainer, false);

                            TextView tvPatient = item.findViewById(R.id.tvPatientId);
                            Button btnView = item.findViewById(R.id.btnViewRecord);
                            Button btnComplete = item.findViewById(R.id.btnMarkCompleted);

                            tvPatient.setText("Patient ID: " + patientId);

                            btnView.setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), PrescriptionDetailActivity.class);
                                intent.putExtra("record_id", recordId);
                                startActivity(intent);
                            });

                            btnComplete.setOnClickListener(v -> {
                                markPrescriptionAsCompleted(recordId, patientId);
                                prescriptionContainer.removeView(item);
                            });

                            prescriptionContainer.addView(item);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching prescriptions", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void markPrescriptionAsCompleted(int recordId, int patientId) {
        String url = BASE_URL + "/markPrescriptionCompleted.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        Toast.makeText(getContext(), "Prescription marked as completed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("record_id", String.valueOf(recordId));
                params.put("patient_id", String.valueOf(patientId));
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onRefresh() {
        fetchPrescriptions();
    }
}
