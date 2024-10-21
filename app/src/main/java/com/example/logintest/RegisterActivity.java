package com.example.logintest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        usernameInput = findViewById(R.id.registerUsername);
        passwordInput = findViewById(R.id.registerPassword);
        confirmPasswordInput = findViewById(R.id.confirmpass);
        registerButton = findViewById(R.id.registerBtn);
        TextView loginAccountText = findViewById(R.id.login_account_text);

        // Register button click event
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call the API to register the user
                new RegisterUserTask(username, password).execute();
            }
        });

        // Click event to switch to login activity
        loginAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // AsyncTask to handle the network request on a background thread
    private class RegisterUserTask extends AsyncTask<Void, Void, String> {
        private String username, password;

        RegisterUserTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url = new URL("https://newsapplication-edb5fqa9e7bvewgt.canadacentral-01.azurewebsites.net/api/v1/account/create");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                // Create JSON payload
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("username", username);
                jsonBody.put("password", password);
                jsonBody.put("email", username);
                jsonBody.put("roleName", "");
                jsonBody.put("phoneNumber", "");

                // Send request
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                // Get response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Registration successful";
                } else {
                    return "Registration failed. Response code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
