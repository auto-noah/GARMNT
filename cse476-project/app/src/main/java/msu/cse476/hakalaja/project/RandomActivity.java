package msu.cse476.hakalaja.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class RandomActivity extends AppCompatActivity {
    // Map to store the user's items
    public static Map<String, List<String>> categorizedItems = new HashMap<>();
    // Map to store the lock status of each item
    public static Map<String, Boolean> itemLockStatus = new HashMap<>();

    // Uri of the items in the current outfit
    private String topUri = null;
    private String shirtUri = null;
    private String pantUri = null;
    private String shoeUri = null;


    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently contained.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random);

        categorizedItems.clear(); // Clear any previous items from the map
        itemLockStatus.clear();   // Clear any previous lock status of all items

        // Get the userID from shared preferences
        SharedPreferences preferences = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
        int userId = preferences.getInt("userID", -1);

        // Retrieve all items for the current user
        SQLiteManager db = new SQLiteManager(this);
        Cursor items = db.getItems(userId);

        // Separate the items into different categories
        while (items.moveToNext()) {
            // Get the type and URI of the item
            int typeColumnIndex = items.getColumnIndex("type");
            int uriColumnIndex = items.getColumnIndex("Uri");

            // If the item has a type and URI, add it to the categorizedItems map
            if (typeColumnIndex != -1 && uriColumnIndex != -1) {
                String type = items.getString(typeColumnIndex);
                String uri = items.getString(uriColumnIndex);

                // Add the URI to the list of items for the type
                // If the type is not already in the map, add it
                if (!categorizedItems.containsKey(type)) { categorizedItems.put(type, new ArrayList<>()); }
                Objects.requireNonNull(categorizedItems.get(type)).add(uri);
            }
        }
        items.close();
        db.close();

        // Listen for user pressing randomize outfit button, and randomizeOutfit() when pressed.
        Button randomizeButton = findViewById(R.id.randomize_button);
        randomizeButton.setOnClickListener(this::randomizeOutfit);

        // Listen for user pressing upload button, and redirect to UploadActivity when pressed.
        Button uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(RandomActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        // Listen for user pressing closet button, and redirect to ClosetActivity when pressed.
        Button closetButton = findViewById(R.id.closet_button);
        closetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ClosetActivity.class);
            startActivity(intent);
        });

        // Listen for user pressing logout button, and logout when pressed.
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            // Set the userID in shared preferences to -1
            SharedPreferences settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("userID", -1);
            editor.apply();

            // Display a message when logout button is clicked
            Toast.makeText(RandomActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Listen for user pressing save outfit button, and save the outfit when pressed.
        Button saveOutfitButton = findViewById(R.id.save_button);
        saveOutfitButton.setOnClickListener(v -> {
            // Get the current userID from shared preferences
            SharedPreferences settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
            int userID = settings.getInt("userID", -1);

            // Save the outfit if the user and all uri's are valid
            if (userID != -1 && topUri != null && shirtUri != null && pantUri != null && shoeUri != null) {
                SQLiteManager sqlManager = SQLiteManager.instanceOfDatabase(this);
                sqlManager.saveOutfit(userID, topUri, shirtUri, pantUri, shoeUri);
                sqlManager.close();
                Toast.makeText(RandomActivity.this, "Outfit saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RandomActivity.this, "Please generate a complete outfit before saving", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for user pressing lock buttons, and lock/unlock the item when pressed.
        Button lockTopButton = findViewById(R.id.lock_top_button);
        lockTopButton.setOnClickListener(v -> {
            if (isLocked("Top")) {
                // If the item is already locked, unlock it
                itemLockStatus.remove("Top");
                lockTopButton.setText(getString(R.string.lock_top_button));
            } else {
                // If the item is not locked, lock it
                itemLockStatus.put("Top", true);
                lockTopButton.setText(getString(R.string.unlock_top_button));
            }
        });
        Button lockShirtButton = findViewById(R.id.lock_shirt_button);
        lockShirtButton.setOnClickListener(v -> {
            if (isLocked("Shirt")) {
                itemLockStatus.remove("Shirt");
                lockShirtButton.setText(getString(R.string.lock_shirt_button));
            } else {
                itemLockStatus.put("Shirt", true);
                lockShirtButton.setText(getString(R.string.unlock_shirt_button));
            }
        });
        Button lockPantsButton = findViewById(R.id.lock_pants_button);
        lockPantsButton.setOnClickListener(v -> {
            if (isLocked("Pants")) {
                itemLockStatus.remove("Pants");
                lockPantsButton.setText(getString(R.string.lock_pants_button));
            } else {
                itemLockStatus.put("Pants", true);
                lockPantsButton.setText(getString(R.string.unlock_pants_button));
            }
        });
        Button lockShoesButton = findViewById(R.id.lock_shoes_button);
        lockShoesButton.setOnClickListener(v -> {
            if (isLocked("Shoes")) {
                itemLockStatus.remove("Shoes");
                lockShoesButton.setText(getString(R.string.lock_shoes_button));
            } else {
                itemLockStatus.put("Shoes", true);
                lockShoesButton.setText(getString(R.string.unlock_shoes_button));
            }
        });

        // Randomize an outfit when the activity is created
        randomizeOutfit(null);
    }

    /**
     * Randomize outfit when randomize button is clicked.
     * @param view The view that was clicked.
     */
    public void randomizeOutfit(View view) {
        // Randomly select one item from each category
        Random random = new Random();
        if (!isLocked("Top") && categorizedItems.get("Top") != null && !Objects.requireNonNull(categorizedItems.get("Top")).isEmpty()) {
            topUri = Objects.requireNonNull(categorizedItems.get("Top")).get(random.nextInt(Objects.requireNonNull(categorizedItems.get("Top")).size()));
        }
        if (!isLocked("Shirt") && categorizedItems.get("Shirt") != null && !Objects.requireNonNull(categorizedItems.get("Shirt")).isEmpty()) {
            shirtUri = Objects.requireNonNull(categorizedItems.get("Shirt")).get(random.nextInt(Objects.requireNonNull(categorizedItems.get("Shirt")).size()));
        }
        if (!isLocked("Pants") && categorizedItems.get("Pants") != null && !Objects.requireNonNull(categorizedItems.get("Pants")).isEmpty()) {
            pantUri = Objects.requireNonNull(categorizedItems.get("Pants")).get(random.nextInt(Objects.requireNonNull(categorizedItems.get("Pants")).size()));
        }
        if (!isLocked("Shoes") && categorizedItems.get("Shoes") != null && !Objects.requireNonNull(categorizedItems.get("Shoes")).isEmpty()) {
            shoeUri = Objects.requireNonNull(categorizedItems.get("Shoes")).get(random.nextInt(Objects.requireNonNull(categorizedItems.get("Shoes")).size()));
        }

        // Display the selected items using Glide only if they are not null
        if(topUri == null || shirtUri == null || pantUri == null || shoeUri == null) {
            Toast.makeText(RandomActivity.this,
                    "Please upload at least one item of each type", Toast.LENGTH_SHORT).show();
            return;
        }
        Glide.with(this)
                .load(topUri)
                .into((ImageView) findViewById(R.id.random_top_image));
        Glide.with(this)
                .load(shirtUri)
                .into((ImageView) findViewById(R.id.random_shirt_image));
        Glide.with(this)
                .load(pantUri)
                .into((ImageView) findViewById(R.id.random_pants_image));
        Glide.with(this)
                .load(shoeUri)
                .into((ImageView) findViewById(R.id.random_shoes_image));
    }

    /**
     * Check if the item is locked.
     * @param type The type of the item.
     * @return True if the item is locked, false otherwise.
     */
    private boolean isLocked(String type) {
        Boolean isLocked = itemLockStatus.get(type);
        return isLocked != null && isLocked;
    }
}
