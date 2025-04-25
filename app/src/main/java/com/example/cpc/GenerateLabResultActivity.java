package com.example.cpc;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GenerateLabResultActivity extends AppCompatActivity {

    private EditText etResultContent;
    private int recordId, patientId, doctorId;
    private final String BASE_URL = "http://10.21.134.17/clinic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_lab_result);

        etResultContent = findViewById(R.id.etResultContent);
        Button btnSubmit = findViewById(R.id.btnSubmitResult);

        recordId = getIntent().getIntExtra("record_id", -1);
        patientId = getIntent().getIntExtra("patient_id", -1);
        doctorId = getIntent().getIntExtra("doctor_id", -1);

        btnSubmit.setOnClickListener(v -> {
            String resultText = etResultContent.getText().toString().trim();
            if (resultText.isEmpty()) {
                Toast.makeText(this, "Please enter result content", Toast.LENGTH_SHORT).show();
                return;
            }
            submitGeneratedResult(resultText);
        });
    }

    private void submitGeneratedResult(String content) {
        String url = BASE_URL + "/generateLabResult.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Set result for the calling activity
                            finish(); // Go back to previous activity
                        } else {
                            Toast.makeText(this, "Error: " + jsonResponse.optString("message", "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("record_id", String.valueOf(recordId));
                params.put("patient_id", String.valueOf(patientId));
                params.put("doctor_id", String.valueOf(doctorId));
                params.put("content", content);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
