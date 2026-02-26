package com.saveetha.myjoints;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InvestigationHistoryActivity extends AppCompatActivity {

    private static final String TAG = "InvestHistory";

    private ImageView backBtn;
    private TextView tvPatientId;
    private RecyclerView rvInvestigations;
    private FloatingActionButton btnAddInvestigation;

    private final List<InvestigationItem> items = new ArrayList<>();
    private InvestigationHistoryAdapter adapter;
    private String patientId;

    private static final String BASE_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String ADD_URL    = BASE_URL + "add_investigation.php";
    private static final String GET_URL    = BASE_URL + "get_investigations.php";
    private static final String DELETE_URL = BASE_URL + "delete_investigation.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investigation_history);

        backBtn = findViewById(R.id.back_btn);
        tvPatientId = findViewById(R.id.tvPatientId);
        rvInvestigations = findViewById(R.id.rvInvestigations);
        btnAddInvestigation = findViewById(R.id.btnAddInvestigation);

        backBtn.setOnClickListener(v -> onBackPressed());

        patientId = getIntent().getStringExtra("patient_id");
        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this, "Patient ID missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvPatientId.setText("Patient ID: " + patientId);

        rvInvestigations.setLayoutManager(new LinearLayoutManager(this));
        rvInvestigations.setItemAnimator(null);

        adapter = new InvestigationHistoryAdapter(
                this,
                items,
                this::confirmDeleteInvestigation
        );
        rvInvestigations.setAdapter(adapter);

        loadInvestigationsFromServer();

        btnAddInvestigation.setOnClickListener(v ->
                showAddInvestigationDialog()
        );
    }

    // =====================================================
    // ADD INVESTIGATION (VALIDATIONS ONLY ADDED)
    // =====================================================
    private void showAddInvestigationDialog() {

        View v = getLayoutInflater()
                .inflate(R.layout.dialog_add_investigation, null);

        EditText hb = v.findViewById(R.id.etHb);
        EditText tlc = v.findViewById(R.id.etTlc);
        EditText dc = v.findViewById(R.id.etDc);
        EditText platelet = v.findViewById(R.id.etPlatelet);
        EditText esr = v.findViewById(R.id.etEsr);
        EditText crp = v.findViewById(R.id.etCrp);
        EditText lftTotal = v.findViewById(R.id.etLftTotalBilirubin);
        EditText lftDirect = v.findViewById(R.id.etLftDirectBilirubin);
        EditText ast = v.findViewById(R.id.etAst);
        EditText alt = v.findViewById(R.id.etAlt);
        EditText albumin = v.findViewById(R.id.etAlbumin);
        EditText totalProtein = v.findViewById(R.id.etTotalProtein);
        EditText ggt = v.findViewById(R.id.etGgt);
        EditText urea = v.findViewById(R.id.etUrea);
        EditText creatinine = v.findViewById(R.id.etCreatinine);
        EditText uricAcid = v.findViewById(R.id.etUricAcid);
        EditText urineRoutine = v.findViewById(R.id.etUrineRoutine);
        EditText urinePcr = v.findViewById(R.id.etUrinePcr);
        EditText raFactor = v.findViewById(R.id.etRaFactor);
        EditText antiCcp = v.findViewById(R.id.etAntiCcp);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Investigation")
                .setView(v)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button add = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            add.setOnClickListener(btn -> {

                try {
                    validate(hb, 5, 20, true);
                    validate(tlc, 1000, 30000, false);
                    validate(dc, 0, 100, false);
                    validate(platelet, 50000, 1000000, false);
                    validate(esr, 0, 150, false);
                    validate(crp, 0, 300, true);
                    validate(lftTotal, 0, 20, true);
                    validate(lftDirect, 0, 10, true);
                    validate(ast, 0, 1000, false);
                    validate(alt, 0, 1000, false);
                    validate(albumin, 0, 10, true);
                    validate(totalProtein, 0, 15, true);
                    validate(ggt, 0, 1000, false);
                    validate(urea, 0, 300, false);
                    validate(creatinine, 0, 20, true);
                    validate(uricAcid, 0, 20, true);
                    validate(urineRoutine, 0, 100, false);
                    validate(urinePcr, 0, 100, true);
                    validate(raFactor, 0, 500, true);
                    validate(antiCcp, 0, 500, true);
                } catch (Exception e) {
                    return;
                }

                addInvestigationToServer(
                        hb.getText().toString().trim(),
                        tlc.getText().toString().trim(),
                        dc.getText().toString().trim(),
                        platelet.getText().toString().trim(),
                        esr.getText().toString().trim(),
                        crp.getText().toString().trim(),
                        lftTotal.getText().toString().trim(),
                        lftDirect.getText().toString().trim(),
                        ast.getText().toString().trim(),
                        alt.getText().toString().trim(),
                        albumin.getText().toString().trim(),
                        totalProtein.getText().toString().trim(),
                        ggt.getText().toString().trim(),
                        urea.getText().toString().trim(),
                        creatinine.getText().toString().trim(),
                        uricAcid.getText().toString().trim(),
                        urineRoutine.getText().toString().trim(),
                        urinePcr.getText().toString().trim(),
                        raFactor.getText().toString().trim(),
                        antiCcp.getText().toString().trim()
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // =====================================================
    // VALIDATION HELPER
    // =====================================================
    private void validate(EditText et, double min, double max, boolean decimal) {
        String v = et.getText().toString().trim();
        if (TextUtils.isEmpty(v)) {
            et.setError("Required");
            throw new RuntimeException();
        }
        if (!v.matches(decimal ? "^\\d+(\\.\\d+)?$" : "^\\d+$")) {
            et.setError("Invalid number");
            throw new RuntimeException();
        }
        double val = Double.parseDouble(v);
        if (val < min || val > max) {
            et.setError("Range " + min + " – " + max);
            throw new RuntimeException();
        }
    }

    // =====================================================
    // ADD INVESTIGATION TO SERVER
    // =====================================================
    private void addInvestigationToServer(
            String hb, String tlc, String dc, String platelet,
            String esr, String crp,
            String lftTotal, String lftDirect,
            String ast, String alt,
            String albumin, String totalProtein,
            String ggt, String urea, String creatinine,
            String uricAcid, String urineRoutine,
            String urinePcr, String raFactor, String antiCcp
    ) {
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
                body.put("hb", hb);
                body.put("total_leukocyte", tlc);
                body.put("differential_count", dc);
                body.put("platelet_count", platelet);
                body.put("esr", esr);
                body.put("crp", crp);
                body.put("lft_total_bilirubin", lftTotal);
                body.put("lft_direct_bilirubin", lftDirect);
                body.put("ast", ast);
                body.put("alt", alt);
                body.put("albumin", albumin);
                body.put("total_protein", totalProtein);
                body.put("ggt", ggt);
                body.put("urea", urea);
                body.put("creatinine", creatinine);
                body.put("uric_acid", uricAcid);
                body.put("urine_routine", urineRoutine);
                body.put("urine_pcr", urinePcr);
                body.put("ra_factor", raFactor);
                body.put("anti_ccp", antiCcp);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                br.close();

                runOnUiThread(this::loadInvestigationsFromServer);

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
    // DELETE INVESTIGATION
    // =====================================================
    private void confirmDeleteInvestigation(InvestigationItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Investigation")
                .setMessage("Are you sure you want to delete this investigation?")
                .setPositiveButton("Yes",
                        (d, w) -> deleteInvestigationFromServer(item))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteInvestigationFromServer(InvestigationItem item) {
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

                runOnUiThread(this::loadInvestigationsFromServer);

            } catch (Exception e) {
                Log.e(TAG, "Delete error", e);
            }
        }).start();
    }

    // =====================================================
    // LOAD INVESTIGATIONS
    // =====================================================
    private void loadInvestigationsFromServer() {
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
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());
                JSONArray arr = res.optJSONArray("investigations");

                List<InvestigationItem> temp = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        List<String> details = new ArrayList<>();
                        Iterator<String> keys = o.keys();
                        while (keys.hasNext()) {
                            String k = keys.next();
                            String v = o.optString(k);
                            if (!TextUtils.isEmpty(v)
                                    && !k.equals("created_at")) {
                                details.add(
                                        k.replace("_", " ").toUpperCase()
                                                + ": " + v
                                );
                            }
                        }
                        temp.add(new InvestigationItem(
                                o.optInt("id", 0),
                                "Investigation",
                                details
                        ));
                    }
                }

                runOnUiThread(() -> {
                    items.clear();
                    items.addAll(temp);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                Log.e(TAG, "Load error", e);
            }
        }).start();
    }
}
