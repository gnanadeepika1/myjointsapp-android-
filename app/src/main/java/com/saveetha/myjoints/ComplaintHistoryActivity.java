package com.saveetha.myjoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class ComplaintHistoryActivity extends AppCompatActivity {

    private RecyclerView rvComplaintHistory;
    private ImageView backBtn;
    private FloatingActionButton fabAddComplaintHistory;
    private TextView tvPatientId;

    private final List<ComplaintHistoryItem> complaints = new ArrayList<>();
    private ComplaintHistoryAdapter adapter;

    private static final String GET_COMPLAINTS_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/get_complaints.php";
    private static final String ADD_COMPLAINT_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/add_complaint.php";
    private static final String DELETE_COMPLAINT_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/delete_complaint.php";

    private static final String PREFS_NAME_DOCTOR = "doctor_prefs";
    private static final String KEY_DOCTOR_ID = "doctor_id";

    private static final int MIN_COMPLAINT_LENGTH = 3;
    private static final int MAX_COMPLAINT_LENGTH = 100;

    private String patientId;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_history);

        rvComplaintHistory = findViewById(R.id.rvComplaintHistory);
        backBtn = findViewById(R.id.back_btn);
        fabAddComplaintHistory = findViewById(R.id.fabAddComplaintHistory);
        tvPatientId = findViewById(R.id.tvPatientId);

        patientId = getIntent().getStringExtra("patient_id");
        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this, "No patient id provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvPatientId.setText("Patient ID: " + patientId);

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME_DOCTOR, MODE_PRIVATE);
        doctorId = prefs.getString(KEY_DOCTOR_ID, null);

        if (TextUtils.isEmpty(doctorId)) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backBtn.setOnClickListener(v -> onBackPressed());
        fabAddComplaintHistory.setOnClickListener(v -> showAddComplaintDialog());

        rvComplaintHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintHistoryAdapter(complaints);
        rvComplaintHistory.setAdapter(adapter);

        loadComplaintsFromServer();
    }

    // ---------------- ADD COMPLAINT ----------------
    private void showAddComplaintDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter complaint");

        new AlertDialog.Builder(this)
                .setTitle("Add Complaint")
                .setView(input)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String text = input.getText().toString().trim();

                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(this, "Complaint cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (text.length() < MIN_COMPLAINT_LENGTH) {
                        Toast.makeText(this, "Minimum 3 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (text.length() > MAX_COMPLAINT_LENGTH) {
                        Toast.makeText(this, "Maximum 100 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!text.matches("^[A-Za-z ,.]+$")) {
                        Toast.makeText(this, "Only letters allowed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (ComplaintHistoryItem item : complaints) {
                        if (item.title.equalsIgnoreCase(text)) {
                            Toast.makeText(this, "Complaint already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    addComplaintToServer(text);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addComplaintToServer(String title) {
        new Thread(() -> {
            try {
                URL url = new URL(ADD_COMPLAINT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("doctor_id", doctorId);
                body.put("title", title);
                body.put("description", "");

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject resp = new JSONObject(sb.toString());
                boolean success = resp.optBoolean("success");

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            success ? "Complaint added" : "Failed",
                            Toast.LENGTH_SHORT).show();
                    if (success) loadComplaintsFromServer();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error adding complaint", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ---------------- LOAD COMPLAINTS ----------------
    private void loadComplaintsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_COMPLAINTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                JSONArray arr = json.optJSONArray("complaints");

                List<ComplaintHistoryItem> temp = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject c = arr.getJSONObject(i);
                        temp.add(new ComplaintHistoryItem(
                                c.optString("title"),
                                c.optString("created_at")));
                    }
                }

                runOnUiThread(() -> {
                    complaints.clear();
                    complaints.addAll(temp);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading complaints", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ---------------- DELETE COMPLAINT ----------------
    private void showDeleteConfirmation(String title) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Complaint")
                .setMessage("Are you sure you want to delete this complaint?")
                .setPositiveButton("Yes", (d, w) -> deleteComplaintFromServer(title))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteComplaintFromServer(String title) {
        new Thread(() -> {
            try {
                URL url = new URL(DELETE_COMPLAINT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("title", title);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject resp = new JSONObject(sb.toString());
                boolean success = resp.optBoolean("success");

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            success ? "Complaint deleted" : "Delete failed",
                            Toast.LENGTH_SHORT).show();
                    if (success) loadComplaintsFromServer();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error deleting complaint", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ---------------- MODEL ----------------
    private static class ComplaintHistoryItem {
        final String title;
        final String date;

        ComplaintHistoryItem(String title, String date) {
            this.title = title;
            this.date = date;
        }
    }

    // ---------------- ADAPTER ----------------
    private class ComplaintHistoryAdapter
            extends RecyclerView.Adapter<ComplaintHistoryAdapter.ViewHolder> {

        private final List<ComplaintHistoryItem> items;

        ComplaintHistoryAdapter(List<ComplaintHistoryItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(
                android.view.ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_complaint_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ComplaintHistoryItem item = items.get(position);
            holder.tvComplaintTitle.setText(item.title);
            holder.tvComplaintDate.setText("Date: " + item.date);

            holder.btnDelete.setOnClickListener(v ->
                    showDeleteConfirmation(item.title));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvComplaintTitle, tvComplaintDate;
            ImageView btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvComplaintTitle = itemView.findViewById(R.id.tvComplaintTitle);
                tvComplaintDate  = itemView.findViewById(R.id.tvComplaintDate);
                btnDelete        = itemView.findViewById(R.id.btnDeleteComplaint);
            }
        }
    }
}
