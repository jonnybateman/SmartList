package com.development.smartlist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DialogCurrencyConfirm extends DialogFragment {

    private static final String KEY_CURRENCY = "CURRENCY_CHANGE";

    // Defines the listener interface with a method for passing back the answer from the
    // dialog box.
    interface DialogListener {
        void onFinishDialogCurrencyConfirm(String locale);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Retrieve dialog arguments
        final String locale = getArguments().getString(KEY_CURRENCY);

        // Create an instance of Dialog.
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(true);
        // Set the custom dialog's layout to the dialog
        dialog.setContentView(R.layout.dialog_currency_confirm);
        // Make the window background in which the dialog sits transparent.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Initialize views of the custom dialog.
        Button btnYes = dialog.findViewById(R.id.btnCurrencyChangeYes);
        btnYes.setText(R.string.button_ok);
        Button btnCancel = dialog.findViewById(R.id.btnCurrencyChangeNo);
        btnCancel.setText(R.string.button_cancel);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create instance of interface listener.
                DialogCurrencyConfirm.DialogListener listener = (DialogCurrencyConfirm.DialogListener) getActivity();
                // Retrieve currency from dialog box and pass back to Activity.
                listener.onFinishDialogCurrencyConfirm(locale);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        return dialog;
    }
}
