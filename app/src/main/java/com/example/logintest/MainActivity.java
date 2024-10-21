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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the views by finding them by their IDs
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.login_btn);
        TextView createAccountText = findViewById(R.id.create_account_text);

        // Set an onClickListener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();

                // Call AsyncTask to perform login
                new LoginTask(username, password).execute();
            }
        });

        createAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // AsyncTask to handle login API call
    private class LoginTask extends AsyncTask<Void, Void, String> {
        private String username, password;

        LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Replace with your actual login API URL
                URL url = new URL("https://newsapplication-edb5fqa9e7bvewgt.canadacentral-01.azurewebsites.net/api/v1/auth/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                // Create the JSON object with the login credentials
                JSONObject json = new JSONObject();
                json.put("emailOrUsername", username);
                json.put("password", password);

                // Send the JSON to the server
                OutputStream os = connection.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {  // Success
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } else {
                    return "Failed: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "API Response: " + result);

            try {
                // Check if the result starts with '{' indicating a JSON response
                if (result.trim().startsWith("{")) {
                    // Parse the response as JSON
                    JSONObject responseJson = new JSONObject(result);

                    // Check if the "success" key is present in the response
                    if (responseJson.has("id")) {
                        String id = responseJson.getString("id");

                        if (!id.isEmpty()) {
                            // If login is successful, navigate to SecondActivity
                            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                            intent.putExtra("USERNAME", username);
                            startActivity(intent);
                            finish();  // Close MainActivity
                        } else {
                            // If login fails, show the error message from the response
                            String message = responseJson.getString("message");
                            Toast.makeText(MainActivity.this, "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle missing "success" field
                        Toast.makeText(MainActivity.this, "Unexpected response format: no 'success' field.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Response JSON missing 'success' field: " + responseJson.toString());
                    }
                } else {
                    // Handle the non-JSON response (like plain text)
                    Toast.makeText(MainActivity.this, "Login Failed: " + result, Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error parsing response: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


    }
}
