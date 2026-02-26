package com.saveetha.myjoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TreatmentsActivity extends AppCompatActivity {

    // Same prefs as other patient screens
    private static final String PREFS_NAME     = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    // Backend
    private static final String BASE_URL = "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String GET_TREATMENTS_URL = BASE_URL + "get_treatments.php";

    private RecyclerView rvTreatments;
    private ImageView backBtn;

    private final List<Treatment> data = new ArrayList<>();
    private TreatmentAdapter adapter;

    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatments);

        rvTreatments = findViewById(R.id.rvTreatments);
        backBtn      = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v -> onBackPressed());

        // 1) Get patient ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        patientId = prefs.getString(KEY_PATIENT_ID, null);

        // 2) Fallback from Intent
        if (TextUtils.isEmpty(patientId)) {
            patientId = getIntent().getStringExtra("patient_id");
        }

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this,
                    "No patient ID found for Treatments",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new TreatmentAdapter(this, data);
        rvTreatments.setLayoutManager(new LinearLayoutManager(this));
        rvTreatments.setAdapter(adapter);

        loadTreatmentsFromServer();
    }

    private void loadTreatmentsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_TREATMENTS_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // JSON body
                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                InputStream is = (conn.getResponseCode() < 400)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject response = new JSONObject(sb.toString());

                if (response.optBoolean("success")) {

                    JSONArray arr = response.optJSONArray("treatments");
                    data.clear();

                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);

                            // ✅ MATCHES doctor side + DB + PHP
                            String medName  = obj.optString("name", "");
                            String dose     = obj.optString("dose", "");
                            String route    = obj.optString("route", "");
                            String freqNum  = obj.optString("frequency_number", "");
                            String freqText = obj.optString("frequency_text", "");
                            String duration = obj.optString("duration", "");

                            List<String> lines = new ArrayList<>();

                            if (!TextUtils.isEmpty(medName))
                                lines.add("Medication: " + medName);

                            if (!TextUtils.isEmpty(dose))
                                lines.add("Dose: " + dose);

                            if (!TextUtils.isEmpty(route))
                                lines.add("Route: " + route);

                            if (!TextUtils.isEmpty(freqNum) || !TextUtils.isEmpty(freqText))
                                lines.add("Frequency: " + freqNum + " " + freqText);

                            if (!TextUtils.isEmpty(duration))
                                lines.add("Duration: " + duration + " weeks");

                            if (!lines.isEmpty()) {
                                data.add(new Treatment("Treatment", lines));
                            }
                        }
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } else {
                    String msg = response.optString(
                            "message",
                            "Failed to load treatments"
                    );
                    runOnUiThread(() ->
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error loading treatments",
                                Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
