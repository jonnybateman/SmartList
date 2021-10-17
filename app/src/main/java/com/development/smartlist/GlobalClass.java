package com.development.smartlist;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.development.custom_spinner_adapter.SpinnerAdapter;

public class GlobalClass extends Application{

    /*
     * Create a global application variable to hold the app's context.
     * Allows the context to be used in non-activity classes.
     */
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }

    /*
     * Declarations and methods relating to the locale value.
     */
    private String currency;

    // Set the global locale.
    public void setCurrency(String location) { this.currency = location; }

    // Get the global locale/currency.
    public String getCurrency() { return currency; }

    /*
     * Declarations and methods relating to the user.
     */
    private String userName;

    public void setUserName(String userName) { this.userName = userName; }

    public String getUserName() { return userName; }

    /*
     * Declarations and methods for items in the list.
     */

    private ArrayList<Item> itemList = new ArrayList<>();

    // Get the array list of item objects.
    public ArrayList<Item> getItemArrayList() {
        return this.itemList;
    }

    // Add an item object to the global item array list.
    public void addItemObject(int itemId, String itemName, int catId, String catName, String quantity,
                              Boolean divider, Boolean checked, Boolean selected, int position) {
        itemList.add(new Item(itemId, itemName, catId, catName, quantity, divider, checked,
                selected, position));
    }

    // Remove item from global item list by item name.
    public boolean deleteItemObject(String itemName) {
        boolean result = false;

        for (int i=0; i<itemList.size(); i++) {
            if (itemList.get(i).getItemName().equals(itemName)) {
                itemList.remove(i);
                result = true;
            }
        }
        return result;
    }

    // Populate the global item object array list.
    public void populateItemArrayList(ArrayList<Item> itemList) {

        for (int pos=0; pos<itemList.size(); pos++) {
            addItemObject(itemList.get(pos).getItemId()
                    ,itemList.get(pos).getItemName()
                    ,itemList.get(pos).getCatId()
                    ,itemList.get(pos).getCatName()
                    ,itemList.get(pos).getQuantity()
                    ,false
                    ,itemList.get(pos).getItemChecked()
                    ,false
                    ,pos);
        }
    }

    // Whenever the global item list has been sorted the position of each item in the array is stored
    // in that item object. Used to aid deletion of item objects from the global item list.
    public void setItemPositions() {
        for (int pos=0; pos<itemList.size(); pos++) {
            itemList.get(pos).setItemPosition(pos);
        }
    }

    // Remove an item object from the item array list.
    public void removeItem(int position) {
        itemList.remove(position);
    }

    /*
     * Declarations and methods for categories list.
     */

    private ArrayList<String> categoriesList = new ArrayList<>();
    private SpinnerAdapter categorySpinnerAdapter = null;

    public void addCategory(String category) {
        categoriesList.add(category);
    }

    ArrayList<String> getCategories() {
        return categoriesList;
    }

    void setCategories(ArrayList<String> categories) {
        categoriesList = categories;
    }

    void removeCategory(String category) {
        for (int i=0; i<categoriesList.size(); i++) {
            if (categoriesList.get(i).equals(category)) {
                categoriesList.remove(i);
                break;
            }
        }
    }

    void orderCategories() {
        Collections.sort(categoriesList, new Comparator<String>() {
            @Override
            public int compare(String cat1, String cat2) {
                return cat1.compareToIgnoreCase(cat2);
            }
        });
    }

    // Create an instance of SpinnerAdapter for the category spinner.
    void createCategorySpinnerAdapter() {
        categorySpinnerAdapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_item,
                categoriesList);
    }

    // Get the category spinner adapter.
    public SpinnerAdapter getCategorySpinnerAdapter() { return categorySpinnerAdapter; }

    /*
     * Declarations and methods for barcode scanner flag.
     */

    private boolean barcodeScannerFlag = false;

    // Change the global barcode scanner flag.
    public void setBarcodeScannerFlag(boolean barcodeScannerFlag) {
        this.barcodeScannerFlag = barcodeScannerFlag; }

    // Get the current status of the barcode scanner flag.
    public boolean getBarcodeScannerFlag() { return barcodeScannerFlag; }

    /*
     * Declarations and methods for shops list.
     */

    private ArrayList<String> shopsList = new ArrayList<>();
    private SpinnerAdapter shopSpinnerAdapter = null;

    public void addShop(String shop) {
        shopsList.add(shop);
    }

    ArrayList<String> getShops() {
        return shopsList;
    }

    void setShops(ArrayList<String> shops) {
        shopsList = shops;
    }

    void removeShop(String shop) {
        for (int i=0; i<shopsList.size(); i++) {
            if (shopsList.get(i).equals(shop)) {
                shopsList.remove(i);
                break;
            }
        }
    }

    void orderShops() {
        Collections.sort(shopsList, new Comparator<String>() {
            @Override
            public int compare(String shop1, String shop2) {
                return shop1.compareToIgnoreCase(shop2);
            }
        });
    }

    // Create an instance of SpinnerAdapter for the shop spinner.
    void createShopSpinnerAdapter() {
        shopSpinnerAdapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_item,
                shopsList);
    }

    // Get the shop spinner adapter.
    public SpinnerAdapter getShopSpinnerAdapter() { return shopSpinnerAdapter; }

    /*
     * Declarations and methods for brands list.
     */

    private ArrayList<String> brandsList = new ArrayList<>();
    private SpinnerAdapter brandSpinnerAdapter = null;

    public void addBrand(String brand) {
        brandsList.add(brand);
    }

    ArrayList<String> getBrands() {
        return brandsList;
    }

    void setBrands(ArrayList<String> brands) {
        brandsList = brands;
    }

    void removeBrand(String brand) {
        for (int i=0; i<brandsList.size(); i++) {
            if (brandsList.get(i).equals(brand)) {
                brandsList.remove(i);
                break;
            }
        }
    }

    void orderBrands() {
        Collections.sort(brandsList, new Comparator<String>() {
            @Override
            public int compare(String brand1, String brand2) {
                return brand1.compareToIgnoreCase(brand2);
            }
        });
    }

    // Create an instance of SpinnerAdapter for the brand spinner.
    void createBrandSpinnerAdapter() {
        brandSpinnerAdapter = new SpinnerAdapter(getApplicationContext(), R.layout.spinner_item,
                brandsList);
    }

    // Get the brand spinner adapter.
    public SpinnerAdapter getBrandSpinnerAdapter() { return brandSpinnerAdapter; }

    public void notifyBrandSpinnerAdapter() { brandSpinnerAdapter.notifyDataSetChanged(); }

    /*
     * Declarations and methods for item lists.
     */

    private int globalListId = 0;

    // Set the global list id
    public void setGlobalListId(int listId) { globalListId = listId; }

    // Return the global list id
    public int getGlobalListId() { return globalListId; }
}
