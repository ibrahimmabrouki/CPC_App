package com.example.cpc;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StorageFragment extends Fragment implements RefreshableFragment{

    private final String BASE_URL = "http://10.21.148.28/clinic";
    private LinearLayout storageContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        storageContainer = view.findViewById(R.id.storageContainer);

        fetchMedicines();
        return view;
    }

    private void fetchMedicines() {
        String url = BASE_URL + "/getMedicines.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    storageContainer.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject med = response.getJSONObject(i);
                            String name = med.getString("name");
                            int quantity = med.getInt("quantity");
                            String imageKey = med.getString("image_url");

                            addMedicineItem(name, quantity, imageKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    TextView errorMsg = new TextView(getContext());
                    errorMsg.setText("Failed to load medicines.");
                    storageContainer.addView(errorMsg);
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void addMedicineItem(String name, int quantity, String imageName) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_medicine, storageContainer, false);

        TextView nameView = item.findViewById(R.id.tvMedicineName);
        TextView qtyView = item.findViewById(R.id.tvQuantity);
        ImageView imgView = item.findViewById(R.id.imgMedicine);
        Button btnPlus = item.findViewById(R.id.btnIncrease);
        Button btnMinus = item.findViewById(R.id.btnDecrease);

        nameView.setText(name);
        qtyView.setText("Quantity: " + quantity);

        // Load image
        int imageResId = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());
        imgView.setImageResource(imageResId != 0 ? imageResId : R.drawable.placeholder);

        final int[] currentQty = {quantity};

        btnPlus.setOnClickListener(v -> {
            currentQty[0]++;
            qtyView.setText("Quantity: " + currentQty[0]);
            updateQuantityInDatabase(name, currentQty[0]);
        });

        btnMinus.setOnClickListener(v -> {
            if (currentQty[0] > 0) {
                currentQty[0]--;
                qtyView.setText("Quantity: " + currentQty[0]);
                updateQuantityInDatabase(name, currentQty[0]);
            }
        });

        storageContainer.addView(item);
    }

    private void updateQuantityInDatabase(String name, int newQuantity) {
        String url = BASE_URL + "/updateMedicineQuantity.php";

        Volley.newRequestQueue(requireContext()).add(new StringRequest(Request.Method.POST, url,
                response -> {
                },
                error -> {
                    Toast.makeText(getContext(), "Failed to update quantity", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("quantity", String.valueOf(newQuantity));
                return params;
            }
        });
    }

    @Override
    public void onRefresh() {
        fetchMedicines();
    }
}
