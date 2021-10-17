package com.development.custom_dialog_single_button;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class DialogSingleButton extends DialogFragment {

    // Defines the listener interface with a method for passing back the category name info from the
    // dialog box.
    public interface DialogListener {
        void onFinishDialogSingleButton(String name,String dialog_type);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create an instance of Dialog.
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(true);

        // Set the custom dialog's layout to the dialog
        dialog.setContentView(R.layout.dialog_single_button);

        // Make the window background in which the dialog sits transparent.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize views of the custom dialog.
        ImageView imgViewIcon = dialog.findViewById(R.id.icon_dialog_single_button);
        TextView txtDialogTitle = dialog.findViewById(R.id.dialogSingleButtonTitle);
        Button btnDialog = dialog.findViewById(R.id.btnDialogSingleButton);
        final AutoCompleteTextView actvName = dialog.findViewById(R.id.dialogDeleteItemACTV);

        // Extract the arguments from the bundle.
        final String dialog_type = getArguments().getString("dialog_type");
        ArrayList<String> list = getArguments().getStringArrayList("list");
        Bitmap icon = getArguments().getParcelable("icon");
        String title = getArguments().getString("title");
        String buttonText = getArguments().getString("button_text");

        // Set dialog icon.
        imgViewIcon.setImageBitmap(icon);
        // Set dialog title
        txtDialogTitle.setText(title);
        // Set button text
        btnDialog.setText(buttonText);
        // Populate the AutoCompleteTextView
        if (list != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, list);
            actvName.setAdapter(arrayAdapter);
            actvName.setThreshold(1);
        }

        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // Create instance of interface listener.
                DialogSingleButton.DialogListener listener = (DialogSingleButton.DialogListener) getActivity();
                // Retrieve info from AutoCompleteTextView and pass back to activity.
                listener.onFinishDialogSingleButton(actvName.getText().toString(), dialog_type);
            }
        });

        return dialog;
    }
}