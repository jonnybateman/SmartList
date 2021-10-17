package com.development.custom_spinner_adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/*
 * Custom Adapter class for Spinner control.
 */
public class SpinnerAdapter extends ArrayAdapter<String> {

    private List<String> objects;

    public SpinnerAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View row = inflater.inflate(R.layout.spinner_item, parent, false);

        // Setup the TextView for the current spinner item
        TextView spinnerItem = row.findViewById(R.id.spinnerTxtItem);
        spinnerItem.setText(objects.get(position));

        // Create a divider to separate spinner items.
        ImageView imgView = (ImageView)row.findViewById(R.id.imgView);
        imgView.setImageResource(R.drawable.spinner_divider);

        return row;
    }
}
