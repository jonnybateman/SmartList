package com.development.smartlist;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.development.custom_input_filter.CustomInputFilter;
import com.development.icon_color_gradient.IconColorGradient;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.NumberFormat;

public class FragmentCreateEditList extends Fragment {

    private View view;
    private DBAdapter dbAdapter;
    private IconColorGradient iconColorGradient;
    private Spinner categorySpinner;
    private EditText edtBarcode;
    private ArrayAdapter<String> actvItemsAdapter;
    private AutoCompleteTextView actvItemName;
    private Switch switchQtyUnit;
    private EditText edtQuantity;
    private Spinner shopSpinner;
    private Spinner brandSpinner;
    private EditText edtPrice;

    private static final int COLOR_GRADIENT_1 = 0xFF66CFE6;
    private static final int COLOR_GRADIENT_2 = 0xFF0060B8;
    private static final String SWITCH_QUANTITY_UNIT_CHECKED = "U";
    private static final String SWITCH_QUANTITY_UNIT_UNCHECKED = "Q";

    private int catId = 0;
    private int itemId = 0;
    private int shopId = 0;
    private int brandId = 0;
    private String barcode;
    private String quantityUnitFlag;
    private boolean disableShopListener = false;
    private boolean disableBrandListener = false;
    private boolean disableQuantityUnitListener = false;

    public FragmentCreateEditList() {
        // Required empty public constructor
    }

