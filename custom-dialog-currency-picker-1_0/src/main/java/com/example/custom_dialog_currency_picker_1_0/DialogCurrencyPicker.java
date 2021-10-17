package com.example.custom_dialog_currency_picker_1_0;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

public class DialogCurrencyPicker extends DialogFragment {

    // Defines the listener interface with a method for passing back the currency info from the
    // dialog box.
    public interface DialogListener {
        void onFinishDialogCurrencyPicker(String currency);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Setup the the various currencies and associated flags.
        //final String[] arrayCurrency = new String[]{"GBP","EUR","CHF","USD"};
        final String[] arrayCurrency = getContext().getResources().getStringArray(R.array.array_currency);
        //final HashMap<Integer, Integer> flags = new HashMap<>();
        final SparseIntArray flags = new SparseIntArray();
        flags.put(0, R.mipmap.flag_uk_tns);
        flags.put(1, R.mipmap.flag_eu_tns);
        flags.put(2, R.mipmap.flag_ch_tns);
        flags.put(3, R.mipmap.flag_us_tns);
        // add all drawables like this

        // Create an instance of Dialog.
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(true);
        // Set the custom dialog's layout to the dialog
        dialog.setContentView(R.layout.dialog_currency_picker);
        // Make the window background in which the dialog sits transparent.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Initialize views of the custom dialog.
        TextView txtCurrencyTitle = dialog.findViewById(R.id.dialogCurrencyPickerTitle);
        Button btnCurrencySelect = dialog.findViewById(R.id.dialogCurrencyPickerBtn);
        final ImageView imgFlag = dialog.findViewById(R.id.dialogCurrencyPickerFlag);
        final NumberPicker pickerCurrency = dialog.findViewById(R.id.dialogCurrencyPicker);
        // Setup the views of the custom dialog.
        txtCurrencyTitle.setText(R.string.dialog_title_currency);
        imgFlag.setImageResource(flags.get(0));
        btnCurrencySelect.setText(R.string.button_ok);
        pickerCurrency.setMinValue(0);
        pickerCurrency.setMaxValue(arrayCurrency.length - 1);
        // Implement text array to number picker
        pickerCurrency.setDisplayedValues(arrayCurrency);
        // Disable the soft keyboard so that it is not displayed when scrolling.
        pickerCurrency.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        // Set wrap true or false.
        pickerCurrency.setWrapSelectorWheel(true);

        // Create listener for picker change in value.
        pickerCurrency.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int position = pickerCurrency.getValue();
                imgFlag.setImageResource(flags.get(position));
            }
        });

        // Create listener for currency picker button
        btnCurrencySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get position from currency picker.
                int position = pickerCurrency.getValue();
                String currency = arrayCurrency[position];
                Log.d("DialogCurrencyPicker","Currency:" + currency);
                // Return the selected value from the picker via the interface.
                // Create instance of interface listener.
                DialogCurrencyPicker.DialogListener listener = (DialogCurrencyPicker.DialogListener) getActivity();
                // Retrieve currency from dialog box and pass back to Activity.
                listener.onFinishDialogCurrencyPicker(currency);

                dialog.dismiss();
            }
        });

        return dialog;
    }
}
