package msu.cse476.hakalaja.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    // Declare username, password EditTexts.
    private EditText editTextUsername;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements.
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        // Handle button onclick event.
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this::login);
    }

    /**
     * Login or sign user up when login button is clicked.
     * @param view The view that was clicked.
     */
    public void login(View view) {
        // Get username and password from UI elements.
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        // Check if the username and password are empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill out both Username and Password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an instance of SQLiteManager
        SQLiteManager db = new SQLiteManager(this);

        // Check if the user exists in the database
        if (db.userExists(username, password)) {
            // If the user exists, get the userID
            int userID = db.getUserID(username, password);

            // Store the userID in shared preferences (FOR GLOBAL ACCESS)
            SharedPreferences settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("userID", userID);
            editor.apply();

            // Start the RandomActivity
            Intent intent = new Intent(this, RandomActivity.class);
            startActivity(intent);
        }
        else {
            if (db.usernameExists(username)) {
                // Existing user, wrong password
                Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
            else {
                // New User
                db.insertUser(username, password);

                // Get the userID for the new user
                int userID = db.getUserID(username, password);

                // Store the userID in shared preferences (FOR GLOBAL ACCESS)
                SharedPreferences settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("userID", userID);
                editor.apply();

                // Display a success message
                Toast.makeText(LoginActivity.this, "User created", Toast.LENGTH_SHORT).show();

                // Start the RandomActivity
                Intent intent = new Intent(this, RandomActivity.class);
                startActivity(intent);
            }
        }
        db.close();
    }
}
