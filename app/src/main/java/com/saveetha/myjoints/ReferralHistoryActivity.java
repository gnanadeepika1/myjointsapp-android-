package com.saveetha.myjoints;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ReferralHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ReferralHistory";
    private static final String PREFS_NAME = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    private static final String BASE_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String ADD_URL = BASE_URL + "referralhistory_add.php";
    private static final String GET_URL = BASE_URL + "referralhistory_get.php";
    private static final String DELETE_URL = BASE_URL + "referralhistory_delete.php";

    private ImageView backBtn;
    private TextView tvPatientId;
    private RecyclerView rvReferrals;
    private FloatingActionButton fabAddReferral;

    private final List<ReferralItem> referralList = new ArrayList<>();
    private ReferralAdapter adapter;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referrals_history);

        backBtn = findViewById(R.id.back_btn);
        tvPatientId = findViewById(R.id.tvPatientId);
        rvReferrals = findViewById(R.id.rvReferrals);
        fabAddReferral = findViewById(R.id.fabAddReferral);

        backBtn.setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        if (intent != null) {
            patientId = intent.getStringExtra("patient_id");
        }

        if (TextUtils.isEmpty(patientId)) {
            SharedPreferences prefs =
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            patientId = prefs.getString(KEY_PATIENT_ID, null);
        }

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this, "Patient ID missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvPatientId.setText("Patient ID: " + patientId);

        rvReferrals.setLayoutManager(new LinearLayoutManager(this));
        rvReferrals.setItemAnimator(null);

        adapter = new ReferralAdapter(referralList, this::confirmDeleteReferral);
        rvReferrals.setAdapter(adapter);

        loadReferralsFromServer();

        fabAddReferral.setOnClickListener(v -> showAddReferralDialog());
    }

    // =====================================================
    // ADD REFERRAL (VALIDATION ADDED)
    // =====================================================
    private void showAddReferralDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter referral details");
        input.setMinLines(3);
        input.setMaxLines(6);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Referral")
                .setView(input)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String text = input.getText().toString().trim();

                            if (TextUtils.isEmpty(text)) {
                                input.setError("Required");
                                return;
                            }

                            if (text.length() < 10 || text.length() > 300) {
                                input.setError("10–300 characters only");
                                return;
                            }

                            // ✅ ONLY LETTERS AND SPACES
                            if (!text.matches("^[A-Za-z ]+$")) {
                                input.setError("Only letters and spaces allowed");
                                return;
                            }

                            addReferralToServer(text);
                            dialog.dismiss();
                        })
        );

        dialog.show();
    }

    private void addReferralToServer(String message) {
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
                body.put("message", message);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    if (res.optBoolean("success")) {
                        Toast.makeText(
                                this,
                                "Referral added",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadReferralsFromServer();
                    } else {
                        Toast.makeText(
                                this,
                                res.optString("message", "Add failed"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Add error", e);
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Server error",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    // =====================================================
    // DELETE REFERRAL
    // =====================================================
    private void confirmDeleteReferral(ReferralItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Referral")
                .setMessage("Are you sure you want to delete this referral?")
                .setPositiveButton(
                        "Yes",
                        (d, w) -> deleteReferralFromServer(item)
                )
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteReferralFromServer(ReferralItem item) {
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
                body.put("id", item.getId());

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                br.close();

                runOnUiThread(this::loadReferralsFromServer);

            } catch (Exception e) {
                Log.e(TAG, "Delete error", e);
            }
        }).start();
    }

    // =====================================================
    // LOAD REFERRALS
    // =====================================================
    private void loadReferralsFromServer() {
        new Thread(() -> {
            List<ReferralItem> temp = new ArrayList<>();
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
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());
                JSONArray arr = res.optJSONArray("data");

                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        temp.add(new ReferralItem(
                                o.getInt("id"),
                                o.getString("message"),
                                o.getString("patient_id")
                        ));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Load error", e);
            }

            runOnUiThread(() -> {
                referralList.clear();
                referralList.addAll(temp);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
