package msu.cse476.hakalaja.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SQLiteManager extends SQLiteOpenHelper
{
    public static SQLiteManager sqLiteManager;
    private static final String DATABASE_NAME = "GarmntDatabase";
    private static final int DATABASE_VERSION = 1;

    // User table
    public static final String USER_TABLE_NAME = "User";
    public static final String USER_ID = "userID";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // Item table
    public static final String ITEM_TABLE_NAME = "Item";
    public static final String ITEM_ID = "itemID";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String URI = "Uri";

    // Outfit table
    public static final String OUTFIT_TABLE_NAME = "Outfit";
    public static final String TOP_URI = "topUri";
    public static final String SHIRT_URI = "shirtUri";
    public static final String PANT_URI = "pantUri";
    public static final String SHOE_URI = "shoeUri";


    /**
     * Constructor for the SQLiteManager.
     * @param context The context of the application.
     */
    public SQLiteManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Get the instance of the database.
     * @param context The context of the application.
     * @return The instance of the database.
     */
    public static SQLiteManager instanceOfDatabase(Context context)
    {
        if(sqLiteManager == null)
        {
            sqLiteManager = new SQLiteManager(context);
        }
        return sqLiteManager;
    }

    /**
     * Create the tables.
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        // Create User table
        StringBuilder sqlUserTable = new StringBuilder()
                .append("CREATE TABLE ")
                .append(USER_TABLE_NAME)
                .append("(")
                .append(USER_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(USERNAME)
                .append(" TEXT,")
                .append(PASSWORD)
                .append(" TEXT)");
        sqLiteDatabase.execSQL(sqlUserTable.toString());

        // Create Item table
        StringBuilder sqlItemTable = new StringBuilder()
                .append("CREATE TABLE ")
                .append(ITEM_TABLE_NAME)
                .append("(")
                .append(ITEM_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(NAME)
                .append(" TEXT,")
                .append(TYPE)
                .append(" TEXT,")
                .append(URI)
                .append(" TEXT,")
                .append(USER_ID)
                .append(" INTEGER,")
                .append("FOREIGN KEY(")
                .append(USER_ID)
                .append(") REFERENCES ")
                .append(USER_TABLE_NAME)
                .append("(")
                .append(USER_ID)
                .append("))");
        sqLiteDatabase.execSQL(sqlItemTable.toString());

        // Create Outfit table
        StringBuilder sqlOutfitTable = new StringBuilder()
                .append("CREATE TABLE ")
                .append(OUTFIT_TABLE_NAME)
                .append("(")
                .append(USER_ID)
                .append(" INTEGER,")
                .append(TOP_URI)
                .append(" TEXT,")
                .append(SHIRT_URI)
                .append(" TEXT,")
                .append(PANT_URI)
                .append(" TEXT,")
                .append(SHOE_URI)
                .append(" TEXT,")
                .append("FOREIGN KEY(")
                .append(USER_ID)
                .append(") REFERENCES ")
                .append(USER_TABLE_NAME)
                .append("(")
                .append(USER_ID)
                .append("))");
        sqLiteDatabase.execSQL(sqlOutfitTable.toString());
    }

    /**
     * Check if a user exists in the User table.
     * @param username The username to check.
     * @param password The password to check.
     * @return True if the user exists, false otherwise.
     */
    public boolean userExists(String username, String password) {
        String hashedPassword = hashPassword(password); // Hash the password
        String sql = "SELECT * FROM User WHERE username = ? AND password = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] {username, hashedPassword}); // Compare with the hashed password
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists; // Return true if user exists, false otherwise
    }

    /**
     * Check if a username exists in the User table.
     * @param username The username to check.
     * @return True if the username exists, false otherwise.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT * FROM User WHERE username = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] {username});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists; // Return true if username exists, false otherwise
    }

    /**
     * Insert a user into the User table.
     * @param username The username to insert.
     * @param password The password to insert.
     */
    public void insertUser(String username, String password) {
        String hashedPassword = hashPassword(password); // Hash the password
        String sql = "INSERT INTO User (username, password) VALUES (?, ?)";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1, username);
        stmt.bindString(2, hashedPassword); // Store the hashed password
        stmt.executeInsert();
    }

    /**
     * Get the userID of a user from the User table.
     * @param username The username to get the userID of.
     * @param password The password to get the userID of.
     * @return The userID of the user, or -1 if the user is not found.
     */
    public int getUserID(String username, String password) {
        String hashedPassword = hashPassword(password); // Hash the password
        String sql = "SELECT userID FROM User WHERE username = ? AND password = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] {username, hashedPassword});
        if (cursor.moveToFirst()) {
            int userID = cursor.getInt(0);
            cursor.close();
            return userID;
        }
        else {
            cursor.close();
            return -1; // Return -1 if user not found
        }
    }

    /**
     * Get all items for a user from the Item table.
     * @param userID The userID to get the items for.
     * @return A cursor containing all items for the user.
     */
    public Cursor getItems(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM Item WHERE userID = ?";
        return db.rawQuery(sql, new String[] {Integer.toString(userID)});
    }

    /**
     * Get all outfits for a user from the Outfit table.
     * @param userID The userID to get the outfits for.
     * @param topUri The top URI to get the outfits for.
     * @param shirtUri The shirt URI to get the outfits for.
     * @param pantUri The pant URI to get the outfits for.
     * @param shoeUri The shoe URI to get the outfits for.
     */
    public void saveOutfit(int userID, String topUri, String shirtUri, String pantUri, String shoeUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_ID, userID);
        contentValues.put(TOP_URI, topUri);
        contentValues.put(SHIRT_URI, shirtUri);
        contentValues.put(PANT_URI, pantUri);
        contentValues.put(SHOE_URI, shoeUri);
        db.insert(OUTFIT_TABLE_NAME, null, contentValues);
    }

    /**
     * Hash a password using MD5.
     * @param password The password to hash.
     * @return The hashed password.
     */
    public String hashPassword(String password) {
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Add password bytes to digest
            md.update(password.getBytes());
            // Get the hash's bytes
            byte[] bytes = md.digest();
            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            // Get complete hashed password in hexadecimal format
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        // Could be added later to implement further upgrade-ability for database contents.
        /*
        switch(oldVersion)
        {
            case 1:
                sqLiteDatabase.execSQL( .... ); /// Alter table accordingly
                break;
            case 2:
                sqLiteDatabase.execSQL( .... ); /// Alter table accordingly
        }
         */
    }
}
