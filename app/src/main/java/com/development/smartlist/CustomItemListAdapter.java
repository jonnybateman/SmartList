package com.development.smartlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CustomItemListAdapter extends ArrayAdapter<Item> {

    private final Activity context;
    private final ArrayList<Item> itemObjectsAndCategoryDividers;
    private DBAdapter dbAdapter;

    CustomItemListAdapter(Activity context, ArrayList<Item> itemObjectsAndCategoryDividers) {
        super(context, R.layout.list_item_row, itemObjectsAndCategoryDividers);
        this.context = context;
        this.itemObjectsAndCategoryDividers = itemObjectsAndCategoryDividers;
        dbAdapter = new DBAdapter(getContext());
    }

    public View getView(final int position, View view, ViewGroup parent) {

        final String KEY_ITEM_INFO_TITLE = "ITEM_INFO_TITLE";
        final String KEY_ITEM_INFO_ID = "ITEM_INFO_ID";
        final String DIALOG_TYPE_ITEM_INFO = "ITEM_INFO";

        LayoutInflater inflater=context.getLayoutInflater();

        // Initialize variables, adapters and class instances
        //final DBAdapter dbAdapter = new DBAdapter(getContext());

        final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

        if (itemObjectsAndCategoryDividers.get(position).getDivider()) {
            view = inflater.inflate(R.layout.list_divider_row, parent, false);
            TextView txtItem = view.findViewById(R.id.txtDivider);
            txtItem.setText(itemObjectsAndCategoryDividers.get(position).getCatName());
        }
        else {
            view = inflater.inflate(R.layout.list_item_row, parent, false);
            TextView txtItem = view.findViewById(R.id.txtItem);
            TextView txtQtyWeight = view.findViewById(R.id.txtQtyWeight);
            txtItem.setText(itemObjectsAndCategoryDividers.get(position).getItemName());
            txtQtyWeight.setText(itemObjectsAndCategoryDividers.get(position).getQuantity());
            ImageView imgInfo = view.findViewById(R.id.imgInfo);
            final CheckBox chkBox = view.findViewById(R.id.chkBoxItem);
            chkBox.setChecked(itemObjectsAndCategoryDividers.get(position).getItemChecked());

            if (itemObjectsAndCategoryDividers.get(position).getItemSelected()) {
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.listViewSelectorColor));
            }

            // Create a listener for each checkbox. If set, update the corresponding Item object
            // to show that the item has been checked.
            chkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Find the item position in the global item list and set the checked flag.
                    int globalItemPosition = itemObjectsAndCategoryDividers.get(position).getItemPosition();

                    if (chkBox.isChecked()) {
                        itemObjectsAndCategoryDividers.get(position).setItemChecked(true);
                        if (globalItemPosition != -1) {
                            globalObject.getItemArrayList().get(globalItemPosition).setItemChecked(true);
                        }
                    }
                    else {
                        itemObjectsAndCategoryDividers.get(position).setItemChecked(false);
                        if (globalItemPosition != -1) {
                            globalObject.getItemArrayList().get(globalItemPosition).setItemChecked(false);
                        }
                    }
                }
            });

            // Create a listener for each items info icon. This will open a fragment to view item
            // information.
            imgInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Get the applicable item information
                    String itemName = itemObjectsAndCategoryDividers.get(position).getItemName();
                    int itemId = itemObjectsAndCategoryDividers.get(position).getItemId();
                    // See if there is any item info for the item id, if not then no need to
                    // display the item info dialog.
                    dbAdapter.open();
                    if (dbAdapter.getItemInfoCount(itemId, globalObject.getCurrency()) > 0) {
                        // Create bundle for passing to dialog.
                        Bundle bundleItemInfo = new Bundle();
                        bundleItemInfo.putString(KEY_ITEM_INFO_TITLE, itemName);
                        bundleItemInfo.putInt(KEY_ITEM_INFO_ID, itemId);
                        // Create instance of DialogItemInfo.
                        DialogItemInfo dialogItemInfo = new DialogItemInfo();
                        // Set the arguments to be passed to the dialog.
                        dialogItemInfo.setArguments(bundleItemInfo);
                        // Create fragment manager for the dialog fragment.
                        FragmentManager fragmentManager = context.getFragmentManager();
                        dialogItemInfo.show(fragmentManager, DIALOG_TYPE_ITEM_INFO);
                    }
                    dbAdapter.close();
                }
            });
        }

        return view;

    }
}
