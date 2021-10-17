package com.development.smartlist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

public class DialogItemInfo extends DialogFragment {

    String KEY_ITEM_INFO_TITLE = "ITEM_INFO_TITLE";
    String KEY_ITEM_INFO_ID = "ITEM_INFO_ID";
    private static final String FIELD_TYPE_SHOP = "shop";
    private static final String FIELD_TYPE_BRAND = "brand";
    private static final String FIELD_TYPE_PRICE = "price";

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Calling Application class (see application tag in AndroidManifest.xml)
        final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

        // Create an instance of Dialog.
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(true);
        // Set the custom dialog's layout to the dialog
        dialog.setContentView(R.layout.dialog_item_info);
        // Make the window background in which the dialog sits transparent.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Initialize views of the custom dialog.
        TextView txtItemInfoTitle = (TextView) dialog.findViewById(R.id.dialogItemInfoTitle);
        String itemInfoTitle = getArguments().getString(KEY_ITEM_INFO_TITLE);
        txtItemInfoTitle.setText(itemInfoTitle);

        // Get the item info for the passed item id.
        DBAdapter dbAdapter = new DBAdapter(getActivity());
        dbAdapter.open();
        ArrayList<Object[]> items = dbAdapter.getDialogItemInfo(getArguments().getInt(KEY_ITEM_INFO_ID),
                globalObject.getCurrency());

        // Determine screen width in px.
        int screenWidthPx = getResources().getDisplayMetrics().widthPixels;
        // Set the width of the dialog.
        int dialogWidthPx = (int) Math.ceil(screenWidthPx * 0.8);
        // Ensure dialog width size is divisible by 3
        int remainder = dialogWidthPx % 3;
        if (remainder !=0) {
            dialogWidthPx = dialogWidthPx - remainder;
        }
        // Set the column size
        int columnWidthPx = dialogWidthPx / 3;

        TableLayout tableDialogItemInfo = (TableLayout) dialog.findViewById(R.id.table_dialog_item_info);
        TableRow infoRow;
        if (items.size() > 0) {
            for (int i=0; i < items.size(); i++) {

                Object[] item;
                item = items.get(i);
                String shop = (String)item[0];
                String brand = (String)item[1];
                Float price = (Float)item[2];
                infoRow = new TableRow(getContext());
                TableRow.LayoutParams params1 = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                infoRow.setLayoutParams(params1);
                infoRow.addView(makeTableRow(shop, FIELD_TYPE_SHOP, columnWidthPx));
                infoRow.addView(makeTableRow(brand,FIELD_TYPE_BRAND, columnWidthPx));
                infoRow.addView(makeTableRow(NumberFormat.getCurrencyInstance().format(price),
                        FIELD_TYPE_PRICE, columnWidthPx));
                tableDialogItemInfo.addView(infoRow);
            }
        }
        dbAdapter.close();

        return dialog;
    }

    public TextView makeTableRow(String text, String fieldType, int columnWidthPx) {
        TextView recyclableTextView = new TextView(getContext());

        recyclableTextView.setPadding(
                getResources().getDimensionPixelSize(R.dimen.table_padding),
                getResources().getDimensionPixelSize(R.dimen.table_padding_top),
                getResources().getDimensionPixelSize(R.dimen.table_padding),
                getResources().getDimensionPixelSize(R.dimen.table_padding_bottom));
        recyclableTextView.setTextAppearance(R.style.TableDataTextStyle);
        switch (fieldType) {
            case FIELD_TYPE_SHOP:
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                break;
            case FIELD_TYPE_BRAND:
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
                break;
            case FIELD_TYPE_PRICE:
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                recyclableTextView.setMaxLines(1);
                break;
        }
        recyclableTextView.setWidth(columnWidthPx);
        recyclableTextView.setText(text);

        return recyclableTextView;
    }
}
