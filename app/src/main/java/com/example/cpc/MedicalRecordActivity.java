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

public class MedicalRecordActivity extends AppCompatActivity {

    EditText etPatientId, etDiagnosis, etSymptoms, etNotes;
    final String BASE_URL = "http://10.21.148.28/clinic";
    int doctorId;
    int appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        etPatientId = findViewById(R.id.et_patient_id);
        etDiagnosis = findViewById(R.id.et_diagnosis);
        etSymptoms = findViewById(R.id.et_symptoms);
        etNotes = findViewById(R.id.et_notes);

        String passedId = getIntent().getStringExtra("patient_id");
        doctorId = getIntent().getIntExtra("doctor_id", 0);
        appointmentId = getIntent().getIntExtra("appointment_id", 0);

        if (passedId != null && !passedId.isEmpty()) {
            etPatientId.setText(passedId);
            etPatientId.setEnabled(false);
            etPatientId.setFocusable(false);
        }
    }

    public void submitMedicalRecord(View view) {
        String id = etPatientId.getText().toString().trim();
        String diagnosis = etDiagnosis.getText().toString().trim();
        String symptoms = etSymptoms.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (id.isEmpty() || diagnosis.isEmpty() || symptoms.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/submitMedicalRecord.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Medical record submitted successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Submission failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patient_id", id);
                params.put("diagnosis", diagnosis);
                params.put("symptoms", symptoms);
                params.put("notes", notes);
                params.put("doctor_id", String.valueOf(doctorId));
                params.put("appointment_id", String.valueOf(appointmentId));
                return params;
            }
        };

        queue.add(request);
    }
}
