package com.saveetha.myjoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class ComorbiditiesActivity extends AppCompatActivity {

    private static final String TAG = "ComorbiditiesActivity";

    private RecyclerView rvComorbidities;
    private ImageView backBtn;
    private final List<Comorbidity> data = new ArrayList<>();
    private ComorbidityAdapter adapter;

    private static final String PREFS_NAME     = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    // Patient only READS comorbidities
    private static final String GET_COMORBIDITIES_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/get_comorbidities.php";
    // If your PHP is actually under /myjoints/, then use:
    // "http://10.25.159.180/myjoints/get_comorbidities.php"

    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comorbidities);

        rvComorbidities = findViewById(R.id.rvComorbidities);
        backBtn         = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v -> onBackPressed());

        // Read patient_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        patientId = prefs.getString(KEY_PATIENT_ID, null);

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(
                    this,
                    "No patient id found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        // RecyclerView + adapter
        adapter = new ComorbidityAdapter(
                data,
                item -> Toast.makeText(
                        ComorbiditiesActivity.this,
                        "Clicked: " + item.getTitle(),
                        Toast.LENGTH_SHORT
                ).show()
        );
        rvComorbidities.setLayoutManager(new LinearLayoutManager(this));
        rvComorbidities.setAdapter(adapter);

        // Load from server (doctor-added comorbidities)
        loadComorbiditiesFromServer();
    }

    // ===================== LOAD FROM SERVER =====================
    private void loadComorbiditiesFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_COMORBIDITIES_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // JSON body: { "patient_id": "..." }
                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                String jsonBody = body.toString();

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                Log.d(TAG, "HTTP code: " + code);

                InputStream is = (code >= 200 && code < 400)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String response = sb.toString();
                Log.d(TAG, "Response: " + response);

                JSONObject json = new JSONObject(response);
                boolean success = json.getBoolean("success");

                if (success) {
                    JSONArray arr = json.optJSONArray("comorbidities");
                    data.clear();

                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject c = arr.getJSONObject(i);
                            String title = c.optString("title", "");
                            String desc  = c.optString("text", ""); // <- IMPORTANT: "text"
                            data.add(new Comorbidity(title, desc));
                        }
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    String message = json.optString(
                            "message",
                            "Failed to load comorbidities"
                    );
                    runOnUiThread(() ->
                            Toast.makeText(
                                    ComorbiditiesActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading comorbidities", e);
                runOnUiThread(() ->
                        Toast.makeText(
                                ComorbiditiesActivity.this,
                                "Error loading comorbidities: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
