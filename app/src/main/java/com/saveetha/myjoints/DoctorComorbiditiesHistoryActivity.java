package com.saveetha.myjoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DoctorComorbiditiesHistoryActivity extends AppCompatActivity {

    private LinearLayout llComorbidityList;
    private ImageView backBtn;
    private FloatingActionButton fabAdd;
    private TextView tvPatientId;

    private static final String BASE_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String GET_URL    = BASE_URL + "get_comorbidities.php";
    private static final String ADD_URL    = BASE_URL + "add_comorbidity.php";
    private static final String DELETE_URL = BASE_URL + "delete_comorbidity.php";

    private String patientId, doctorId;
    private final List<ComorbidityItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_comorbidities_history);

        llComorbidityList = findViewById(R.id.llComorbidityList);
        backBtn = findViewById(R.id.back_btn);
        fabAdd = findViewById(R.id.fabAdd);
        tvPatientId = findViewById(R.id.tvPatientId);

        patientId = getIntent().getStringExtra("patient_id");
        if (TextUtils.isEmpty(patientId)) {
            toast("Patient ID missing");
            finish();
            return;
        }

        SharedPreferences prefs =
                getSharedPreferences("doctor_prefs", MODE_PRIVATE);
        doctorId = prefs.getString("doctor_id", null);
        if (TextUtils.isEmpty(doctorId)) {
            toast("Please login again");
            finish();
            return;
        }

        tvPatientId.setText("Patient ID: " + patientId);

        backBtn.setOnClickListener(v -> onBackPressed());
        fabAdd.setOnClickListener(v -> showAddDialog());

        loadFromServer();
    }

    // =====================================================
    // ADD COMORBIDITY
    // =====================================================
    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter comorbidity");

        new AlertDialog.Builder(this)
                .setTitle("Add Comorbidity")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String text = input.getText().toString().trim();

                    if (TextUtils.isEmpty(text)) {
                        toast("Comorbidity cannot be empty");
                        return;
                    }

                    // ✅ VALIDATION ADDED (ONLY CHARACTERS AND SPACES)
                    if (!text.matches("^[A-Za-z ]+$")) {
                        toast("Only letters and spaces are allowed");
                        return;
                    }

                    addToServer(text);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addToServer(String text) {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(ADD_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("doctor_id", doctorId);
                body.put("title", "Comorbidity");
                body.put("text", text);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    if (res.optBoolean("success")) {
                        toast("Comorbidity added");
                        loadFromServer();
                    } else {
                        toast(res.optString("message", "Add failed"));
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> toast("Server error"));
            }
        }).start();
    }

    // =====================================================
    // DELETE COMORBIDITY
    // =====================================================
    private void deleteFromServer(ComorbidityItem item) {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(DELETE_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("id", item.id);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    if (res.optBoolean("success")) {
                        toast("Comorbidity deleted");
                        loadFromServer();
                    } else {
                        toast(res.optString("message", "Delete failed"));
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> toast("Server error"));
            }
        }).start();
    }

    // =====================================================
    // LOAD COMORBIDITIES
    // =====================================================
    private void loadFromServer() {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(GET_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());
                JSONArray arr = res.optJSONArray("comorbidities");

                List<ComorbidityItem> temp = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        temp.add(new ComorbidityItem(
                                o.getInt("id"),
                                o.getString("title"),
                                o.getString("text")
                        ));
                    }
                }

                runOnUiThread(() -> {
                    items.clear();
                    items.addAll(temp);
                    render();
                });

            } catch (Exception e) {
                runOnUiThread(() -> toast("Load failed"));
            }
        }).start();
    }

    // =====================================================
    // RENDER UI
    // =====================================================
    private void render() {
        llComorbidityList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ComorbidityItem item : items) {
            View v = inflater.inflate(
                    R.layout.item_comorbidity_history,
                    llComorbidityList,
                    false
            );

            ((TextView) v.findViewById(R.id.tvTitle))
                    .setText(item.title);
            ((TextView) v.findViewById(R.id.tvText))
                    .setText(item.text);

            v.findViewById(R.id.btnDeleteComorbidity)
                    .setOnClickListener(x -> deleteFromServer(item));

            llComorbidityList.addView(v);
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    // =====================================================
    // MODEL
    // =====================================================
    static class ComorbidityItem {
        final int id;
        final String title, text;

        ComorbidityItem(int id, String title, String text) {
            this.id = id;
            this.title = title;
            this.text = text;
        }
    }
}