    static FragmentCreateEditList newInstance() {
        FragmentCreateEditList fragment = new FragmentCreateEditList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize variables, adapters and class instances
        dbAdapter = new DBAdapter(getActivity());
        iconColorGradient = new IconColorGradient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the view inside the ActivityMain class using the fragment_create_edit_list layout.
        view = inflater.inflate(R.layout.fragment_create_edit_list, container, false);

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {

            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Assign the category spinner adapter to the category spinner.
            categorySpinner = view.findViewById(R.id.spnCategory);
            categorySpinner.setAdapter(globalObject.getCategorySpinnerAdapter());

            // Create listener for whenever a category spinner item is selected. This will allow us to populate
            // the items AutoCompleteTextView with items related to the selected category.
            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    dbAdapter.open();
                    // Retrieve the selected category's id.
                    catId = dbAdapter.getCategoryId(categorySpinner.getSelectedItem().toString());
                    // Get the items related to the selected category.
                    actvItemsAdapter.clear();
                    actvItemsAdapter.addAll(dbAdapter.getCategoryItems(catId));
                    dbAdapter.close();
                    // Clear the item data entry widgets only if we are not adding items to list via scanner.
                    if (!globalObject.getBarcodeScannerFlag()) {
                        clearItemFields();
                    }
                    // Set the focus ready for new item to be entered
                    actvItemName.requestFocus();
                    Log.d("CategorySpinnerListener", "catId:" + catId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            // Create widget variable for the items AutoCompleteTextView.
            actvItemName = view.findViewById(R.id.actvItem);
            actvItemName.setThreshold(1); // Set number of characters entered before auto complete suggestions are displayed.
            actvItemsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            actvItemName.setAdapter(actvItemsAdapter);

            actvItemName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("actvItemNameListen", "Item count:" + globalObject.getItemArrayList().size());
                    // Get the item id for the item currently displayed in the item AutoCompleteTextView.
                    dbAdapter.open();
                    itemId = dbAdapter.getItemId(actvItemName.getText().toString());
                    dbAdapter.close();
                    // Get any available item info for the current item.
                    if (itemId > 0) {
                        getItemInfo();
                    }

                    // Set the input type for quantity edit text depending on the Quantity/Unit flag.
                    if (quantityUnitFlag.equals("Q")) {
                        edtQuantity.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else {
                        edtQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                    Log.d("actvItemNameListen", "Item count:" + globalObject.getItemArrayList().size());
                }
            });

            actvItemName.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Get the item id for the item currently displayed in the item AutoCompleteTextView.
                        dbAdapter.open();
                        itemId = dbAdapter.getItemId(actvItemName.getText().toString());
                        dbAdapter.close();
                        // Get any available item info for the current item.
                        if (itemId > 0) {
                            getItemInfo();
                        }
                        return true;
                    } else if (keyEvent.getAction() == KeyEvent.KEYCODE_BACK) {
                        itemId = 0;
                    } else if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_DEL)) {
                        // Clear the item info fields
                        shopSpinner.setSelection(0);
                        brandSpinner.setSelection(0);
                        edtPrice.setText("0");
                        edtBarcode.setText("");
                        edtQuantity.setText("");
                        shopId = 0;
                        brandId = 0;
                        itemId = 0;
                        barcode = "";
                    }
                    return false;
                }
            });

            actvItemName.setFilters(new InputFilter[]{new CustomInputFilter()});

            // Create widget variable for entering the quantity/units
            edtQuantity = view.findViewById(R.id.edtQuantityUnit);

            // Create widget variable for quantity/unit switch.
            switchQtyUnit = view.findViewById(R.id.switchQtyUnit);
            // Setup the initial value of the switch
            switchQtyUnit.setChecked(false);
            quantityUnitFlag = "Q";

            // Switch listener for when the switch position is changed.
            switchQtyUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    // Setup the quantity/unit input type depending on switch position.
                    if (!switchQtyUnit.isChecked()) {
                        quantityUnitFlag = SWITCH_QUANTITY_UNIT_UNCHECKED;
                        // Set the input type depending on the Quantity/Unit flag.
                        edtQuantity.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else {
                        quantityUnitFlag = SWITCH_QUANTITY_UNIT_CHECKED;
                        // Set the input type depending on the Quantity/Unit flag.
                        edtQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }

                    // If an item is currently selected.
                    if (itemId > 0 && !disableQuantityUnitListener) {
                        // Get the stored qtyUnitFlag for the item selected.
                        dbAdapter.open();
                        String itemQtyUnitFlag = dbAdapter.getItemQtyUnitFlag(itemId);
                        dbAdapter.close();

                        if (!quantityUnitFlag.equals(itemQtyUnitFlag)) {
                            // The QtyUnitFlag has changed for the item, update the item.
                            dbAdapter.open();
                            if (dbAdapter.updateQtyUnitFlag(itemId, quantityUnitFlag)) {
                                Toast.makeText(view.getContext(), "Qty/Unit updated for current item.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // If the quantity/unit flag has been set to unit then remove the quantity values
                            // for each of the item's info records.
                            if (quantityUnitFlag.equals("U")) {
                                dbAdapter.updateItemQtys(itemId);
                            }

                            dbAdapter.close();

                            // Clear the quantity/unit edit text field.
                            edtQuantity.setText("");
                        }

                        // The quantity/unit type has changed for the current item remove the item from
                        // the current item list if necessary.
                        if (globalObject.getItemArrayList().size() > 0) {
                            // Loop through the item list and find the item if it is there.
                            for (Item item : globalObject.getItemArrayList()) {
                                if (item.getItemId() == itemId) {
                                    // Have found a match remove from the item list.
                                    globalObject.getItemArrayList().remove(item);
                                    break;
                                }
                            }
                        }
                    }
                    // Reset the switch's listener disable flag.
                    disableQuantityUnitListener = false;
                }
            });

            // Setup the barcode widgets
            edtBarcode = view.findViewById(R.id.edtBarcode);
            ImageView imgBarCode = view.findViewById(R.id.imgBarCode);
            Bitmap myBitmap = ((BitmapDrawable) imgBarCode.getDrawable()).getBitmap();
            Bitmap bitmapBarCode = iconColorGradient.addGradient(myBitmap, COLOR_GRADIENT_1, COLOR_GRADIENT_2);
            imgBarCode.setImageDrawable(new BitmapDrawable(getResources(), bitmapBarCode));
            // Prevent the barcode field from being manually edited.
            edtBarcode.setEnabled(false);
            // Create on click listener of barcode icon. Used to add barcode number to an item.
            imgBarCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (actvItemName.length() > 0) { // only if an item name has been selected.
                        // Not scanning to add images to list so reset scanning flag.
                        globalObject.setBarcodeScannerFlag(false);
                        // Setup scanner to obtain barcode for current item.
                        // Result from scanner is sent to onActivityResult() in ActivityMain.
                        IntentIntegrator integrator = new IntentIntegrator(getActivity());
                        integrator.setOrientationLocked(false);
                        integrator.setCaptureActivity(ActivityScanOrientationPortrait.class);
                        integrator.initiateScan();
                    }
                }
            });

            // Create a listener for the add item info icon
            ImageView imgAddItemInfoIcon = view.findViewById(R.id.iconAddInfo);
            imgAddItemInfoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Add the item info to the database
                    addItemInfo();
                }
            });

            // Create a listener for the delete item info icon.
            ImageView imgDeleteItemInfoIcon = view.findViewById(R.id.iconDeleteInfo);
            imgDeleteItemInfoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Delete the currently displayed item info from the database.
                    deleteItemInfo();
                }
            });

            // Assign the shop spinner adapter to the shop spinner.
            shopSpinner = view.findViewById(R.id.spnShop);
            shopSpinner.setAdapter(globalObject.getShopSpinnerAdapter());

            // Create a listener for when a shop is selected from the shop spinner.
            shopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // If a shop has been selected via the shop spinner and not set by the getItemInfo() method.
                    if (!disableShopListener) {
                        // Set the shop id for the selected shop.
                        dbAdapter.open();
                        shopId = dbAdapter.getShopId(shopSpinner.getSelectedItem().toString());
                        Log.d("shopSpinner", "shopId:" + shopId);
                        dbAdapter.close();
                        if (shopId > 0) {
                            dbAdapter.open();
                            // Get the item info, if any, for the current shop and item
                            Object[] itemShopInfo = dbAdapter.getItemShopInfo(itemId, shopId,
                                    globalObject.getCurrency());
                            dbAdapter.close();
                            // Set the brand spinner to the returned brand name if it exists.
                            if (itemShopInfo != null) {
                                brandSpinner.setSelection(globalObject.getBrandSpinnerAdapter().
                                        getPosition((String) itemShopInfo[2]));
                                brandId = ((Integer) itemShopInfo[1]);
                                edtPrice.setText(String.format("%.2f", (Float) itemShopInfo[0]));
                                edtQuantity.setText(itemShopInfo[3].toString());
                            } else {
                                // No item information stored in database for current item/shop combo.
                                brandSpinner.setSelection(0);
                                brandId = 0;
                                edtPrice.setText("0");
                            }
                        } else {
                            edtPrice.setText("0");
                            brandSpinner.setSelection(0);
                            brandId = 0;
                        }
                    } else {
                        // Re-enable the shop spinner listener logic.
                        disableShopListener = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            // Assign the brand spinner adapter to the brand spinner.
            brandSpinner = view.findViewById(R.id.spnBrand);
            brandSpinner.setAdapter(globalObject.getBrandSpinnerAdapter());

            // Create a listener for when a brand is selected from the brand spinner.
            brandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Set the brand id for the selected brand
                    if (!disableBrandListener) {
                        dbAdapter.open();
                        brandId = dbAdapter.getBrandId(brandSpinner.getSelectedItem().toString());
                        Log.d("brandSpinnerListener", "brandId:" + brandId);
                        dbAdapter.close();
                    } else {
                        disableBrandListener = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            // Format the Price EditText field with the currency symbol of the selected Locale. Text changed listener
            // needs to be added to ensure that if the user accidentally deletes the currency symbol it will be
            // automatically re-inserted.
            edtPrice = view.findViewById(R.id.edtPrice);
            edtPrice.setText(NumberFormat.getCurrencyInstance().format(0));
            edtPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        edtPrice.post(new Runnable() {
                            @Override
                            public void run() {
                                // If focus has changed to the Price EditText field then ensure cursor
                                // is set to the end of the contained text.
                                edtPrice.setSelection(edtPrice.getText().length());
                            }
                        });
                    }
                }
            });

            // Listener is responsible for formatting the text as numbers are inputed. The format is
            // "CUR###.##".
            edtPrice.addTextChangedListener(new TextWatcher() {
                String current = "";

                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!editable.toString().equals(current)) { // display the entered price with 2 decimal places.
                        edtPrice.removeTextChangedListener(this);
                        String replaceable = String.format("[%s,.'\\s]",
                                NumberFormat.getCurrencyInstance().getCurrency().getSymbol());
                        String cleanPrice = editable.toString().replaceAll(replaceable, "");
                        double parsed = Double.parseDouble(cleanPrice);
                        String formatted = NumberFormat.getCurrencyInstance().format(parsed / 100);
                        current = formatted;
                        edtPrice.setText(formatted);
                        edtPrice.setSelection(formatted.length());
                        edtPrice.addTextChangedListener(this);
                    }
                }
            });

            // Set up the fragment Clear button and associated listener.
            Button btnClearShoppingList = view.findViewById(R.id.btnClear);
            btnClearShoppingList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Clear the global item array list.
                    globalObject.getItemArrayList().clear();
                }
            });

            // Ensure all fields have been cleared and reset fragment variables.
            clearItemFields();
        }

        return view;
    }

    /*
     * Category has been added/deleted. Set the spinner selection accordingly.
     */
    void onCategoryChange(String actionCategory, int action) { // '-1' category removed; '1' category added.

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Get the current position of the category spinner before any changes are made.
            int position = categorySpinner.getSelectedItemPosition();
            Log.d("onCategoryChange", "position:" + position);

            ArrayAdapter adapter = globalObject.getCategorySpinnerAdapter();
            // Set the category spinner to that of the newly added category.
            if (action == 1) {
                globalObject.addCategory(actionCategory);
                globalObject.orderCategories();
                globalObject.getCategorySpinnerAdapter().notifyDataSetChanged();

                for (int i = 0; i < adapter.getCount(); i++) {

                    @SuppressWarnings("ConstantConditions") String category = adapter.getItem(i).toString();
                    if (category.equals(actionCategory)) {
                        categorySpinner.setSelection(i);

                        if (i == position) {
                            // Position in the spinner of the new category is the same as the position of
                            // the previously displayed category. Since positions are the same the spinner
                            // listener will not fire. Get category id of new category.
                            dbAdapter.open();
                            catId = dbAdapter.getCategoryId(actionCategory);

                            // Get the items related to the new category.
                            actvItemsAdapter.clear();
                            actvItemsAdapter.addAll(dbAdapter.getCategoryItems(catId));
                            dbAdapter.close();

                            Log.d("onCategoryChange", "catId:" + catId);
                        }

                        break;
                    }
                }
            } else if (action == -1) {
                // Get the name of the currently displayed category.
                String currentCategory = categorySpinner.getSelectedItem().toString();
                // Remove supplied category from global category list.
                globalObject.removeCategory(actionCategory);
                // Notify category spinner adapter of data set change.
                globalObject.getCategorySpinnerAdapter().notifyDataSetChanged();

                // Compare the category being deleted and the currently displayed category.
                // str1 > str2 returns +ve; str1 = str2 returns 0; str1 < str2 returns -ve.
                int result = actionCategory.compareTo(currentCategory);

                if (result == 0) {

                    if (position > 0) {
                        // Category being deleted is the same as the one that was displayed in spinner.
                        // New spinner position will be one position less.
                        categorySpinner.setSelection(position - 1);
                    } else if (position == 0) {
                        // Category being deleted is the same as one displayed in spinner, and, is also
                        // the first category in spinner. The subsequent category will now be displayed in position
                        // zero. Since position has not changed spinner listener will not fire, need to
                        // get the category id of the subsequent category if there is one.
                        if (globalObject.getCategories().size() > 0) {
                            dbAdapter.open();
                            catId = dbAdapter.getCategoryId(categorySpinner.getItemAtPosition(0).toString());

                            // Get the items related to the new category.
                            actvItemsAdapter.clear();
                            actvItemsAdapter.addAll(dbAdapter.getCategoryItems(catId));
                            dbAdapter.close();
                        } else {
                            catId = 0;
                        }
                        Log.d("onCategoryChange", "catId:" + catId);
                    }
                } else if (result < 0) {
                    // Category being deleted appears before displayed category so all categories appearing
                    // after deleted category will be shifted down 1 position resulting in a different
                    // category being displayed from the one that was originally displayed. Ensure the
                    // original displayed category is set in the spinner.
                    categorySpinner.setSelection(position - 1);
                }
                //else if (result > 0) {
                // Category being deleted appears after the currently displayed category so no action
                // required. Displayed category does not change and catId does not change.
            }
        }
        else {
            Toast.makeText(view.getContext(), "Context error, unable to change category!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Clear the item fields of data once the item has been added to the shopping list.
     */
    void clearItemFields() {

        actvItemName.setText("");
        edtQuantity.setText("");
        edtBarcode.setText("");
        shopSpinner.setSelection(0);
        brandSpinner.setSelection(0);
        edtPrice.setText("0");
        edtBarcode.setText("");

        itemId = 0;
        shopId = 0;
        brandId = 0;
        barcode = "";
    }

    /*
     * Add item to the shopping list by adding it to the global item array list. Also add it to the
     * database if that particular item does not already exist (used for the item AutoCompleteTextView.
     */
    void onAddItem() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Must have a category and an item in order to add to the shopping list.
            if (catId > 0 && actvItemName.getText().length() > 0) {

                Log.d("onAddItem","catId:" + catId);

                // Get the item name from the item AutoCompleteTextView.
                String itemName = actvItemName.getText().toString();

                Log.d("onAddItem","itemName:" + itemName);

                // Get the category name from the category spinner.
                String category = categorySpinner.getSelectedItem().toString();

                // Get the barcode if there is one.
                if (edtBarcode.length() > 0) {
                    barcode = edtBarcode.getText().toString();
                }

                Log.d("onAddItem","barcode:" + barcode + ".");

                //Insert item into the database if it does not already exist.
                dbAdapter.open();
                if (!dbAdapter.checkItemExists(itemName)) {

                    Log.d("onAddItem","Insert item to database");

                    // If we have successfully inserted the item then add to the item AutoCompleteTextView.
                    if (dbAdapter.insertItem(itemName, catId, quantityUnitFlag, barcode)) {
                        // Add the item to the items AutoCompleteTextView by updating it's array adapter.
                        actvItemsAdapter.add(itemName);
                        // Get the itemId for the current item.
                        itemId = dbAdapter.getItemId(itemName);

                        Toast.makeText(view.getContext(), "Item added: " + itemName, Toast.LENGTH_SHORT).show();
                    }
                }
                dbAdapter.close();

                // Set the quantity for the item being added to the list.
                String quantity = edtQuantity.getText().toString();
                if (quantity.length() == 0) {
                    quantity = "1";
                }

                // Add the item to the global list of items if it has not already been added
                int i = 0;
                boolean itemInList = false;
                while (i < globalObject.getItemArrayList().size()) {
                    if (globalObject.getItemArrayList().get(i).getItemId() == itemId) {
                        itemInList = true;
                        break;
                    }
                    i++;
                }

                // If a category has been selected and the item does not currently exist in the list then add
                // the item to the global list of items.
                if (!itemInList) {
                    int itemPosition = globalObject.getItemArrayList().size();
                    globalObject.addItemObject(itemId, itemName, catId, category,
                            quantity, false, false, false, itemPosition);
                }

                // If item info is present for the item add or update the info to the database.
                if (shopSpinner.getSelectedItemPosition() > 0) {
                    addItemInfo();
                }
            }
        }
        else {
            Toast.makeText(view.getContext(), "Unable to add item!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Add the item info to the database
     */
    private void addItemInfo() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {

            // Calling Application class (see application tag in AndroidManifest.xml);
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Set the price as a float data type.
            float price = Float.parseFloat(edtPrice.getText().toString().replaceAll("[\\s(\u20ac)CHF£$]+", ""));

            // Variable to store the quantity for the current item.
            String quantity = "";

            // Get the currently displayed barcode.
            //String displayedBarcode = edtBarcode.getText().toString();

            // Has the item been set in the database.
            if (itemId > 0) {
/*
                // Update the barcode for the current item if it has changed.
                if (!displayedBarcode.equals(barcode) && !displayedBarcode.equals("")) {
                    dbAdapter.open();

                    if (!dbAdapter.updateItemBarcode(itemId, displayedBarcode)) {
                        edtBarcode.setText("");
                        barcode = null;
                        Toast.makeText(view.getContext(), "Could not set barcode, barcode already exits",
                                Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                }
*/
                // Add/edit item info to database if the shop has been selected and price set.
                if (shopSpinner.getSelectedItemPosition() > 0 && price > 0) {
                    // Only insert/update the quantity of the item if the quantity_unit flag is 'Q'
                    if (quantityUnitFlag.equals("Q")) {
                        if (edtQuantity.getText().length() > 0) {
                            quantity = edtQuantity.getText().toString();
                        }
                    }

                    // Check the current item info status in the database.
                    dbAdapter.open();
                    int status = dbAdapter.itemInfoStatus(itemId, shopId, brandId, price,
                            globalObject.getCurrency(), quantity);
                    switch (status) {
                        case 0:
                            if (dbAdapter.insertItemInfo(itemId, price, shopId, brandId,
                                    globalObject.getCurrency(), quantity, quantityUnitFlag)) {
                                Toast.makeText(view.getContext(), "Item info added.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1:
                            if (dbAdapter.updateItemInfo(itemId, price, shopId, brandId, globalObject.getCurrency(),
                                    quantity)) {
                                Toast.makeText(view.getContext(), "Item info updated.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            break;
                    }
                    dbAdapter.close();

                } else {
                    Toast.makeText(view.getContext(), "Cannot save info, shop and price must be set!",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(view.getContext(), "Cannot save info, item has not been set!",
                        Toast.LENGTH_SHORT).show();
            }
            Log.d("addItemInfo", "Item Qty:" + globalObject.getItemArrayList().size());
        }
        else {
            Toast.makeText(view.getContext(), "Context error, unable to add item info!",
                    Toast.LENGTH_LONG).show();
        }

    }

    /*
     * Populate the item info fields for the current item.
     */
    private void getItemInfo() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Check to see if there is item info for the current item.
            dbAdapter.open();
            Object[] itemInfo = dbAdapter.getItemInfo(itemId, globalObject.getCurrency());
            dbAdapter.close();

            // If item info has been found for the current item.
            if (itemInfo != null) {

                // Set the quantity unit switch for the current item.
                quantityUnitFlag = (String) itemInfo[5];
                // Do not want all of quantityUnit switch listener logic to be executed at this time.
                disableQuantityUnitListener = true;

                if (quantityUnitFlag.equals("Q")) {
                    switchQtyUnit.setChecked(false);
                    // Set the quantity edit text field with the quantity.
                    edtQuantity.setText((String) itemInfo[6]);
                } else {
                    switchQtyUnit.setChecked(true);
                }

                if ((Integer) itemInfo[3] != shopId) {
                    // Get the id of the current shop selected.
                    shopId = ((Integer) itemInfo[3]);
                    // Listener only needs to be disabled if the shop has changed otherwise listener will fire.
                    disableShopListener = true;
                }

                // Get the position of the returned shop in the shop spinner.
                int positionShop = globalObject.getShopSpinnerAdapter().getPosition((String) itemInfo[0]);
                // Set the shop in the shop spinner to that of the item info.
                shopSpinner.setSelection(positionShop);

                if ((Integer) itemInfo[4] != brandId) {
                    // Set the brand id for the returned brand.
                    brandId = ((Integer) itemInfo[4]);
                    // Listener only needs to be disabled if the brand has changed otherwise listener will not fire.
                    disableBrandListener = true;
                }

                // Get the position of the returned brand in the brand spinner.
                int positionBrand = globalObject.getBrandSpinnerAdapter().getPosition((String) itemInfo[1]);
                // Set the brand in the brand spinner to that of the item info.
                brandSpinner.setSelection(positionBrand);

                // Set the barcode if one has been returned for the current item.
                barcode = ((String) itemInfo[7]);
                edtBarcode.setText(barcode);

                // Set the price edit text field to that of the item info.
                edtPrice.setText(String.format("%.2f", itemInfo[2])); // Ensures retrieved price will have 2 decimal places.
            }
        }
        else {
            Toast.makeText(view.getContext(), "Unable to get item info!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Delete the displayed item info for the current item.
     */
    private void deleteItemInfo() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();
            dbAdapter.open();
            // Delete the item info for the current item if it exists.
            if (itemId > 0) {
                if (dbAdapter.deleteItemInfo(itemId, shopId, globalObject.getCurrency())) {
                    Toast.makeText(view.getContext(), "Item info deleted.", Toast.LENGTH_SHORT).show();
                    // See if there is any other info for the current item.
                    getItemInfo();
                } else {
                    Toast.makeText(view.getContext(), "Item info not present", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            Toast.makeText(view.getContext(), "Unable to delete item info!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Remove the item from the shopping list.
     */
    void onDeleteItem(String itemName) {
        // itemName is supplied from the delete item dialog. When this method is called from the
        // delete icon in the action bar we use the text from the item AutoCompleteTextView in the
        // CreateEditList fragment for itemName.
        if (itemName == null) {
            itemName = actvItemName.getText().toString();
        }

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {

            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Delete item from shopping list.
            if (globalObject.deleteItemObject(itemName)) {
                Toast.makeText(view.getContext(), "Item removed from shopping list.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(view.getContext(), "Context error, unable to remove item!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * When an item is deleted from database remove the item from the item AutoCompleteTextView if necessary.
     */
    @SuppressWarnings({"ConstantConditions"})
    void actvRemoveItem(final String itemName) {

        for (int i = 0; i < actvItemsAdapter.getCount(); i++) {

            if (actvItemsAdapter.getItem(i).equals(itemName)) {
                actvItemsAdapter.remove(itemName);
                break;
            }
        }
    }

    /*
     * Shop has been added or deleted from shop spinner, set spinner and shop id accordingly.
     */
    void onShopChange(String actionShop, int action) { // '-1' shop removed; '1' shop added.

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {

            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Get the position and name of the current selected item in the shop spinner before any changes are made.
            int position = shopSpinner.getSelectedItemPosition();
            String currentShop = shopSpinner.getSelectedItem().toString();

            ArrayAdapter adapter = globalObject.getShopSpinnerAdapter();
            // Set the shop spinner to that of the newly added shop.
            if (action == 1) {
                globalObject.addShop(actionShop);
                globalObject.orderShops();
                globalObject.getShopSpinnerAdapter().notifyDataSetChanged();

                for (int i = 0; i < adapter.getCount(); i++) {

                    @SuppressWarnings("ConstantConditions") String shop = adapter.getItem(i).toString();
                    if (shop.equals(actionShop)) {
                        shopSpinner.setSelection(i);

                        if (i == position) {
                            // Position in the spinner of the new shop is the same as the position of
                            // the previously displayed shop. Since positions are the same the spinner
                            // listener will not fire. Get shop id of new brand.
                            dbAdapter.open();
                            shopId = dbAdapter.getShopId(actionShop);
                            dbAdapter.close();
                        }

                        break;
                    }
                }
            } else if (action == -1) {

                // Remove shop from global shop list.
                globalObject.removeShop(actionShop);
                // Notify shop spinner adapter of data set change.
                globalObject.getShopSpinnerAdapter().notifyDataSetChanged();

                // Compare the shop being deleted and the currently displayed shop.
                // str1 > str2 returns +ve; str1 = str2 returns 0; str1 < str2 returns -ve.
                int result = actionShop.compareTo(currentShop);

                if (position > 0) {
                    if (result == 0) {
                        // Shop being deleted is the same as the one that was displayed in spinner.
                        // New spinner position will be one position less.
                        shopSpinner.setSelection(position - 1);
                    } else if (result < 0) {
                        // Shop being deleted appears before displayed shop so all shops appearing
                        // after deleted shop will be shifted down 1 position resulting in a different
                        // shop being displayed from the one that was originally displayed. Ensure the
                        // original displayed shop is set in the spinner.
                        shopSpinner.setSelection(position - 1);
                    }
                    //else if (result > 0) {
                    // Shop being deleted appears after the currently displayed shop so no issue.
                    // Displayed shop does not change and shopId does not change.
                }
            }
        }
        else {
            Toast.makeText(view.getContext(), "Unable to update shop status!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Brand has been added or deleted from brand spinner, set spinner and brand id accordingly.
     */
    void onBrandChange(String actionBrand, int action) { // '-1' brand removed; '1' brand added.

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {

            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Get the position and name of the current selected item in the brand spinner before any changes are made.
            int position = brandSpinner.getSelectedItemPosition();
            String currentBrand = brandSpinner.getSelectedItem().toString();

            ArrayAdapter adapter = globalObject.getBrandSpinnerAdapter();
            // Set the brand spinner to that of the newly added brand.
            if (action == 1) {
                globalObject.addBrand(actionBrand);
                globalObject.orderBrands();
                globalObject.getBrandSpinnerAdapter().notifyDataSetChanged();

                for (int i = 0; i < adapter.getCount(); i++) {
                    @SuppressWarnings("ConstantConditions") String brand = adapter.getItem(i).toString();
                    if (brand.equals(actionBrand)) {
                        brandSpinner.setSelection(i);

                        if (i == position) {
                            // Position in the spinner of the new brand is the same as the position of
                            // the previously displayed brand. Since positions are the same the spinner
                            // listener will not fire. Get brand id of new brand.
                            dbAdapter.open();
                            brandId = dbAdapter.getBrandId(actionBrand);
                            dbAdapter.close();
                        }

                        break;
                    }
                }
            } else if (action == -1) {

                // Remove brand from global brand list.
                globalObject.removeBrand(actionBrand);
                // Notify brand spinner adapter of data set change.
                globalObject.getBrandSpinnerAdapter().notifyDataSetChanged();

                // Compare the brand being deleted and the currently displayed brand.
                // str1 > str2 returns +ve; str1 = str2 returns 0; str1 < str2 returns -ve.
                int result = actionBrand.compareTo(currentBrand);

                if (position > 0) {
                    if (result == 0) {
                        // Brand being deleted is the same as the one that was displayed in spinner.
                        // New spinner position will be one position less.
                        brandSpinner.setSelection(position - 1);
                    } else if (result < 0) {
                        // Brand being deleted appears before displayed brand so all brands appearing
                        // after deleted brand will be shifted down 1 position resulting in a different
                        // brand being displayed from the one that was originally displayed. Ensure the
                        // original displayed brand is set in the spinner.
                        brandSpinner.setSelection(position - 1);
                    }
                    //else if (result > 0) {
                    // Brand being deleted appears after the currently displayed brand so no issue.
                    // Displayed brand does not change and brandId does not change.
                }
            }
        }
        else {
            Toast.makeText(view.getContext(), "Unable to update brand status!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Change the currency symbol in the Price EditText as set by the Currency Dialog Picker.
     */
    void onChangeCurrency() {
        edtPrice.setText(NumberFormat.getCurrencyInstance().format(0));
    }

    /*
     * Called on scan of a barcode, allows us to process the returned barcode number.
     */
    void onBarcodeScan(String barcode) {

        // Set the barcode TextView with the scan result.
        edtBarcode.setText(barcode);

        // Only set the barcode if we currently have an item set.
        if (itemId > 0) {

            // If we currently have an item set then update that item's barcode value in the database.
            dbAdapter.open();
            if (dbAdapter.updateItemBarcode(itemId, barcode)) {
                this.barcode = barcode;
                Toast.makeText(getContext(), "Barcode updated for item.", Toast.LENGTH_SHORT).show();
            }
            else {
                edtBarcode.setText("");
                this.barcode = "";
                Toast.makeText(getContext(), "Could not set barcode, barcode already exists",
                        Toast.LENGTH_SHORT).show();
            }
            dbAdapter.close();
        }
    }

    /*
     * Adding items to list via barcode scanning. Setup item from scanned barcode if item exists in database.
     */
    void setupScannedItem(int itemId, int catId, String itemName, String barcode) {

        // Set the item id for the fragment.
        this.itemId = itemId;

        // Set the category for the item.
        // Get the category name.
        dbAdapter.open();
        String category = dbAdapter.getCategoryName(catId);
        dbAdapter.close();

        // Show category in the category spinner.
        for (int i=0; i<categorySpinner.getCount(); i++) {
            if (categorySpinner.getItemAtPosition(i).toString().equals(category)) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        // Set the item name.
        actvItemName.setText(itemName);

        // Set the barcode.
        edtBarcode.setText(barcode);

        // Now get item info for the item.
        getItemInfo();
    }

    /*
     * Adding items to ist via barcode scanning. Setup blank item fragment for editing with scanned barcode.
     */
    void blankScannedItem(String barcode) {

        // Set the barcode in the items edit fragment.
        edtBarcode.setText(barcode);

    }

}
