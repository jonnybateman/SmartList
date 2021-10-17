package com.development.smartlist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogUser extends DialogFragment{

    // Defines the listener interface with a method for passing back the user name info from the
    // dialog box.
    interface DialogListener {
        void onFinishDialogUser(int userId, String userName, String userUpdateType);
    }

    @SuppressWarnings({"ConstantConditions"})
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Initialize variables, adapters and class instances
        DBAdapter dbAdapter = new DBAdapter(getActivity());
        final int userId;
        final String userUpdateType;

        // Create an instance of Dialog.
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(true);
        // Set the custom dialog's layout to the dialog
        dialog.setContentView(R.layout.dialog_user);
        // Make the window background in which the dialog sits transparent.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize views of the custom dialog.
        TextView txtDialogTitle = dialog.findViewById(R.id.dialogUserTitle);
        Button btnDialog = dialog.findViewById(R.id.btnDialogCreateUser);
        final EditText edtUserName = dialog.findViewById(R.id.dialogUserNameEDT);
        // Set the dialog title
        txtDialogTitle.setText(getString(R.string.dialog_title_create_edit_user));

        dbAdapter.open();
        // Populate EditText with current username if any.
        String userName = dbAdapter.getUserName();
        // Get the user id if it exists.
        userId = dbAdapter.getUserId();
        dbAdapter.close();

        // Determine if we are creating a user or modifying one.
        if (userName.length() > 0) {
            userUpdateType = "modify";
        } else {
            userUpdateType = "new";
        }

        // Set EditText field with username.
        edtUserName.setText(userName);
        // Set the button text.
        btnDialog.setText(getString(R.string.button_save));

        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // Create instance of interface listener.
                DialogUser.DialogListener listener = (DialogUser.DialogListener) getActivity();
                // Retrieve info from AutoCompleteTextView and pass back to activity.
                if (edtUserName.length() > 0) {
                    listener.onFinishDialogUser(userId, edtUserName.getText().toString(), userUpdateType);
                }
            }
        });

        return dialog;
    }
}