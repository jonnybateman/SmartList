package com.development.custom_dialog_add_delete_10;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import com.development.custom_input_filter.CustomInputFilter;

public class DialogAddDelete extends DialogFragment {

    private static final String DIALOG_ACTION_ADD = "add";
    private static final String DIALOG_ACTION_DELETE = "delete";

    // Defines the listener interface with a method for passing back the category name info from the
    // dialog box.
    public interface DialogListener {
        void onFinishDialogAddDelete(String name, String dialog_type, String dialog_action);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create an instance of Dialog.
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(true);
        // Set the custom dialog's layout to the dialog
        dialog.setContentView(R.layout.dialog_add_delete);
        // Make the window background in which the dialog sits transparent.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize views of the custom dialog.
        ImageView imgViewIcon = (ImageView)dialog.findViewById(R.id.icon_dialog_add_delete);
        TextView txtAddDelete = (TextView) dialog.findViewById(R.id.dialogAddDeleteTitle);
        Button btnAdd = (Button)dialog.findViewById(R.id.dialogAddBtn);
        btnAdd.setText(R.string.dialog_button_add);
        Button btnDelete = (Button)dialog.findViewById(R.id.dialogDeleteBtn);
        btnDelete.setText(R.string.dialog_button_delete);
        final AutoCompleteTextView actvName = (AutoCompleteTextView) dialog.findViewById(R.id.dialogAddDeleteACTV);
        actvName.setFilters(new InputFilter[] { new CustomInputFilter() });

        // Extract the arguments from the bundle.
        final String dialog_type = getArguments().getString("dialog_type");
        ArrayList<String> list = getArguments().getStringArrayList("list");
        Bitmap icon = getArguments().getParcelable("icon");
        String title = getArguments().getString("title");

        if (dialog_type != null && list != null) {

            // Populate the AutoCompleteTextView with category data
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, list);
            actvName.setAdapter(categoryAdapter);
            actvName.setThreshold(1);
        }

        // Set the icon and title of the dialog.
        imgViewIcon.setImageBitmap(icon);
        txtAddDelete.setText(title);

        // Create listener on the dialog's add button.
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); // dismiss the dialog.
                // Create instance of interface listener.
                DialogAddDelete.DialogListener listener = (DialogAddDelete.DialogListener) getActivity();
                // Retrieve info from AutoCompleteTextView and pass back to Activity.
                listener.onFinishDialogAddDelete(actvName.getText().toString(), dialog_type, DIALOG_ACTION_ADD);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // Create instance of interface listener.
                DialogAddDelete.DialogListener listener = (DialogAddDelete.DialogListener) getActivity();
                // Retrieve info from AutoCompleteTextView and pass back to activity.
                listener.onFinishDialogAddDelete(actvName.getText().toString(), dialog_type, DIALOG_ACTION_DELETE);
            }
        });

        return dialog;
    }
}
