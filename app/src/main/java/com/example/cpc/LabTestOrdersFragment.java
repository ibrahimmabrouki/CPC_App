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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LabTestOrdersFragment extends Fragment {

    private final String BASE_URL = "http://10.21.139.29/clinic";
    private LinearLayout labOrderContainer;
    private String labTechId = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lab_test_orders, container, false);
        labOrderContainer = view.findViewById(R.id.labOrderContainer);

        if (getArguments() != null) {
            labTechId = getArguments().getString("user_id", "");
        }

        fetchLabTestOrders();
        return view;
    }

    private void fetchLabTestOrders() {
        String url = BASE_URL + "/getLabTestOrders.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    labOrderContainer.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int recordId = obj.getInt("record_id");

                            View card = LayoutInflater.from(getContext()).inflate(R.layout.item_lab_order, labOrderContainer, false);
                            TextView tvId = card.findViewById(R.id.tvTestOrderId);
                            Button btnView = card.findViewById(R.id.btnViewOrder);

                            tvId.setText("Test Order ID: " + recordId);
                            btnView.setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), LabTestDetailActivity.class);
                                intent.putExtra("record_id", recordId);
                                startActivity(intent);
                            });

                            labOrderContainer.addView(card);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Toast.makeText(getContext(), "Error loading lab test orders", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
