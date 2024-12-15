package com.example.masstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * MassRepository manages all interactions with the database.
 */
public class MassRepository {
    private final DatabaseHelper dbHelper;

    /**
     * Constructor initializes the DatabaseHelper.
     *
     * @param context Application context.
     */
    public MassRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Fetches all mass logs for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of mass log strings in the format "date - mass".
     */
    public List<String> getMassLogs(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> logs = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_MASS,
                new String[]{DatabaseHelper.COLUMN_DATE, DatabaseHelper.COLUMN_MASS},
                DatabaseHelper.COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                double mass = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MASS));
                logs.add(date + " - " + mass);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return logs;
    }

    /**
     * Adds a new mass log for the user.
     *
     * @param userId The user ID.
     * @param date   The date of the log.
     * @param mass   The mass value.
     * @return True if the log was added successfully, otherwise false.
     */
    public boolean addMassLog(int userId, String date, double mass) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_MASS, mass);

        long result = db.insert(DatabaseHelper.TABLE_MASS, null, values);
        return result != -1;
    }

    /**
     * Retrieves all mass logs for a specific user.
     *
     * @param userId The ID of the user whose mass logs are being retrieved.
     * @return A HashMap where the keys are dates (String) and the values are mass values (Double).
     */
    public HashMap<String, Double> getAllMassLogs(int userId) {
        HashMap<String, Double> massLogs = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the database to fetch mass logs for the specified user
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MASS,
                new String[]{DatabaseHelper.COLUMN_DATE, DatabaseHelper.COLUMN_MASS},
                DatabaseHelper.COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        // Populate the HashMap with data from the database
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                double mass = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MASS));
                massLogs.put(date, mass);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return massLogs;
    }
}
