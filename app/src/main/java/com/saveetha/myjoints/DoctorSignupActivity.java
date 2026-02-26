package com.saveetha.myjoints;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.saveetha.network.RetrofitClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DoctorSignupActivity extends AppCompatActivity {

    private ImageView backBtn;
    private EditText etDoctorId, etFullName, etEmail, etPhone,
            etSpecialization, etPassword, etConfirmPassword;
    private MaterialButton btnPatient;

    private static final String PREFS_NAME = "doctor_prefs";
    private static final String KEY_DOCTOR_ID = "doctor_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_SPECIALIZATION = "specialization";
    private static final String KEY_PASSWORD = "password";

    private static final String SIGNUP_URL =
            RetrofitClient.BASE_URL + "doctor_signup.php";

    // 🔹 PASSWORD VALIDATION (same as login)
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/])" +
                    "[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]{6,10}$";

    // 🔹 NAME: only letters and spaces
    private static final String NAME_PATTERN = "^[A-Za-z ]+$";

    // 🔹 EMAIL: letters + numbers + @gmail.com
    private static final String EMAIL_PATTERN = "^[A-Za-z][A-Za-z0-9]*@gmail\\.com$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_signup);

        backBtn = findViewById(R.id.back_btn);
        etDoctorId = findViewById(R.id.etDoctorId);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSpecialization = findViewById(R.id.etSpecialization);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnPatient = findViewById(R.id.btnPatient);

        backBtn.setOnClickListener(v -> onBackPressed());
        btnPatient.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {

        String doctorId = etDoctorId.getText().toString().trim();
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String spec = etSpecialization.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String conf = etConfirmPassword.getText().toString();

        // 🔹 Doctor ID
        if (TextUtils.isEmpty(doctorId)) {
            etDoctorId.setError("Doctor ID is required");
            return;
        }
        if (!doctorId.matches("(?i)^doc_\\d{4}$")) {
            etDoctorId.setError("Doctor ID must be like doc_1001");
            return;
        }

        // 🔹 Name
        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name is required");
            return;
        }
        if (name.length() < 3) {
            etFullName.setError("Name must be at least 3 characters");
            return;
        }
        if (!name.matches(NAME_PATTERN)) {
            etFullName.setError("Name should contain only letters and spaces");
            return;
        }

        // 🔹 Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (!email.matches(EMAIL_PATTERN)) {
            etEmail.setError("Email must be in format abc123@gmail.com");
            return;
        }

        // 🔹 Phone
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            return;
        }
        if (!phone.matches("\\d{10}")) {
            etPhone.setError("Phone number must be exactly 10 digits");
            return;
        }
        if (phone.matches("(\\d)\\1{9}")) {
            etPhone.setError("All digits cannot be the same");
            return;
        }

        // 🔹 Specialization
        if (TextUtils.isEmpty(spec)) {
            etSpecialization.setError("Specialization is required");
            return;
        }

        // 🔹 Password
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Password is required");
            return;
        }
        if (!pass.matches(PASSWORD_PATTERN)) {
            etPassword.setError(
                    "Password must be 6–10 chars with 1 capital, 1 digit & 1 special character"
            );
            return;
        }

        // 🔹 Confirm Password
        if (TextUtils.isEmpty(conf)) {
            etConfirmPassword.setError("Please confirm password");
            return;
        }
        if (!pass.equals(conf)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        sendDataToServer(doctorId, name, email, phone, spec, pass);
    }

    private void sendDataToServer(String doctorId, String name, String email,
                                  String phone, String spec, String pass) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject body = new JSONObject();
                body.put("doctor_id", doctorId);
                body.put("full_name", name);
                body.put("email", email);
                body.put("phone", phone);
                body.put("specialization", spec);
                body.put("password", pass);

                URL url = new URL(SIGNUP_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject respJson = new JSONObject(sb.toString());
                boolean success = respJson.optBoolean("success");

                runOnUiThread(() -> {
                    if (success) {
                        SharedPreferences prefs =
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        prefs.edit()
                                .putString(KEY_DOCTOR_ID, doctorId)
                                .putString(KEY_FULL_NAME, name)
                                .putString(KEY_EMAIL, email)
                                .putString(KEY_PHONE, phone)
                                .putString(KEY_SPECIALIZATION, spec)
                                .putString(KEY_PASSWORD, pass)
                                .apply();

                        Toast.makeText(this,
                                "Registration Successful!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, DoctorLoginActivity.class)
                                .putExtra("doctor_id", doctorId));
                        finish();
                    } else {
                        Toast.makeText(this,
                                respJson.optString("message", "Registration failed"),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
