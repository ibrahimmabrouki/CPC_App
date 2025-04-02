package com.example.cpc;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PrescriptionActivity extends AppCompatActivity {

    EditText etPatientId, etMedication, etDosage, etInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        etPatientId = findViewById(R.id.et_patient_id);
        etMedication = findViewById(R.id.et_medication);
        etDosage = findViewById(R.id.et_dosage);
        etInstructions = findViewById(R.id.et_instructions);

        // You can also get patient ID from intent later
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

        // Simulate sending to patient
        Toast.makeText(this, "Prescription submitted successfully!", Toast.LENGTH_LONG).show();
        finish(); // go back to patient list
    }
}
