package com.development.smartlist;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FragmentViewList extends Fragment {

    private CustomItemListAdapter customItemListAdapter;
    private ArrayList<Item> itemObjectsAndCategoryDividers;
    private ListView listView;
    private TextView txtShop;
    private TextView txtItemQty;
    private TextView txtCost;

    static FragmentViewList newInstance() {
        FragmentViewList fragment = new FragmentViewList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_list, container, false);

        // Create a new array list of item objects to store the sorted items and category dividers.
        itemObjectsAndCategoryDividers = new ArrayList<>();

        // Create an instance of the custom item list array adapter to display the list of items.
        customItemListAdapter = new CustomItemListAdapter(getActivity(),
                itemObjectsAndCategoryDividers);

        // Initialise the list view widget.
        listView = view.findViewById(R.id.itemListView);
        listView.setAdapter(customItemListAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setClickable(true);

        // Initialise the shop and cost textview widgets.
        txtShop = view.findViewById(R.id.txtShop);
        txtItemQty = view.findViewById(R.id.txtItemQty);
        txtCost = view.findViewById(R.id.txtCost);

        // Setup a listener of the list view when a list item is clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!itemObjectsAndCategoryDividers.get(i).getDivider()) {
                    if (listView.isItemChecked(i)) {
                        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.listViewSelectorColor));
                        itemObjectsAndCategoryDividers.get(i).setItemSelected(true);
                    } else {
                        view.setBackgroundColor(Color.TRANSPARENT);
                        itemObjectsAndCategoryDividers.get(i).setItemSelected(false);
                    }
                }
            }
        });

        // Now populate the list view with items.
        populateShoppingList();

        return view;
    }

    void deleteShoppingListItems() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();
            List<Integer> selectedItemPositions = new ArrayList<>();

            if (itemObjectsAndCategoryDividers.size() > 0) {
                for (int i = itemObjectsAndCategoryDividers.size() - 1; i >= 0; i--) {
                    if (itemObjectsAndCategoryDividers.get(i).getItemSelected()) {
                        selectedItemPositions.add(itemObjectsAndCategoryDividers.get(i).getItemPosition());
                        itemObjectsAndCategoryDividers.remove(i);
                    }
                }

                // Remove the selected items from the global item list.
                for (int i = 0; i < selectedItemPositions.size(); i++) {
                    globalObject.removeItem(selectedItemPositions.get(i));
                }

                // Re-populate the list view
                populateShoppingList();
            }
        }
    }

    private void populateShoppingList() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();
            // Clear the itemObjectsAndCategoryDividers array of any elements.
            itemObjectsAndCategoryDividers.clear();
            customItemListAdapter.clear();

            if (globalObject.getItemArrayList().size() > 0) {
                // Sort the global items list into categories.
                Collections.sort(globalObject.getItemArrayList(), new Comparator<Item>() {
                    @Override
                    public int compare(Item item1, Item item2) {
                        return item1.getCatName().compareToIgnoreCase(item2.getCatName());
                    }
                });

                // Reset the global item index fields to the element's new positions.
                globalObject.setItemPositions();

                // Loop through the item objects array list and add a category divider whenever an item
                // with a new category is encountered.
                String category = " ";
                String currentCategory;
                for (int i = 0; i < globalObject.getItemArrayList().size(); i++) {
                    currentCategory = globalObject.getItemArrayList().get(i).getCatName();

                    // If it is a new category add a category separator to the itemObjectsAndCategoryDividers array.
                    if (!currentCategory.equals(category)) {
                        // Add a category divider since it is new category type.
                        itemObjectsAndCategoryDividers.add(new Item(0, null, 0, currentCategory, null,
                                true, false, false, -1));
                        category = currentCategory;
                    }
                    // Now add the item to the array list for displaying in the list view.
                    itemObjectsAndCategoryDividers.add(new Item(globalObject.getItemArrayList().get(i).getItemId(),
                            globalObject.getItemArrayList().get(i).getItemName(),
                            globalObject.getItemArrayList().get(i).getCatId(),
                            globalObject.getItemArrayList().get(i).getCatName(),
                            globalObject.getItemArrayList().get(i).getQuantity(),
                            false,
                            globalObject.getItemArrayList().get(i).getItemChecked(),
                            globalObject.getItemArrayList().get(i).getItemSelected(),
                            globalObject.getItemArrayList().get(i).getItemPosition()));
                }
            }

            // To display the items in the itemObjectsAndCategoryDividers array we need to notify the
            // associated adapter of the data change.
            customItemListAdapter.notifyDataSetChanged();

            // Create string of item Ids.
            StringBuilder sb = new StringBuilder();
            String itemIds = "";
            for (int i = 0; i < globalObject.getItemArrayList().size(); i++) {
                sb.append(globalObject.getItemArrayList().get(i).getItemId());
                sb.append(",");
            }

            if (sb.length() > 0) { // Remove the comma from the end of the string.
                sb.deleteCharAt(sb.length() - 1);
                itemIds = sb.toString();
            }

            // Obtain the total cost of the list of items.
            calculateListCost(itemIds);
        }
    }

    // Load the list of items into the global item array using the supplied list id.
    void onLoadList(int listId) {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Clear the global items array of items.
            globalObject.getItemArrayList().clear();

            // Get the items for the given listId and populate the global items array
            DBAdapter dbAdapter = new DBAdapter(getActivity());
            dbAdapter.open();
            globalObject.populateItemArrayList(dbAdapter.loadList(listId));
            dbAdapter.close();

            // Populate the view list fragment with the list items.
            populateShoppingList();

            // Set the current (global) list id
            globalObject.setGlobalListId(listId);
        }
    }

    // Remove all items from the global items array.
    void onClearList() {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();
            globalObject.getItemArrayList().clear();
            // Clear the fragment view list of items.
            populateShoppingList();
        }
    }

    /*
     * Calculate the total cost of items in the current list. Cost only reflects the items that have
     * had a price entered.
     */
    private void calculateListCost(String itemIds) {

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Create database adapter instance for accessing the database.
            DBAdapter dbAdapter = new DBAdapter(getActivity());

            // Get the item info data to calculate the total cost for each shop and find the cheapest
            // shop for the current item list.
            dbAdapter.open();
            ArrayList<Object[]> info = dbAdapter.getItemInfoForItemList(itemIds, globalObject.getCurrency());
            ArrayList<String> shopList = new ArrayList<>(dbAdapter.getShopsNamesForItems(itemIds,
                    globalObject.getCurrency()));
            dbAdapter.close();

            // Create map to store shop names and their associated running costs.
            Map<String, Float> shopTotal = new HashMap<>();
            // Create map to store shop item counts.
            Map<String, Integer> shopItemCount = new HashMap<>();
            // Populate the maps with shop names acting as keys.
            for (int i = 0; i < shopList.size(); i++) {
                shopTotal.put(shopList.get(i), 0F);
                shopItemCount.put(shopList.get(i), 0);
            }

            // If item info has been returned for the current item list.
            if (info != null) {

                int lastItemId = 0;
                int units = 1;

                // For each cursor record.
                for (int i = 0; i < info.size(); i++) {
                    // Get the current info object from the ArrayList
                    Object[] row = info.get(i);

                    String shopName = row[3].toString();
                    Float price = (Float) row[5];
                    int itemId = (int) row[0];

                    if (row[2].equals("U")) {
                        if (itemId != lastItemId) { // Determine the number of units for the new itemId.
                            // The price is per unit so multiply the price by the number of units
                            // Loop through the global item list to find the item and it's corresponding
                            // units value.
                            for (int k = 0; k < globalObject.getItemArrayList().size(); k++) {

                                if (globalObject.getItemArrayList().get(k).getItemId() == itemId) {
                                    units = Integer.parseInt(globalObject.getItemArrayList().get(k).getQuantity());
                                    break;
                                }
                            }
                        }
                        price = price * units;
                    }

                    // Add the current row's price to the running total of the corresponding shop.
                    shopTotal.put(shopName, shopTotal.get(shopName) + price);
                    // Add 1 to the shop's item count.
                    shopItemCount.put(shopName, shopItemCount.get(shopName) + 1);

                    lastItemId = itemId;
                }
            }

            // Determine the shop(s) with the highest item count.
            // First order the shopItemCount map.
            List<Map.Entry<String, Integer>> listShopItemCount = new LinkedList<>(shopItemCount.entrySet());
            Collections.sort(listShopItemCount, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                    return (m2.getValue()).compareTo(m1.getValue()); // descending order (max --> min)
                }
            });

            // Set the maximum shop item count, will be the first record in the list as the list has
            // been sorted in descending order.
            List<String> shops = new ArrayList<>();
            int max = 0;
            if (listShopItemCount.size() > 0) {
                max = listShopItemCount.get(0).getValue();
                // Determine which shop(s) have the maximum item shop count.
                for (int i = 0; i < listShopItemCount.size(); i++) {  // Loop thru list of shop item counts from max to min
                    if (listShopItemCount.get(i).getValue() == max) {
                        shops.add(i, listShopItemCount.get(i).getKey()); // add key (shop name) to the array
                    } else {
                        break;
                    }
                }
            }

            // Of the shops with the highest item count determine which of those shop(s) has the lowest
            // total cost.
            String cheapestShop = "";
            Float lowestPrice = 0F;
            if (shops.size() > 0) {
                lowestPrice = shopTotal.get(shops.get(0));
                cheapestShop = shops.get(0);
                for (String shop : shops) {
                    if (shopTotal.get(shop) < lowestPrice) {
                        lowestPrice = shopTotal.get(shop);
                        cheapestShop = shop;
                    }
                }
            }

            if (lowestPrice > 0) {
                txtShop.setText(cheapestShop);
                txtItemQty.setText(max + "/" + globalObject.getItemArrayList().size());
                txtCost.setText(NumberFormat.getCurrencyInstance().format(lowestPrice));
            } else {
                txtItemQty.setText("0/0");
                txtCost.setText(NumberFormat.getCurrencyInstance().format(0));
            }
        }
    }
}
