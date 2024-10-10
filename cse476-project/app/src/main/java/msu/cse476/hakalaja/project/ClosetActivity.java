package msu.cse476.hakalaja.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;

public class ClosetActivity extends AppCompatActivity {
    /** @noinspection rawtypes */
    // Create an array list of outfits and an index to keep track of the current outfit
    private ArrayList<HashMap> outfits = new ArrayList<>();
    private int currentOutfitIndex = 0;
    // Shared preferences to access the userID
    SharedPreferences settings;

    /**
     * This method is called when the activity is created.
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);

        // Get the userID from shared preferences
        settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
        int userID = settings.getInt("userID", -1);

        // Get the database
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        SQLiteDatabase db = sqLiteManager.getReadableDatabase();

        // Query the database for all outfits belonging to the current user
        Cursor cursor = db.query(SQLiteManager.OUTFIT_TABLE_NAME, null, SQLiteManager.USER_ID + "=?", new String[]{String.valueOf(userID)}, null, null, null);
        while (cursor.moveToNext()) {
            // Create a HashMap for each row in the result set and add it to the outfits array
            HashMap<String, String> outfit = new HashMap<>();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                outfit.put(cursor.getColumnName(i), cursor.getString(i));
            }
            outfits.add(outfit);
        }
        cursor.close();
        db.close();

        // Display the first outfit
        try {
            if (!outfits.isEmpty()) {
                displayOutfit(outfits.get(0));
            }
        } catch (Exception e) {
            // If an exception occurs, display a toast message to the user
            Toast.makeText(this, "You need to save an outfit first.", Toast.LENGTH_SHORT).show();
        }

        // Set up the left button
        Button leftButton = findViewById(R.id.left_button);
        leftButton.setOnClickListener(v -> {
            try {
                if (currentOutfitIndex > 0) {
                    currentOutfitIndex--;
                } else {
                    // If the current index is 0, wrap around to the end of the array
                    currentOutfitIndex = outfits.size() - 1;
                }
                displayOutfit(outfits.get(currentOutfitIndex));
            } catch (Exception e) {
                // If an exception occurs, display a toast message to the user
                Toast.makeText(this, "You need to save an outfit first.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the right button
        Button rightButton = findViewById(R.id.right_button);
        rightButton.setOnClickListener(v -> {
            try {
                if (currentOutfitIndex < outfits.size() - 1) {
                    currentOutfitIndex++;
                } else {
                    // If the current index is at the end of the array, wrap around to the start of the array
                    currentOutfitIndex = 0;
                }
                displayOutfit(outfits.get(currentOutfitIndex));
            } catch (Exception e) {
                // If an exception occurs, display a toast message to the user
                Toast.makeText(this, "You need to save an outfit first.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for user pressing upload button, and redirect to UploadActivity when pressed.
        Button uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(ClosetActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        // Listen for user pressing randomize button, and take user to RandomActivity when pressed.
        Button randomizeButton = findViewById(R.id.randomize_button);
        randomizeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ClosetActivity.this, RandomActivity.class);
            startActivity(intent);
        });

        // Listen for user pressing logout button, and logout when pressed.
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            // Set the userID in shared preferences to -1
            settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("userID", -1);
            editor.apply();

            // Display a message when logout button is clicked
            Toast.makeText(ClosetActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * This method displays the outfit in the UI.
     *
     * @param outfit the outfit to display
     */
    private void displayOutfit(HashMap<String, String> outfit) {
        try {
            // Get the ImageViews
            ImageView topImageView = findViewById(R.id.outfit_top_image);
            ImageView shirtImageView = findViewById(R.id.outfit_shirt_image);
            ImageView pantImageView = findViewById(R.id.outfit_pant_image);
            ImageView shoeImageView = findViewById(R.id.outfit_shoe_image);

            // Load the images from the URIs into the ImageViews
            Glide.with(this).load(outfit.get("topUri")).into(topImageView);
            Glide.with(this).load(outfit.get("shirtUri")).into(shirtImageView);
            Glide.with(this).load(outfit.get("pantUri")).into(pantImageView);
            Glide.with(this).load(outfit.get("shoeUri")).into(shoeImageView);
        } catch (Exception e) {
            // If an exception occurs, display a toast message to the user
            Toast.makeText(this, "You need to save an outfit first.", Toast.LENGTH_SHORT).show();
        }
    }
}
