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

public class ComplaintsActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private ImageView backBtn;

    private final List<Complaint> data = new ArrayList<>();
    private ComplaintAdapter adapter;

    private static final String PREFS_NAME     = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    // use the same endpoint as doctor history
    private static final String GET_COMPLAINTS_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/get_complaints.php";

    private static final String TAG = "ComplaintsActivity";

    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        rvComplaints = findViewById(R.id.rvComplaints);
        backBtn      = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v -> onBackPressed());

        // Get patient_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        patientId = prefs.getString(KEY_PATIENT_ID, null);

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this,
                    "No patient id found. Please login again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Setup adapter (read-only list)
        adapter = new ComplaintAdapter(
                data,
                complaint -> Toast.makeText(
                        ComplaintsActivity.this,
                        "Complaint: " + complaint.getTitle(),
                        Toast.LENGTH_SHORT
                ).show()
        );
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);

        // Load complaints from server
        loadComplaintsFromServer();
    }

    private void loadComplaintsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_COMPLAINTS_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                String jsonBody = body.toString();

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
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
                Log.d(TAG, "GET_COMPLAINTS response: " + response);

                JSONObject json = new JSONObject(response);
                boolean success = json.getBoolean("success");

                if (success) {
                    JSONArray arr = json.optJSONArray("complaints");
                    List<Complaint> tempList = new ArrayList<>();

                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject c = arr.getJSONObject(i);
                            String title     = c.optString("title", "");
                            String createdAt = c.optString("created_at", "");
                            tempList.add(new Complaint(title, createdAt));
                        }
                    }

                    runOnUiThread(() -> {
                        data.clear();
                        data.addAll(tempList);
                        adapter.notifyDataSetChanged();

                        if (data.isEmpty()) {
                            Toast.makeText(ComplaintsActivity.this,
                                    "No complaints found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String message = json.optString("message", "Failed to load complaints");
                    runOnUiThread(() ->
                            Toast.makeText(ComplaintsActivity.this,
                                    message,
                                    Toast.LENGTH_LONG).show()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(ComplaintsActivity.this,
                                "Error loading complaints: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
