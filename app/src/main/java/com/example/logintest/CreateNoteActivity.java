package com.example.logintest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText editTextNoteTitle;
    private EditText editTextNoteDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_note);

        editTextNoteTitle = findViewById(R.id.editTextNoteTitle);
        editTextNoteDescription = findViewById(R.id.editTextNoteDescription);
        Button buttonSaveNote = findViewById(R.id.buttonSaveNote);

        buttonSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote() {
        String title = editTextNoteTitle.getText().toString().trim();
        String description = editTextNoteDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call AsyncTask to perform the API request
        new AddNoteTask(title, description).execute();
    }

    // AsyncTask to perform API call
    private class AddNoteTask extends AsyncTask<Void, Void, String> {
        private String title, description;

        AddNoteTask(String title, String description) {
            this.title = title;
            this.description = description;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("https://newsapplication-edb5fqa9e7bvewgt.canadacentral-01.azurewebsites.net/api/v1/note");  // Replace with your actual API URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                // Create JSON object for request body
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("userId", "5c76b3d5-9912-4c32-ae15-3083b3219847");
                jsonBody.put("title", title);
                jsonBody.put("content", description);
                jsonBody.put("isPinned", true);

                // Write JSON data to output stream
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "To-Do added successfully!";
                } else {
                    return "Failed to add To-Do. Response code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(CreateNoteActivity.this, result, Toast.LENGTH_SHORT).show();
            if (result.contains("successfully")) {
                finish(); // Close the activity if the API call is successful
            }
        }
    }
}
