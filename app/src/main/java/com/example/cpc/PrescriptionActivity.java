package com.example.cpc;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class PrescriptionActivity extends AppCompatActivity {

    EditText etPatientId, etMedication, etDosage, etInstructions;
    String doctorId = "";
    final String BASE_URL = "http://10.21.148.28/clinic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        etPatientId = findViewById(R.id.et_patient_id);
        etMedication = findViewById(R.id.et_medication);
        etDosage = findViewById(R.id.et_dosage);
        etInstructions = findViewById(R.id.et_instructions);

        String passedId = getIntent().getStringExtra("patient_id");
        doctorId = String.valueOf(getIntent().getIntExtra("doctor_id", 0));

        if (passedId != null && !passedId.isEmpty()) {
            etPatientId.setText(passedId);
            etPatientId.setEnabled(false);
            etPatientId.setFocusable(false);
        }
    }

    public void submitPrescription(View view) {
        String patientId = etPatientId.getText().toString().trim();
        String medication = etMedication.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        if (patientId.isEmpty() || medication.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/submitPrescription.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Prescription submitted!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Submission failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patient_id", patientId);
                params.put("doctor_id", doctorId); // ✅ dynamic doctor ID sent
                params.put("medication", medication);
                params.put("dosage", dosage);
                params.put("instructions", instructions);
                return params;
            }
        };

        queue.add(request);
    }
}