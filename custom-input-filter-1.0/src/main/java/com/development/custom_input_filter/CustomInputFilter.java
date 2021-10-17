package com.development.custom_input_filter;

import android.text.InputFilter;
import android.text.Spanned;

public class CustomInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {

        String filteredCharacters = "'\"";
        if (charSequence != null && filteredCharacters.contains("" + charSequence)) {
            return "";
        }

        return null;
    }
}
