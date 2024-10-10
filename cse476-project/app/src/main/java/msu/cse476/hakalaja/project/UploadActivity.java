package msu.cse476.hakalaja.project;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/** @noinspection BlockingMethodInNonBlockingContext, FieldCanBeLocal */
public class UploadActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 13;
    private Button buttonUpload;
    private ImageView imageView;
    private FirebaseStorage storage;
    private Uri imageUri;
    AutoCompleteTextView dropdown;
    private String[] items = {"Top", "Shirt", "Pants", "Shoes"};
    ArrayAdapter<String> adapter;
    EditText itemName;
    int userID = -1;
    String uri;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in the savedInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Listen for user pressing randomize button, and take user to RandomActivity when pressed.
        Button randomizeButton = findViewById(R.id.randomize_button);
        randomizeButton.setOnClickListener(v -> {
            Intent intent = new Intent(UploadActivity.this, RandomActivity.class);
            startActivity(intent);
        });

        // Listen for user pressing closet button, and redirect to ClosetActivity when pressed.
        Button closetButton = findViewById(R.id.closet_button);
        closetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ClosetActivity.class);
            startActivity(intent);
        });

        // Listen for user pressing logout button, and logout() when pressed.
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            // Set the userID in shared preferences to -1
            SharedPreferences settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("userID", -1);
            editor.apply();

            // Display a message when logout button is clicked
            Toast.makeText(UploadActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonUpload = findViewById(R.id.buttonUpload); // Get the button for uploading an image
        buttonUpload.setOnClickListener(this::onAddToCloset);
        imageView = findViewById(R.id.imageView);  // Get the imageView for displaying the image
        dropdown = findViewById(R.id.dropdown_menu); // Get the dropdown for selecting the item type
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        dropdown.setAdapter(adapter);

        itemName = findViewById(R.id.editTextItemName); // Get the editText for the item name

        // Take the photo of the item when the activity is created
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            // Create file for the image
            File tempFile = null;
            try {
                // TODO If publishing or trying to run on an actual device, use the path below for imagePath:
                // File imagePath = new File("Android/data/msu.cse476.hakalaja.project/files/Pictures")
                File imagePath = new File("/storage/emulated/0/Android/data/msu.cse476.hakalaja.project/files/Pictures/");
                tempFile = File.createTempFile("tempFile",".jpg",imagePath);
            } catch (IOException e) {
                return;
            }
            // Start the camera activity and send result to tempFile
            imageUri = getUriForFile(Objects.requireNonNull(getApplicationContext()), "msu.cse476.hakalaja.project" + ".fileprovider",tempFile);
            camera.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(camera, REQUEST_CODE);
        } catch (Exception e) {
            Log.i("Error","unable to start camera activity");
        }
        storage = FirebaseStorage.getInstance();

        // Get the userID from shared preferences
        SharedPreferences settings = getSharedPreferences("my.app.packagename_preferences", Context.MODE_PRIVATE);
        userID = settings.getInt("userID", -1);
    }

    /**
     * Handle the user clicking the upload button.
     * @param view The view that was clicked.
     */
    public void onAddToCloset(View view) {
        if (uri == null) {
            // Take the photo of the item when the activity is created
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                // Create file for the image
                File tempFile = null;
                try {
                    // TODO If publishing or trying to run on an actual device, use the path below for imagePath:
                    // File imagePath = new File("Android/data/msu.cse476.hakalaja.project/files/Pictures")
                    File imagePath = new File("/storage/emulated/0/Android/data/msu.cse476.hakalaja.project/files/Pictures/");
                    tempFile = File.createTempFile("tempFile",".jpg",imagePath);
                } catch (IOException e) {
                    return;
                }
                // Start the camera activity and send result to tempFile
                imageUri = getUriForFile(Objects.requireNonNull(getApplicationContext()), "msu.cse476.hakalaja.project" + ".fileprovider",tempFile);
                camera.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(camera, REQUEST_CODE);
            } catch (Exception e) {
                Log.i("Error","unable to start camera activity");
            }
            return;
        }
        String type = dropdown.getText().toString();
        String name = itemName.getText().toString();

        // Check if the type, name, uri, and userID are valid to ensure no null items are added to the database
        if (type.isEmpty() || name.isEmpty() || uri.isEmpty() || userID == -1) {
            Toast.makeText(UploadActivity.this, "Please fill in all fields and take a picture before uploading", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the database
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        SQLiteDatabase db = sqLiteManager.getWritableDatabase();
        try {
            // Insert the item into the database
            ContentValues contentValues = new ContentValues();
            contentValues.put(SQLiteManager.NAME, name);
            contentValues.put(SQLiteManager.TYPE, type);
            contentValues.put(SQLiteManager.URI, uri);
            contentValues.put(SQLiteManager.USER_ID, userID);
            db.insert(SQLiteManager.ITEM_TABLE_NAME, null, contentValues);
            Toast.makeText(UploadActivity.this, "Item added to closet", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(UploadActivity.this, "Error adding item to closet", Toast.LENGTH_SHORT).show();
        }
        db.close(); // Close the database connection

        // Clear the item name and dropdown menu and reset the image
        itemName.setText("");
        dropdown.setText("");
        imageView.setImageResource(0);
        uri = null;
    }

    /**
     * Handle the result of the image capture activity.
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if image capture was successful
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                // Get the image from the camera
                Bitmap realBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                ByteArrayOutputStream realStream = new ByteArrayOutputStream();
                realBitmap.compress(Bitmap.CompressFormat.JPEG, 100, realStream);
                byte[] byteArray1 = realStream.toByteArray();

                // Upload the image to the server
                // TODO Get a unique reference for the image. "{user_name}_{date}_{time}.jpg" is an example format.
                String uniqueRef = userID + "_" + System.currentTimeMillis() + ".jpg";
                //StorageReference testRef = storage.getReference().child("test.jpg");
                StorageReference testRef = storage.getReference().child(uniqueRef);
                UploadTask uploadTask = testRef.putBytes(byteArray1);
                uploadTask.addOnFailureListener(e -> Toast.makeText(UploadActivity.this, "Upload failed", Toast.LENGTH_SHORT).show())
                        .addOnSuccessListener(taskSnapshot -> Toast.makeText(UploadActivity.this, "Upload successful", Toast.LENGTH_SHORT).show());

                // Get the image from the server
                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return testRef.getDownloadUrl(); // Continue to get download URL if the upload is successful
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult(); // Get the download URL
                        uri = downloadUri.toString(); // Store the download URL as a string

                        // Set imageView bitmap to the downloaded image
                        Glide.with(UploadActivity.this)
                                .load(downloadUri)
                                .into(imageView);
                    } else {
                        Toast.makeText(UploadActivity.this, "Image download failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Image capture failed or was cancelled
        else {
            Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show();
        }
    }
}

