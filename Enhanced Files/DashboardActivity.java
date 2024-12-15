package com.example.masstracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardActivity manages the main user interface for displaying, searching,
 * and interacting with mass logs. It supports pagination, sorting, and searching
 * functionalities.
 */
public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private GridView massGridView;
    private Button nextPageButton, prevPageButton, sortButton, searchButton;
    private EditText searchEditText;
    private MassLogAdapter adapter;
    private HashMap<String, Double> massLogs = new HashMap<>();
    private List<Map.Entry<String, Double>> paginatedList = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 5; // Number of logs per page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize ViewModel and UI components
        viewModel = new DashboardViewModel(this);
        massGridView = findViewById(R.id.massGridView);
        nextPageButton = findViewById(R.id.nextPageButton);
        prevPageButton = findViewById(R.id.prevPageButton);
        sortButton = findViewById(R.id.sortButton);
        searchButton = findViewById(R.id.searchButton);
        searchEditText = findViewById(R.id.searchEditText);

        adapter = new MassLogAdapter(this, new ArrayList<>());
        massGridView.setAdapter(adapter);

        // Load data and set event listeners
        loadMassData();
        nextPageButton.setOnClickListener(v -> nextPage());
        prevPageButton.setOnClickListener(v -> prevPage());
        sortButton.setOnClickListener(v -> sortLogs());
        searchButton.setOnClickListener(v -> searchLogs());
    }

    /**
     * Loads mass log data from the ViewModel and displays the first page.
     */
    private void loadMassData() {
        massLogs = viewModel.getMassLogs();
        paginateLogs();
    }

    /**
     * Paginates logs based on the current page and updates the UI.
     */
    private void paginateLogs() {
        paginatedList = viewModel.getPaginatedLogs(massLogs, currentPage, PAGE_SIZE);
        List<String> displayList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : paginatedList) {
            displayList.add(entry.getKey() + " - " + entry.getValue());
        }
        adapter.updateData(displayList);
        updatePaginationButtons();
    }

    /**
     * Updates the state of pagination buttons based on the current page.
     */
    private void updatePaginationButtons() {
        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(viewModel.hasNextPage(massLogs, currentPage, PAGE_SIZE));
    }

    /**
     * Moves to the next page and refreshes the data.
     */
    private void nextPage() {
        currentPage++;
        paginateLogs();
    }

    /**
     * Moves to the previous page and refreshes the data.
     */
    private void prevPage() {
        currentPage--;
        paginateLogs();
    }

    /**
     * Sorts the mass logs based on user-selected criteria (date or mass, ascending or descending).
     */
    private void sortLogs() {
        String[] sortOptions = {"Date Ascending", "Date Descending", "Mass Ascending", "Mass Descending"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by")
                .setItems(sortOptions, (dialog, which) -> {
                    massLogs = viewModel.sortLogs(massLogs, which);
                    currentPage = 0;
                    paginateLogs();
                })
                .show();
    }

    /**
     * Searches for a log based on the user's input, either by date or mass.
     */
    private void searchLogs() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Attempt to search by mass
            double massQuery = Double.parseDouble(query);
            String closestLog = viewModel.searchByMass(massLogs, massQuery);
            Toast.makeText(this, "Closest log: " + closestLog, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            // Search by date if input is not a valid number
            String closestLog = viewModel.searchByDate(massLogs, query);
            Toast.makeText(this, "Closest log: " + closestLog, Toast.LENGTH_SHORT).show();
        }
    }
}
