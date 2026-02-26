package com.saveetha.myjoints;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.saveetha.network.RetrofitClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PatientSignupActivity extends AppCompatActivity {

    private EditText etPatientId, etName, etEmail, etAge, etSex,
            etOccupation, etAddress, etMobile, etPassword, etRePassword;
    private ImageView backBtn;
    private MaterialButton btnSubmit;

    private static final String PREFS_NAME = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";
    private static final String KEY_PATIENT_NAME = "patient_name";
    private static final String KEY_PATIENT_EMAIL = "patient_email";

    private static final String SIGNUP_URL =
            RetrofitClient.BASE_URL + "patient_signup.php";

    // 🔹 REGEX
    private static final String NAME_PATTERN = "^[A-Za-z ]+$";
    private static final String EMAIL_PATTERN = "^[A-Za-z][A-Za-z0-9]*@gmail\\.com$";
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,10}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_signup);

        initViews();
        setupListeners();
    }

    private void initViews() {
        backBtn = findViewById(R.id.back_btn);
        etPatientId = findViewById(R.id.etPatientId);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etSex = findViewById(R.id.etSex);
        etOccupation = findViewById(R.id.etOccupation);
        etAddress = findViewById(R.id.etAddress);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etRePassword = findViewById(R.id.etRePassword);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> onBackPressed());
        btnSubmit.setOnClickListener(v -> submitForm());
    }

    private void submitForm() {

        String patientId = etPatientId.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String sex = etSex.getText().toString().trim();
        String occupation = etOccupation.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();

        // 🔹 Patient ID
        if (TextUtils.isEmpty(patientId)) {
            etPatientId.setError("Patient ID is required");
            return;
        }
        if (!patientId.matches("(?i)^pat_\\d{4}$")) {
            etPatientId.setError("Patient ID must be like pat_1001");
            return;
        }

        // 🔹 Name
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (name.length() < 3 || !name.matches(NAME_PATTERN)) {
            etName.setError("Name must contain only letters and spaces");
            return;
        }

        // 🔹 Email
        if (!email.matches(EMAIL_PATTERN)) {
            etEmail.setError("Email must be abc123@gmail.com");
            return;
        }

        // 🔹 Age
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 1 || age > 120) {
                etAge.setError("Enter valid age (1–120)");
                return;
            }
        } catch (Exception e) {
            etAge.setError("Invalid age");
            return;
        }

        // 🔹 Sex
        if (!(sex.equalsIgnoreCase("male")
                || sex.equalsIgnoreCase("female")
                || sex.equalsIgnoreCase("other"))) {
            etSex.setError("Enter Male / Female / Other");
            return;
        }

        // 🔹 Occupation
        if (TextUtils.isEmpty(occupation) || !occupation.matches(NAME_PATTERN)) {
            etOccupation.setError("Occupation must contain only letters");
            return;
        }

        // 🔹 Address
        if (address.length() < 5) {
            etAddress.setError("Address too short");
            return;
        }

        // 🔹 Mobile
        if (!mobile.matches("\\d{10}")) {
            etMobile.setError("Mobile must be 10 digits");
            return;
        }
        if (mobile.matches("(\\d)\\1{9}")) {
            etMobile.setError("All digits cannot be same");
            return;
        }

        // 🔹 Password
        if (!password.matches(PASSWORD_PATTERN)) {
            etPassword.setError(
                    "Password must be 6–10 chars with capital, digit & special char");
            return;
        }

        // 🔹 Confirm password
        if (!password.equals(rePassword)) {
            etRePassword.setError("Passwords do not match");
            return;
        }

        registerPatientOnServer(
                patientId, name, email, age, sex,
                occupation, address, mobile, password
        );
    }

    private void registerPatientOnServer(String patientId, String name,
                                         String email, int age, String sex,
                                         String occupation, String address,
                                         String mobile, String password) {

        new Thread(() -> {
            try {
                URL url = new URL(SIGNUP_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("name", name);
                body.put("email", email);
                body.put("age", age);
                body.put("sex", sex);
                body.put("occupation", occupation);
                body.put("address", address);
                body.put("mobile", mobile);
                body.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                runOnUiThread(() -> handleResponse(sb.toString(), patientId));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Server error",
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void handleResponse(String response, String patientId) {
        try {
            JSONObject json = new JSONObject(response);
            if (json.getBoolean("success")) {

                SharedPreferences prefs =
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit()
                        .putString(KEY_PATIENT_ID, patientId)
                        .putString(KEY_PATIENT_NAME, etName.getText().toString())
                        .putString(KEY_PATIENT_EMAIL, etEmail.getText().toString())
                        .apply();

                Toast.makeText(this,
                        "Registered Successfully",
                        Toast.LENGTH_LONG).show();

                startActivity(new Intent(this, PatientLoginActivity.class));
                finish();

            } else {
                Toast.makeText(this,
                        json.getString("message"),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Invalid response",
                    Toast.LENGTH_LONG).show();
        }
    }
}
