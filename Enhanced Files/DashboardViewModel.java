package com.example.masstracker;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardViewModel provides business logic for the DashboardActivity,
 * handling data retrieval, sorting, searching, and pagination.
 */
public class DashboardViewModel {

    private MassRepository repository;

    public DashboardViewModel(Context context) {
        repository = new MassRepository(context);
    }

    /**
     * Retrieves a paginated subset of logs.
     *
     * @param logs     The complete set of logs.
     * @param page     The current page number.
     * @param pageSize The number of items per page.
     * @return A list of entries representing the logs for the current page.
     */
    public List<Map.Entry<String, Double>> getPaginatedLogs(HashMap<String, Double> logs, int page, int pageSize) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(logs.entrySet());
        int start = page * pageSize;
        int end = Math.min(start + pageSize, list.size());
        return list.subList(start, end);
    }

    /**
     * Determines if there are more pages of logs.
     *
     * @param logs     The complete set of logs.
     * @param page     The current page number.
     * @param pageSize The number of items per page.
     * @return True if there are more pages; false otherwise.
     */
    public boolean hasNextPage(HashMap<String, Double> logs, int page, int pageSize) {
        return (page + 1) * pageSize < logs.size();
    }

    /**
     * Sorts logs based on the specified sorting criteria.
     *
     * @param logs   The complete set of logs.
     * @param option The sorting option (0-3).
     * @return A sorted HashMap of logs.
     */
    public HashMap<String, Double> sortLogs(HashMap<String, Double> logs, int option) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(logs.entrySet());
        switch (option) {
            case 0: // Date Ascending
                list.sort(Map.Entry.comparingByKey());
                break;
            case 1: // Date Descending
                list.sort((a, b) -> b.getKey().compareTo(a.getKey()));
                break;
            case 2: // Mass Ascending
                list.sort(Map.Entry.comparingByValue());
                break;
            case 3: // Mass Descending
                list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                break;
        }
        HashMap<String, Double> sortedLogs = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedLogs.put(entry.getKey(), entry.getValue());
        }
        return sortedLogs;
    }

    /**
     * Finds the log closest to the specified mass.
     *
     * @param logs The complete set of logs.
     * @param mass The target mass.
     * @return The closest log as a String.
     */
    public String searchByMass(HashMap<String, Double> logs, double mass) {
        return logs.entrySet().stream()
                .min((a, b) -> Double.compare(Math.abs(a.getValue() - mass), Math.abs(b.getValue() - mass)))
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .orElse("No logs found");
    }

    /**
     * Finds the log closest to the specified date.
     *
     * @param logs The complete set of logs.
     * @param date The target date.
     * @return The closest log as a String.
     */
    public String searchByDate(HashMap<String, Double> logs, String date) {
        return logs.entrySet().stream()
                .min((a, b) -> Integer.compare(Math.abs(a.getKey().compareTo(date)), Math.abs(b.getKey().compareTo(date))))
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .orElse("No logs found");
    }

    public HashMap<String, Double> getMassLogs(int userId) {
        return (HashMap<String, Double>) repository.getMassLogs(userId);
    }
}
