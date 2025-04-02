package com.example.cpc;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MedicalRecordActivity extends AppCompatActivity {

    EditText etPatientId, etDiagnosis, etSymptoms, etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        etPatientId = findViewById(R.id.et_patient_id);
        etDiagnosis = findViewById(R.id.et_diagnosis);
        etSymptoms = findViewById(R.id.et_symptoms);
        etNotes = findViewById(R.id.et_notes);
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

        Toast.makeText(this, "Medical record submitted successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
