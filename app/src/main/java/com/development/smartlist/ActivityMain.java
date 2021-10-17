package com.development.smartlist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.app.FragmentManager;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.development.custom_dialog_add_delete_10.*;
import com.development.custom_dialog_single_button.DialogSingleButton;
import com.example.custom_dialog_currency_picker_1_0.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Locale;

public class ActivityMain extends AppCompatActivity implements DialogAddDelete.DialogListener,
        DialogSingleButton.DialogListener, DialogCurrencyPicker.DialogListener,
        DialogCurrencyConfirm.DialogListener, DialogUser.DialogListener {

    private static final String CURRENCY_GBP = "GBP";
    private static final String CURRENCY_EURO = "EUR";
    private static final String CURRENCY_SWISS = "CHF";
    private static final String CURRENCY_USD = "USD";
    private static final String KEY_CURRENCY = "CURRENCY_CHANGE";
    private static final String DIALOG_TYPE_CURRENCY_CONFIRM = "currency_confirm";
    private static final String DIALOG_TYPE_CATEGORY = "category";
    private static final String DIALOG_TYPE_SHOP = "shop";
    private static final String DIALOG_TYPE_BRAND = "brand";
    private static final String DIALOG_ACTION_ADD = "add";
    private static final String DIALOG_ACTION_DELETE = "delete";
    private static final String DIALOG_TYPE_LIST_SAVE = "list_save";
    private static final String DIALOG_TYPE_LIST_LOAD = "list_load";
    private static final String DIALOG_TYPE_LIST_DELETE = "list_delete";
    private static final String DIALOG_TYPE_ITEM_DELETE = "item_delete";
    private static final String METHOD_ON_CHANGE_CURRENCY = "onChangeCurrency";
    private static final String METHOD_CLEAR_ITEM_FIELDS = "clearItemFields";
    private static final String METHOD_ON_ADD_ITEM = "onAddItem";
    private static final String METHOD_ON_DELETE_ITEM = "onDeleteItem";
    private static final String METHOD_ACTV_REMOVE_ITEM = "actvRemoveItem";
    private static final String METHOD_ON_LOAD_LIST = "onLoadList";
    private static final String METHOD_ON_CLEAR_LIST = "onClearShoppingList";
    private static final String METHOD_ON_CATEGORY_CHANGE = "onCategoryChange";
    private static final String METHOD_ON_SHOP_CHANGE = "onShopChange";
    private static final String METHOD_ON_BRAND_CHANGE = "onBrandChange";
    private static final String METHOD_GET_ITEM_INFO = "geIemInfo";
    private static final String METHOD_ON_BARCODE_SCAN = "onBarcodeScan";
    private static final String METHOD_SETUP_SCANNED_ITEM = "setupScannedItem";
    private static final String METHOD_BLANK_SCANNED_ITEM = "blankScannedItem";

    private DrawerLayout drawerLayout;
    private DBAdapter dbAdapter;
    private ImageView imgDeleteIcon;
    private ImageView imgAddIcon;
    private ImageView imgMenuIcon;
    private IntentIntegrator intentIntegrator;

    private VerifyBackupPermissions verifyBackupPermissions;
    private ClientValidateUser clientValidateUser;
    private ClientBackup clientBackup;
    private ClientBackupXML clientBackupXML;
    private ClientRestore clientRestore;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment selectedFragment;

            switch (item.getItemId()) {
                case R.id.navigation_create_edit_list:
                    selectedFragment = FragmentCreateEditList.newInstance();
                    transaction.replace(R.id.frame_layout, selectedFragment, "fragmentCreateEditList");
                    imgAddIcon.setEnabled(true);
                    imgDeleteIcon.setEnabled(true);
                    imgMenuIcon.setEnabled(true);
                    break;

                case R.id.navigation_view_list:
                    selectedFragment = FragmentViewList.newInstance();
                    transaction.replace(R.id.frame_layout, selectedFragment, "fragmentViewList");
                    imgAddIcon.setEnabled(false);
                    imgDeleteIcon.setEnabled(true);
                    imgMenuIcon.setEnabled(true);
                    break;

                case R.id.navigation_compare_list:
                    selectedFragment = FragmentCompareList.newInstance();
                    transaction.replace(R.id.frame_layout, selectedFragment, "fragmentCompareList");
                    break;
            }

            //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //transaction.replace(R.id.frame_layout, selectedFragment, "fragment");
            transaction.commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Calling and setting up Application class (see application tag in AndroidManifest.xml)
        final GlobalClass globalObject = (GlobalClass) getApplicationContext();

        // Initialize variables, adapters and class instances
        dbAdapter = new DBAdapter(this);
        verifyBackupPermissions = new VerifyBackupPermissions(this);
        clientValidateUser = new ClientValidateUser();
        clientBackupXML = new ClientBackupXML();
        clientBackup = new ClientBackup();
        clientRestore = new ClientRestore();

        // Display and setup the custom actionbar.
        Toolbar toolBar = findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        imgAddIcon = findViewById(R.id.iconAdd);
        imgDeleteIcon = findViewById(R.id.iconDelete);
        imgMenuIcon = findViewById(R.id.iconMenu);

        // Setup the fragment navigation panel
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, FragmentCreateEditList.newInstance());
        transaction.commit();

        // Setup the barcode scanner for adding items to the list by scanning
        intentIntegrator = new IntentIntegrator(ActivityMain.this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(ActivityScanOrientationPortrait.class);

        // Set the Locale value
        setLocale();

        // Retrieve the user name from the database if there is one.
        dbAdapter.open();
        globalObject.setUserName(dbAdapter.getUserName());
        dbAdapter.close();

        // Setup the drawer navigation panel.
        drawerLayout = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);
        final TextView txtNavHeaderUser = headerView.findViewById(R.id.navHeaderUser);

        // Create listener for ActionBar add button.
        imgAddIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the onAddItem method in the FragmentCreateEditList class to add the
                // item to the global item array list. Also add item to the database if it does not
                // already exist (used to populate the item AutoCompleteTextView).
                callFragmentMethod(METHOD_ON_ADD_ITEM, null);
                // Clear the item AutoCompleteTextView and quantity/unit EditText ready for the next item.
                callFragmentMethod(METHOD_CLEAR_ITEM_FIELDS, null);
                // If the add items to list by barcode flag is set re-initiate the barcode scanner.
                if (globalObject.getBarcodeScannerFlag()) {
                    intentIntegrator.initiateScan();
                }
            }
        });
        // Create listener for ActionBar delete button.
        imgDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment displayedFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                if (displayedFragment instanceof FragmentCreateEditList) {
                    // Current fragment displayed is the CreateEditList fragment, get the item and
                    // attempt to delete it by calling the appropriate method in the CreateEditList
                    // fragment.
                    ((FragmentCreateEditList) displayedFragment).onDeleteItem(null); // Cast displayedFragment to FragmentCreateEditList
                    // to allow us to call method in that fragment class.
                }
                else if (displayedFragment instanceof FragmentViewList){
                    ((FragmentViewList) displayedFragment).deleteShoppingListItems();
                }
            }
        });

        // Create listener for ActionBar menu button.
        imgMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Create listener for the menu drawer menu items.
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Create fragment manager for the dialog fragment.
                        FragmentManager fragmentManager = getFragmentManager();

                        // Create Bundle for passing arguments to the fragment.
                        Bundle bundle = new Bundle();
                        Bitmap bitmap;

                        switch (menuItem.getItemId()) {
                            case R.id.nav_user:
                                // Check for the necessary permissions to run the backup routine.
                                verifyBackupPermissions.verifyPermissions();
                                // Create a new instance of the user dialog fragment.
                                DialogUser dialogUser = new DialogUser();
                                // Display the dialog.
                                dialogUser.show(fragmentManager, "fragDialogUser");
                                break;

                            case R.id.nav_category:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_CATEGORY);
                                bundle.putStringArrayList("list", globalObject.getCategories());
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_category);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_add_delete_category));
                                // Create a new instance of the add/delete dialog.
                                DialogAddDelete dialogAddDeleteCategory = new DialogAddDelete();
                                // Display the dialog.
                                dialogAddDeleteCategory.setArguments(bundle);
                                dialogAddDeleteCategory.show(fragmentManager, "fragDialogAddDelete");
                                break;

                            case R.id.nav_shop:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_SHOP);
                                bundle.putStringArrayList("list", globalObject.getShops());
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_shopping_cart);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_add_delete_shop));
                                // Create a new instance of the add/delete dialog.
                                DialogAddDelete dialogAddDeleteShop = new DialogAddDelete();
                                // Display the dialog.
                                dialogAddDeleteShop.setArguments(bundle);
                                dialogAddDeleteShop.show(fragmentManager, "fragDialogAddDelete");
                                break;

                            case R.id.nav_brand:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_BRAND);
                                bundle.putStringArrayList("list", globalObject.getBrands());
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_brand);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_add_delete_brand));
                                // Create a new instance of the add/delete dialog.
                                DialogAddDelete dialogAddDeleteBrand = new DialogAddDelete();
                                // Display the dialog.
                                dialogAddDeleteBrand.setArguments(bundle);
                                dialogAddDeleteBrand.show(fragmentManager, "fragDialogAddDelete");
                                break;

                            case R.id.nav_item:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_ITEM_DELETE);
                                dbAdapter.open();
                                bundle.putStringArrayList("list", dbAdapter.getAllItems());
                                dbAdapter.close();
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_item);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_delete_item));
                                bundle.putString("button_text", getResources().getString(R.string.dialog_button_delete));
                                // Create a new instance of the single button dialog.
                                DialogSingleButton dialogDeleteItem = new DialogSingleButton();
                                // Display the dialog.
                                dialogDeleteItem.setArguments(bundle);
                                dialogDeleteItem.show(fragmentManager, "fragDialogSingleButton");
                                break;

                            case R.id.nav_currency:
                                // Create a new instance of the dialog fragment
                                DialogCurrencyPicker dialog = new DialogCurrencyPicker();
                                // Display the select currency dialog.
                                dialog.show(fragmentManager, "fragDialogCurrency");
                                break;

                            case R.id.nav_barcode:
                                // Using barcode scanner to add items to list so set flag to true.
                                globalObject.setBarcodeScannerFlag(true);
                                // Start the barcode scanner
                                intentIntegrator.initiateScan();
                                break;

                            case R.id.nav_load_list:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_LIST_LOAD);
                                dbAdapter.open();
                                bundle.putStringArrayList("list", dbAdapter.getListNames());
                                dbAdapter.close();
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_load);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_list_load));
                                bundle.putString("button_text", getResources().getString(R.string.button_load));
                                // Create a new instance of the single button dialog item dialog.
                                DialogSingleButton dialogLoadList = new DialogSingleButton();
                                dialogLoadList.setArguments(bundle);
                                // Display the dialog.
                                dialogLoadList.show(fragmentManager, "fragDialogSingleButton");
                                break;

                            case R.id.nav_save_list:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_LIST_SAVE);
                                dbAdapter.open();
                                bundle.putStringArrayList("list", dbAdapter.getListNames());
                                dbAdapter.close();
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_save);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_list_save));
                                bundle.putString("button_text", getResources().getString(R.string.button_save));
                                // Create a new instance of the single button dialog item dialog.
                                DialogSingleButton dialogSaveList = new DialogSingleButton();
                                bundle.putString("dialog_type", DIALOG_TYPE_LIST_SAVE);
                                dialogSaveList.setArguments(bundle);
                                // Display the dialog.
                                dialogSaveList.show(fragmentManager, "fragDialogSingleButton");
                                break;

                            case R.id.nav_delete_list:
                                // Create bundle to be passed to dialog fragment.
                                bundle.putString("dialog_type", DIALOG_TYPE_LIST_DELETE);
                                dbAdapter.open();
                                bundle.putStringArrayList("list", dbAdapter.getListNames());
                                dbAdapter.close();
                                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_delete);
                                bundle.putParcelable("icon", bitmap);
                                bundle.putString("title", getResources().getString(R.string.dialog_title_list_delete));
                                bundle.putString("button_text", getResources().getString(R.string.button_delete));
                                // Create a new instance of the single button dialog item dialog.
                                DialogSingleButton dialogDeleteList = new DialogSingleButton();
                                dialogDeleteList.setArguments(bundle);
                                // Display the dialog.
                                dialogDeleteList.show(fragmentManager, "fragDialogSingleButton");
                                break;

                            case R.id.nav_backup:
                                // Check for the necessary permissions to run the backup routine.
                                verifyBackupPermissions.verifyPermissions();
                                // Create the backup XML file.
                                Log.d("nav_backup","Call clientBackupXML");
                                String fileName = clientBackupXML.createBackupXML();
                                // Send the backup xml file to the server.
                                if (fileName != null) {
                                    clientBackup.backUpData(fileName);
                                }
                                break;

                            case R.id.nav_restore:
                                // Check for the necessary permissions to run the data restore routine.
                                verifyBackupPermissions.verifyPermissions();
                                // Launch the data restore routine.
                                clientRestore.restoreData();
                                break;
                        }
                        // set item as selected to persist highlight
                        //menuItem.setChecked(true);
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // Get an instance of the current displayed fragment.
                final Fragment displayedFragment = getSupportFragmentManager().
                        findFragmentById(R.id.frame_layout);

                // Set the username text in the header.
                GlobalClass globalObject = (GlobalClass) getApplicationContext();
                txtNavHeaderUser.setText(globalObject.getUserName());

                // Create instances of each MenuItem to be enabled/disabled.
                Menu menuNav = navigationView.getMenu();
                MenuItem navCategory = menuNav.findItem(R.id.nav_category);
                MenuItem navShop = menuNav.findItem(R.id.nav_shop);
                MenuItem navBrand = menuNav.findItem(R.id.nav_brand);
                MenuItem navItem = menuNav.findItem(R.id.nav_item);
                MenuItem navLoadList = menuNav.findItem(R.id.nav_load_list);
                MenuItem navSaveList = menuNav.findItem(R.id.nav_save_list);
                MenuItem navDeleteList = menuNav.findItem(R.id.nav_delete_list);
                MenuItem navBarcode = menuNav.findItem(R.id.nav_barcode);

                // Depending on current fragment enable/disable menu items.
                if (displayedFragment instanceof FragmentCreateEditList) {
                    navCategory.setEnabled(true);
                    navShop.setEnabled(true);
                    navBrand.setEnabled(true);
                    navItem.setEnabled(true);
                    navLoadList.setEnabled(false);
                    navSaveList.setEnabled(false);
                    navDeleteList.setEnabled(false);
                    navBarcode.setEnabled(true);
                }
                else if (displayedFragment instanceof  FragmentViewList) {
                    navCategory.setEnabled(false);
                    navShop.setEnabled(false);
                    navBrand.setEnabled(false);
                    navItem.setEnabled(false);
                    navLoadList.setEnabled(true);
                    navSaveList.setEnabled(true);
                    navDeleteList.setEnabled(true);
                    navBarcode.setEnabled(false);
                }
                else {
                    navCategory.setEnabled(false);
                    navShop.setEnabled(false);
                    navBrand.setEnabled(false);
                    navItem.setEnabled(false);
                    navLoadList.setEnabled(false);
                    navSaveList.setEnabled(false);
                    navDeleteList.setEnabled(false);
                    navBarcode.setEnabled(false);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // Setup the barcode scanner for adding items to list by scanning.
        intentIntegrator = new IntentIntegrator(ActivityMain.this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(ActivityScanOrientationPortrait.class);

        // Retrieve the list of categories from the database and populate the global categoriesList.
        dbAdapter.open();
        globalObject.setCategories(dbAdapter.getAllCategories());
        dbAdapter.close();
        // Sort the categories according to alphabetical order.
        globalObject.orderCategories();
        // Create global spinner adapter for the category spinner.
        globalObject.createCategorySpinnerAdapter();

        // Retrieve the list of shops from the database and populate the global shopsList.
        dbAdapter.open();
        globalObject.setShops(dbAdapter.getAllShops());
        dbAdapter.close();
        // Sort the categories according to alphabetical order.
        globalObject.orderShops();
        // Create global spinner adapter for the category spinner.
        globalObject.createShopSpinnerAdapter();

        // Retrieve the list of brands from the database and populate the global brandsList.
        dbAdapter.open();
        globalObject.setBrands(dbAdapter.getAllBrands());
        dbAdapter.close();
        // Sort the categories according to alphabetical order.
        globalObject.orderBrands();
        // Create global spinner adapter for the category spinner.
        globalObject.createBrandSpinnerAdapter();

    }

    /*
     * Get the currently selected locale from the database and set the app's locale
     */
    void setLocale() {
        // Calling the global application class
        GlobalClass globalObject = (GlobalClass) getApplicationContext();

        // Set the currently stored Locale value
        dbAdapter.open();
        String currency = dbAdapter.getCurrency();
        dbAdapter.close();
        globalObject.setCurrency(currency);
        switch (currency) {
            case CURRENCY_GBP:
                Locale.setDefault(Locale.UK);
                break;
            case CURRENCY_EURO:
                Locale.setDefault(new Locale("en", "FR"));
                break;
            case CURRENCY_SWISS:
                Locale.setDefault(new Locale("de", "CH"));
                break;
            case CURRENCY_USD:
                Locale.setDefault(Locale.US);
                break;
        }
    }

    /*
     * Take data passed from DialogAddDelete and add/delete a category/shop/brand.
     */
    public void onFinishDialogAddDelete(String name, String dialog_type, String dialog_action) {

        switch (dialog_type) {
            case DIALOG_TYPE_CATEGORY:
                if (dialog_action.equals(DIALOG_ACTION_ADD)) {
                    // Add the new category to the database.
                    // Check category does not already exist in database.
                    dbAdapter.open();
                    if (!dbAdapter.categoryExists(name)) {
                        if (dbAdapter.insertCategory(name) > 0) {
                            Toast.makeText(this, "Category inserted!", Toast.LENGTH_SHORT).show();

                            // Reset the current category id as a new category has been added. Set the
                            // category spinner to the new category.
                            Object[] obj = new Object[] { name, 1 };
                            callFragmentMethod(METHOD_ON_CATEGORY_CHANGE, obj);
                        } else {
                            Toast.makeText(this, "Unable to create category", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Category already exists!", Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                } else if (dialog_action.equals(DIALOG_ACTION_DELETE)) {
                    // Delete the category from the database.
                    // Check category exists in database
                    dbAdapter.open();
                    if (dbAdapter.deleteCategory(name) > 0) {
                        Toast.makeText(this, "Category deleted.", Toast.LENGTH_SHORT).show();

                        // Check if category id needs to be updated.
                        Object[] obj = new Object[] { name, -1 };
                        callFragmentMethod(METHOD_ON_CATEGORY_CHANGE, obj);
                    } else {
                        Toast.makeText(this, "Category not deleted!",
                                Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                }
                break;

            case DIALOG_TYPE_SHOP:
                if (dialog_action.equals(DIALOG_ACTION_ADD)) {
                    // Add the shop to the database
                    dbAdapter.open();

                    if (dbAdapter.insertShop(name) > 0) {
                        Toast.makeText(this, "Shop added.", Toast.LENGTH_SHORT).show();
                        // Reset the current brand id as a new brand has been added. Set the
                        // brand spinner to the new brand.
                        Object[] obj = new Object[] { name, 1 };
                        callFragmentMethod(METHOD_ON_SHOP_CHANGE, obj);
                    }
                    else {
                        Toast.makeText(this, "Shop not added!", Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                }
                else if (dialog_action.equals(DIALOG_ACTION_DELETE)) {
                    dbAdapter.open();
                    if (dbAdapter.deleteShop(name) > 0) {
                        Toast.makeText(this, "Shop removed.", Toast.LENGTH_SHORT).show();
                        // Reset the brand id as a brand has been deleted. Displayed brand may
                        // not match global brandId.
                        Object[] obj = new Object[] { name, -1 };
                        callFragmentMethod(METHOD_ON_SHOP_CHANGE, obj);
                    } else {
                        Toast.makeText(this, "Shop not deleted", Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                }
                break;

            case DIALOG_TYPE_BRAND:
                // Add the new brand to the database.
                if (dialog_action.equals(DIALOG_ACTION_ADD)) {
                    dbAdapter.open();

                    if (dbAdapter.insertBrand(name) > 0) {
                        Toast.makeText(this, "Brand added.", Toast.LENGTH_SHORT).show();
                        // Reset the current brand id as a new brand has been added. Set the
                        // brand spinner to the new brand.
                        Object[] obj = new Object[] { name, 1 };
                        callFragmentMethod(METHOD_ON_BRAND_CHANGE, obj);
                    }
                    else {
                        Toast.makeText(this, "Brand not added!", Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                }
                else if (dialog_action.equals(DIALOG_ACTION_DELETE)) {
                    dbAdapter.open();

                    if (dbAdapter.deleteBrand(name) > 0) {
                        Toast.makeText(this, "Brand removed.", Toast.LENGTH_SHORT).show();
                        // Reset the brand id as a brand has been deleted. Displayed brand may
                        // not match global brandId.
                        Object[] obj = new Object[] { name, -1 };
                        callFragmentMethod(METHOD_ON_BRAND_CHANGE, obj);
                    } else {
                        Toast.makeText(this, "Brand not deleted", Toast.LENGTH_SHORT).show();
                    }
                    dbAdapter.close();
                }
                break;

            default:
                break;
        }
    }

    public void onFinishDialogSingleButton(String name, String dialog_type) {
        // Calling the global application class
        GlobalClass globalObject = (GlobalClass) getApplicationContext();

        Object[] obj;

        // Delete the item from the database.
        switch (dialog_type) {
            case DIALOG_TYPE_ITEM_DELETE:
                dbAdapter.open();
                if (dbAdapter.deleteItem(name) > 0) {
                    Toast.makeText(this, "Item deleted from database.", Toast.LENGTH_SHORT).show();
                }
                dbAdapter.close();
                // Delete the item from the shopping list if it exists in the list
                obj = new Object[] { name };
                callFragmentMethod(METHOD_ON_DELETE_ITEM, obj);
                // Remove the item from the item AutoCompleteTextView if necessary
                callFragmentMethod(METHOD_ACTV_REMOVE_ITEM, obj);
                break;
            case DIALOG_TYPE_LIST_SAVE:
                dbAdapter.open();
                // Create a new List of items in the database.
                if (dbAdapter.saveList(name, globalObject.getItemArrayList())) {
                    // Set the global list id
                    globalObject.setGlobalListId(dbAdapter.getListId(name));
                    Toast.makeText(this, "Shopping List Saved", Toast.LENGTH_SHORT).show();
                }
                dbAdapter.close();
                break;
            case DIALOG_TYPE_LIST_LOAD:
                dbAdapter.open();
                // Get the list id for the returned list name.
                int listId = dbAdapter.getListId(name);
                dbAdapter.close();
                // Call the load list method in the view list fragment.
                obj = new Object[] { listId };
                callFragmentMethod(METHOD_ON_LOAD_LIST, obj);
                break;
            case DIALOG_TYPE_LIST_DELETE:
                // Delete list from the database
                Log.d("onFinishSingleButton","listName:" + name);
                dbAdapter.open();
                int deleteListId = dbAdapter.getListId(name);
                dbAdapter.deleteList(name);
                Log.d("onFinishSingleButton","listId:" + deleteListId);
                // Check if the list being deleted is equal to the current (global) list id.
                dbAdapter.close();
                if (deleteListId == globalObject.getGlobalListId()) {
                    // If the list being deleted is equal to the current (global) list id then the list must
                    // also be cleared from the list view. No list is therefore currently loaded, global list id
                    // to be reset to zero.
                    globalObject.setGlobalListId(0);
                    // call the onClearList method
                    callFragmentMethod(METHOD_ON_CLEAR_LIST, null);
                }
                break;
        }
    }

    /*
     * Receives the new currency setting from Dialog Currency Picker and sets the new Locale.
     */
    public void onFinishDialogCurrencyPicker(String currency) {

        // Calling the global application class
        GlobalClass globalObject = (GlobalClass) getApplicationContext();

        dbAdapter.open();
        if (!currency.equals(globalObject.getCurrency())) {
            DialogCurrencyConfirm dialogCurrencyChange = new DialogCurrencyConfirm();
            // Create bundle for passing to dialog.
            Bundle bundle = new Bundle();
            bundle.putString(KEY_CURRENCY, currency);
            dialogCurrencyChange.setArguments(bundle);
            // Create fragment manager for the dialog fragment.
            FragmentManager fragmentManager = getFragmentManager();
            dialogCurrencyChange.show(fragmentManager, DIALOG_TYPE_CURRENCY_CONFIRM);
        }
        dbAdapter.close();
    }

    /*
     * Dialog displays a confirmation message on change of currency.
     */
    public void onFinishDialogCurrencyConfirm(String currency) {

        // Calling the global application class
        final GlobalClass globalObject = (GlobalClass)getApplicationContext();

        // Locale/currency is to be changed. Update global locale, set locale in the database
        // and delete all current item info.
        globalObject.setCurrency(currency);
        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        switch (currency) {
            case CURRENCY_GBP:
                Locale.setDefault(Locale.UK);
                dbAdapter.setCurrency(CURRENCY_GBP);
                break;
            case CURRENCY_EURO:
                Locale.setDefault(new Locale("en", "FR"));
                dbAdapter.setCurrency(CURRENCY_EURO);
                break;
            case CURRENCY_SWISS:
                Locale.setDefault(new Locale("de", "CH"));
                dbAdapter.setCurrency(CURRENCY_SWISS);
                break;
            case CURRENCY_USD:
                Locale.setDefault(Locale.US);
                dbAdapter.setCurrency(CURRENCY_USD);
                break;
        }
        dbAdapter.close();
        // Call the changeCurrency method in the FragmentCreateEditList fragment to update the
        // the currency symbol in the Price EditText field.
        callFragmentMethod(METHOD_ON_CHANGE_CURRENCY, null);
        // Call the clearItemFields method of the FragmentCreateEditList fragment to clear all
        // item fields
        callFragmentMethod(METHOD_CLEAR_ITEM_FIELDS, null);
    }

    /*
     * Take data passed from DialogUser and create/edit a user.
     */
    public void onFinishDialogUser(int userId, String userName, String userUpdateType) {

        // Take the username and pass to the server for validating.
        clientValidateUser.validateUser(userId, userName, userUpdateType);
    }

    /*
     * Called on scan of a barcode, allows us to process the returned barcode number.
     */
    @Override
    @SuppressWarnings({"ConstantConditions"})
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        final GlobalClass globalObject = (GlobalClass) getApplicationContext();

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == -1) {   // if a barcode has been scanned.

            // Get the barcode.
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            String barcode = scanResult.getContents();

            if (globalObject.getBarcodeScannerFlag()) {
                // Adding items to the shopping list by scanning barcodes.
                // Determine if there is an item for the scanned code.
                dbAdapter.open();
                Object[] item = dbAdapter.getItemFromBarcode(barcode);
                dbAdapter.close();

                if (item != null) {
                    // Call routine in FragmentCreateEditList to set the scanned item. Item already exists in database
                    callFragmentMethod(METHOD_SETUP_SCANNED_ITEM, item);
                }
                else { // No item stored for scanned barcode, setup blank item edit fragment for barcode.
                    item = new Object[]{barcode};
                    callFragmentMethod(METHOD_BLANK_SCANNED_ITEM, item);
                }

            }
            else {
                // Process the barcode for the current item.
                Fragment displayedFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                ((FragmentCreateEditList) displayedFragment).onBarcodeScan(barcode);
            }
        }
    }

    /*
     * Method for calling a particular method in another fragment class.
     */
    public void callFragmentMethod(String method, Object[] args) {
        Fragment displayedFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

        if (displayedFragment instanceof FragmentCreateEditList) {
            switch (method) {
                case METHOD_ON_CHANGE_CURRENCY:
                    ((FragmentCreateEditList)displayedFragment).onChangeCurrency();
                    break;
                case METHOD_CLEAR_ITEM_FIELDS:
                    ((FragmentCreateEditList)displayedFragment).clearItemFields();
                    break;
                case METHOD_ON_ADD_ITEM:
                    ((FragmentCreateEditList)displayedFragment).onAddItem();
                    break;
                case METHOD_ON_DELETE_ITEM:
                    String item = args[0].toString();
                    ((FragmentCreateEditList)displayedFragment).onDeleteItem(item);
                    break;
                case METHOD_ACTV_REMOVE_ITEM:
                    String actvItem = args[0].toString();
                    ((FragmentCreateEditList)displayedFragment).actvRemoveItem(actvItem);
                    break;
                case METHOD_ON_CATEGORY_CHANGE:
                    String category = args[0].toString();
                    int action = (Integer)args[1];
                    ((FragmentCreateEditList)displayedFragment).onCategoryChange(category, action);
                    break;
                case METHOD_ON_SHOP_CHANGE:
                    String shop = args[0].toString();
                    int action1 = (Integer)args[1];
                    ((FragmentCreateEditList)displayedFragment).onShopChange(shop, action1);
                    break;
                case METHOD_ON_BRAND_CHANGE:
                    String brand = args[0].toString();
                    int action2 = (Integer)args[1];
                    ((FragmentCreateEditList)displayedFragment).onBrandChange(brand, action2);
                    break;
                case METHOD_SETUP_SCANNED_ITEM:
                    int itemId = (Integer)args[0];
                    int catId = (Integer)args[1];
                    String itemName = args[2].toString();
                    String barcode1 = args[3].toString();
                    ((FragmentCreateEditList)displayedFragment).setupScannedItem(itemId, catId,
                            itemName, barcode1);
                    break;
                case METHOD_BLANK_SCANNED_ITEM:
                    String barcode3 = args[0].toString();
                    ((FragmentCreateEditList)displayedFragment).blankScannedItem(barcode3);
                    break;
                case METHOD_ON_BARCODE_SCAN:
                    String barcode2 = args[0].toString();
                    ((FragmentCreateEditList)displayedFragment).onBarcodeScan(barcode2);
                    break;
            }
        }
        else if (displayedFragment instanceof FragmentViewList) {
            switch (method) {
                case METHOD_ON_LOAD_LIST:
                    int listId = (Integer)args[0];
                    ((FragmentViewList)displayedFragment).onLoadList(listId);
                    break;
                case METHOD_ON_CLEAR_LIST:
                    ((FragmentViewList)displayedFragment).onClearList();
            }
        }
    }
}
