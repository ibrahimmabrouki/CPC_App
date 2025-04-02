package com.example.cpc;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LabTestOrderActivity extends AppCompatActivity {

    EditText etPatientId, etTestName, etReason, etInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_test_order);

        etPatientId = findViewById(R.id.et_patient_id);
        etTestName = findViewById(R.id.et_test_name);
        etReason = findViewById(R.id.et_reason);
        etInstructions = findViewById(R.id.et_instructions);
    }

    public void submitLabTestOrder(View view) {
        String id = etPatientId.getText().toString().trim();
        String test = etTestName.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (id.isEmpty() || test.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please complete all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Lab Test Order submitted!", Toast.LENGTH_LONG).show();
        finish();
    }
}
