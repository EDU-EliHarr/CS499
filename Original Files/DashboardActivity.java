package com.example.masstracker;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private GridView massGridView;
    private Button addButton, updateButton, deleteButton;
    private TextView goalTextView;
    private MassLogAdapter adapter;
    private ArrayList<String> massList = new ArrayList<>();
    private int selectedUserId;
    private String selectedDate;
    private double selectedMass;
    private double userGoal;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        massGridView = findViewById(R.id.massGridView);
        addButton = findViewById(R.id.addButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        goalTextView = findViewById(R.id.goalTextView);

        selectedUserId = getIntent().getIntExtra("userId", -1);
        userGoal = getIntent().getDoubleExtra("goal", -1);

        if (selectedUserId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the user ID is not passed correctly
            return;
        }

        adapter = new MassLogAdapter(this, massList);
        massGridView.setAdapter(adapter);

        if (userGoal == -1) {
            showSetGoalDialog();
        } else {
            goalTextView.setText("Goal: " + userGoal + " kg");
        }

        loadMassData();

        massGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] massData = massList.get(position).split(" - ");
                selectedDate = massData[0];
                selectedMass = Double.parseDouble(massData[1]);
                selectedPosition = position;
                adapter.setSelectedPosition(position); // Highlight the selected item
            }
        });

        addButton.setOnClickListener(v -> showAddMassDialog());
        updateButton.setOnClickListener(v -> showUpdateMassDialog());
        deleteButton.setOnClickListener(v -> deleteMassData());
    }

    @Override
    public void onBackPressed() {
        // Do nothing to prevent back navigation to the login page
    }

    private void showSetGoalDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_set_goal);
        EditText goalEditText = dialog.findViewById(R.id.goalEditText);
        Button saveGoalButton = dialog.findViewById(R.id.saveGoalButton);

        saveGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goalStr = goalEditText.getText().toString().trim();
                if (goalStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Please enter a goal", Toast.LENGTH_SHORT).show();
                    return;
                }

                double goal = Double.parseDouble(goalStr);
                userGoal = goal;
                goalTextView.setText("Goal: " + userGoal + " kg");

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_GOAL, goal);
                db.update(DatabaseHelper.TABLE_USERS, values, DatabaseHelper.COLUMN_USER_ID + "=?",
                        new String[]{String.valueOf(selectedUserId)});

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loadMassData() {
        massList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MASS,
                new String[]{DatabaseHelper.COLUMN_DATE, DatabaseHelper.COLUMN_MASS},
                DatabaseHelper.COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(selectedUserId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                double mass = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MASS));
                massList.add(date + " - " + mass);
                checkGoalReached(mass); // Check if the goal is reached
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void showAddMassDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_mass);

        EditText dateEditText = dialog.findViewById(R.id.dateEditText);
        EditText massEditText = dialog.findViewById(R.id.massEditText);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button todayButton = dialog.findViewById(R.id.todayButton);

        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                dateEditText.setText(currentDate);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = dateEditText.getText().toString().trim();
                String massStr = massEditText.getText().toString().trim();

                if (date.isEmpty() || massStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Please enter both date and mass", Toast.LENGTH_SHORT).show();
                    return;
                }

                double mass = Double.parseDouble(massStr);
                addMassData(date, mass);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showUpdateMassDialog() {
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a record to update", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_mass);

        EditText dateEditText = dialog.findViewById(R.id.dateEditText);
        EditText massEditText = dialog.findViewById(R.id.massEditText);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button todayButton = dialog.findViewById(R.id.todayButton);

        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                dateEditText.setText(currentDate);
            }
        });

        // Pre-fill the dialog with the selected mass log data
        dateEditText.setText(selectedDate);
        massEditText.setText(String.valueOf(selectedMass));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDate = dateEditText.getText().toString().trim();
                String newMassStr = massEditText.getText().toString().trim();

                if (newDate.isEmpty() || newMassStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Please enter both date and mass", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newMass = Double.parseDouble(newMassStr);
                updateMassData(newDate, newMass);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addMassData(String date, double mass) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, selectedUserId);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_MASS, mass);

        long newRowId = db.insert(DatabaseHelper.TABLE_MASS, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Mass log added", Toast.LENGTH_SHORT).show();
            loadMassData(); // Refresh the data in the GridView
        } else {
            Toast.makeText(this, "Failed to add mass log", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMassData(String date, double mass) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_MASS, mass);

        int rowsUpdated = db.update(DatabaseHelper.TABLE_MASS, values,
                DatabaseHelper.COLUMN_USER_ID_FK + "=? AND " + DatabaseHelper.COLUMN_DATE + "=?",
                new String[]{String.valueOf(selectedUserId), selectedDate});

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Mass log updated", Toast.LENGTH_SHORT).show();
            loadMassData(); // Refresh the data in the GridView
        } else {
            Toast.makeText(this, "Failed to update mass log", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMassData() {
        if (selectedDate != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete(DatabaseHelper.TABLE_MASS,
                    DatabaseHelper.COLUMN_USER_ID_FK + "=? AND " + DatabaseHelper.COLUMN_DATE + "=?",
                    new String[]{String.valueOf(selectedUserId), selectedDate});

            if (rowsDeleted > 0) {
                Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
                loadMassData();
            } else {
                Toast.makeText(this, "Failed to delete record", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a record to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGoalReached(double mass) {
        if (mass <= userGoal) {
            Toast.makeText(this, "Congratulations! You've reached your goal!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                sendSmsNotification("1234567890", "Congratulations! You've reached your goal!");
            }
        }
    }

    private void sendSmsNotification(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
}
