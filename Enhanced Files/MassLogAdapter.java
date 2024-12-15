package com.example.masstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MassLogAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> massLogs;
    private int selectedPosition = -1;

    public MassLogAdapter(Context context, ArrayList<String> massLogs) {
        this.context = context;
        this.massLogs = massLogs;
    }

    @Override
    public int getCount() {
        return massLogs.size();
    }

    @Override
    public Object getItem(int position) {
        return massLogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.grid_item_text);
        textView.setText(massLogs.get(position));

        // Highlight the selected item
        if (position == selectedPosition) {
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        return convertView;
    }

    public void updateData(List<String> displayList) {
    }
}
