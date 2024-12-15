package com.example.masstracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * DatabaseHelper handles the creation and management of the SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "massTracker.db";
    private static final int DATABASE_VERSION = 2; // Incremented version for upgrades

    // User table and columns
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_GOAL = "goal";

    // Mass table and columns
    public static final String TABLE_MASS = "mass";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_MASS = "mass";
    public static final String COLUMN_USER_ID_FK = "user_id_fk";

    // SQL statements for creating tables with constraints
    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " + // Ensures unique usernames
                    COLUMN_PASSWORD + " TEXT NOT NULL, " + // Ensures password is provided
                    COLUMN_GOAL + " REAL DEFAULT 0.0);"; // Default goal value set to 0.0

    private static final String TABLE_CREATE_MASS =
            "CREATE TABLE " + TABLE_MASS + " (" +
                    COLUMN_DATE + " TEXT NOT NULL, " + // Ensures a date is provided
                    COLUMN_MASS + " REAL NOT NULL, " + // Ensures mass value is provided
                    COLUMN_USER_ID_FK + " INTEGER NOT NULL, " + // Ensures foreign key exists
                    "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);";

    // Indexes for improving query performance
    private static final String CREATE_INDEX_DATE =
            "CREATE INDEX idx_date ON " + TABLE_MASS + "(" + COLUMN_DATE + ");";

    private static final String CREATE_INDEX_USER =
            "CREATE INDEX idx_user ON " + TABLE_MASS + "(" + COLUMN_USER_ID_FK + ");";

    private static final String CREATE_INDEX_MASS =
            "CREATE INDEX idx_mass ON " + TABLE_MASS + "(" + COLUMN_MASS + ");";

    /**
     * Constructor for DatabaseHelper.
     *
     * @param context The application context.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     *
     * @param db The database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_MASS);
        db.execSQL(CREATE_INDEX_DATE);
        db.execSQL(CREATE_INDEX_USER);
        db.execSQL(CREATE_INDEX_MASS);
    }

    /**
     * Called when the database needs to be upgraded.
     *
     * @param db         The database instance.
     * @param oldVersion The current version of the database.
     * @param newVersion The target version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MASS);
        onCreate(db);
    }

    /**
     * Resets the database by dropping all tables and recreating them.
     */
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MASS);
        onCreate(db);
    }

    /**
     * Exports the database to a specified backup location.
     *
     * @param backupPath The file path where the database should be backed up.
     * @return True if the backup was successful, false otherwise.
     */
    public boolean exportDatabase(String backupPath, Context context) {
        try {
            File dbFile = new File(context.getDatabasePath(DATABASE_NAME).getPath()); // Locate the database file
            File backupFile = new File(backupPath); // Define the backup file location
            FileChannel src = new FileInputStream(dbFile).getChannel();
            FileChannel dst = new FileOutputStream(backupFile).getChannel();
            dst.transferFrom(src, 0, src.size()); // Copy database content to backup file
            src.close();
            dst.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions
            return false;
        }
    }

    /**
     * Restores the database from a specified backup file.
     *
     * @param backupPath The file path of the backup database.
     * @return True if the restore was successful, false otherwise.
     */
    public boolean restoreDatabase(String backupPath, Context context) {
        try {
            File dbFile = new File(context.getDatabasePath(DATABASE_NAME).getPath()); // Locate the database file
            File backupFile = new File(backupPath); // Locate the backup file
            FileChannel src = new FileInputStream(backupFile).getChannel();
            FileChannel dst = new FileOutputStream(dbFile).getChannel();
            dst.transferFrom(src, 0, src.size()); // Overwrite current database with backup file
            src.close();
            dst.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions
            return false;
        }
    }
}