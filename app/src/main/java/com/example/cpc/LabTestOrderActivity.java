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

public class LabTestOrderActivity extends AppCompatActivity {

    EditText etPatientId, etTestName, etReason, etInstructions;
    final String BASE_URL = "http://10.21.148.28/clinic";
    int doctorId;
    int appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_test_order);

        etPatientId = findViewById(R.id.et_patient_id);
        etTestName = findViewById(R.id.et_test_name);
        etReason = findViewById(R.id.et_reason);
        etInstructions = findViewById(R.id.et_instructions);

        String passedId = getIntent().getStringExtra("patient_id");
        doctorId = getIntent().getIntExtra("doctor_id", 7);
        appointmentId = getIntent().getIntExtra("appointment_id", 0);

        if (passedId != null && !passedId.isEmpty()) {
            etPatientId.setText(passedId);
            etPatientId.setEnabled(false);
            etPatientId.setFocusable(false);
        }
    }

    public void submitLabTestOrder(View view) {
        String patientId = etPatientId.getText().toString().trim();
        String testName = etTestName.getText().toString().trim();
        String reason = etReason.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        if (patientId.isEmpty() || testName.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/submitLabTestOrder.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Lab Test Order submitted successfully!", Toast.LENGTH_LONG).show();
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
                params.put("doctor_id", String.valueOf(doctorId));
                params.put("test_name", testName);
                params.put("reason", reason);
                params.put("instructions", instructions);
                params.put("appointment_id", String.valueOf(appointmentId));
                return params;
            }
        };

        queue.add(request);
    }
}
