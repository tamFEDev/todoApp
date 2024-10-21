package com.example.logintest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SecondActivity extends AppCompatActivity {

    private LinearLayout notesContainer;
    private static final String TAG = "SecondActivity"; // Add a tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        String username = getIntent().getStringExtra("USERNAME");
        notesContainer = findViewById(R.id.notesContainer);

        TextView greetingText = findViewById(R.id.greetingText);
        greetingText.setText("Welcome, " + username + "!");

        Button addNoteButton = findViewById(R.id.addNoteButton);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start CreateNoteActivity to add a new note
                Intent intent = new Intent(SecondActivity.this, CreateNoteActivity.class);
                startActivity(intent);
            }
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch the list of notes from the API when the activity resumes
        new FetchNotesTask().execute();
    }

    // AsyncTask to fetch notes from the API
    private class FetchNotesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Replace with your actual API URL
                URL url = new URL("https://newsapplication-edb5fqa9e7bvewgt.canadacentral-01.azurewebsites.net/api/v1/note");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                } else {
                    return "Failed to fetch notes. Response code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "API Response: " + result);  // Log the raw API response

            try {
                // Clear existing notes before adding new ones
                notesContainer.removeAllViews();

                // Parse the response JSON
                JSONObject responseObject = new JSONObject(result);  // Parse the entire JSON object
                JSONArray notesArray = responseObject.getJSONArray("listResult");  // Get the 'listResult' array

                Log.d(TAG, "Parsed listResult: " + notesArray.toString());  // Log the parsed listResult

                // Loop through the array and extract 'title' and 'content'
                for (int i = 0; i < notesArray.length(); i++) {
                    JSONObject noteObject = notesArray.getJSONObject(i);
                    String id = noteObject.getString("id");
                    String title = noteObject.getString("title");
                    String content = noteObject.getString("content");
                    String status = noteObject.getString("status"); // Get the status field

                    // Check if the status is ACTIVE
                    if ("ACTIVE".equals(status)) {
                        addNewToDoCard(title, content, id);  // Add the title and content to the UI
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error parsing notes: " + e.getMessage());  // Log any errors during parsing
                Toast.makeText(SecondActivity.this, "Error parsing notes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Add a new card dynamically to the UI
    private void addNewToDoCard(String title, String description, String noteId) {
        // Inflate the to-do card layout
        View cardView = getLayoutInflater().inflate(R.layout.todo_card, null);

        // Set the title and description
        TextView todoTitle = cardView.findViewById(R.id.todoTitle);
        TextView todoDescription = cardView.findViewById(R.id.todoDescription);

        todoTitle.setText(title);
        todoDescription.setText(description);

        // Handle delete button click
        Button deleteButton = cardView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote(noteId);  // Call the deleteNote function with the note ID
            }
        });

        // Add the new card to the container
        notesContainer.addView(cardView);
    }

    // Method to delete a note
    private void deleteNote(String noteId) {
        new DeleteNoteTask(noteId).execute();  // Call AsyncTask to perform the API request
    }

    // AsyncTask to perform API call for deletion
    private class DeleteNoteTask extends AsyncTask<Void, Void, String> {
        private String noteId;

        DeleteNoteTask(String noteId) {
            this.noteId = noteId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("https://newsapplication-edb5fqa9e7bvewgt.canadacentral-01.azurewebsites.net/api/v1/note/" + noteId);  // Replace with your actual API URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Note deleted successfully!";
                } else {
                    return "Failed to delete note. Response code: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(SecondActivity.this, result, Toast.LENGTH_SHORT).show();
            // Refresh notes after deletion
            if (result.contains("successfully")) {
                new FetchNotesTask().execute();  // Re-fetch the notes to refresh the list
            }
        }
    }


    private void logout() {
        Intent intent = new Intent(SecondActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
