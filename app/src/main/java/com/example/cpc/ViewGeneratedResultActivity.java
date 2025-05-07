package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewGeneratedResultActivity extends AppCompatActivity {

    private final String BASE_URL = "http://10.21.139.29/clinic";
    private TextView tvResultContent;
    private Button btnEdit;
    private int recordId, patientId, doctorId;
    private String resultContent;
    private static final int EDIT_RESULT_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_generated_result);

        tvResultContent = findViewById(R.id.tvResultContent);
        btnEdit = findViewById(R.id.btnEditResult);

        recordId = getIntent().getIntExtra("record_id", -1);

        fetchGeneratedResult(recordId);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, GenerateLabResultActivity.class);
            intent.putExtra("record_id", recordId);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("doctor_id", doctorId);
            intent.putExtra("existing_content", resultContent);
            startActivityForResult(intent, EDIT_RESULT_REQUEST_CODE);
        });
    }

    private void fetchGeneratedResult(int recordId) {
        String url = BASE_URL + "/checkGeneratedResult.php?record_id=" + recordId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject obj = response.getJSONObject(0);
                            patientId = obj.getInt("patient_id");
                            doctorId = obj.getInt("doctor_id");
                            resultContent = obj.getString("content");
                            String date = obj.getString("created_at");

                            String display = "Patient ID: " + patientId +
                                    "\nDoctor ID: " + doctorId +
                                    "\n\nResult:\n" + resultContent +
                                    "\n\nCreated At: " + date;

                            tvResultContent.setText(display);
                        } else {
                            tvResultContent.setText("No result found for this record.");
                            btnEdit.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvResultContent.setText("Error parsing result.");
                    }
                },
                error -> {
                    Toast.makeText(this, "Error loading result", Toast.LENGTH_SHORT).show();
                    tvResultContent.setText("Network error.");
                });

        Volley.newRequestQueue(this).add(request);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_RESULT_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchGeneratedResult(recordId);
        }
    }
}
