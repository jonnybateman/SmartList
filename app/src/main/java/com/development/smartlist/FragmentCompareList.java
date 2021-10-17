package com.development.smartlist;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentCompareList extends Fragment{

    private static final String TABLE_FIELD_FIXED_HEADER = "FIXED_HEADER";
    private static final String TABLE_FIELD_SCROLLABLE_HEADER = "SCROLLABLE_HEADER";
    private static final String TABLE_FIELD_FIXED_COLUMN = "FIXED_COLUMN";
    private static final String TABLE_FIELD_DATA = "DATA";
    private static final String TABLE_FIELD_FIXED_TOTALS = "FIXED_TOTALS";

    public static FragmentCompareList newInstance() {
        FragmentCompareList fragment = new FragmentCompareList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int numberCols;
        int dpToPixels;
        int columnWidth;
        String tableFieldType;
        int itemId;
        String itemName;
        String shopName;
        String brandName;
        String quantityUnit;
        int numberOfUnits;
        Float price;
        String lastItem = "";
        String itemsColumnName = "Items";

        TableRow headerRow = new TableRow(getContext());
        TableRow dataRow;
        TableRow totalsRow = new TableRow(getContext());

        // Following used to determine the height of text.
        Paint mPaint = new Paint();
        Rect textBounds = new Rect();

        DBAdapter dbAdapter = new DBAdapter(getActivity());
        SparseArray<Float> sparseArrayShopTotals = new SparseArray<>();
        View view = inflater.inflate(R.layout.fragment_compare_list, container, false);

        // Ensure Context is not null before proceeding.
        if (getContext() != null) {
            // Calling Application class (see application tag in AndroidManifest.xml)
            final GlobalClass globalObject = (GlobalClass) getContext().getApplicationContext();

            // Determine the number of pixels to dp.
            dpToPixels = (int) getResources().getDisplayMetrics().density;

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

            // Get the shop names for the header fields.
            dbAdapter.open();
            ArrayList<String> shops = dbAdapter.getShopsForTable(itemIds, globalObject.getCurrency());
            dbAdapter.close();
            if (shops.size() == 0) {
                shops.add(0, getResources().getString(R.string.table_compare_header_no_item_info));
            }

            // Initialise the sparse array for storing the shop totals.
            for (int i = 0; i < shops.size(); i++) {
                sparseArrayShopTotals.put(i, 0F);
            }

            // Set the number of columns for the table.
            numberCols = shops.size() + 1;  // +1 for the items fixed (horizontally) column.

            // Setup the width for each column
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            //columnWidth = (screenWidth - (2 * getResources().getDimensionPixelSize(
                    //R.dimen.table_compare_margin_left_right) * dpToPixels)) / numberCols;
            columnWidth = (screenWidth - (2 * getResources().getDimensionPixelSize(
                    R.dimen.table_compare_margin_left_right))) / numberCols;
            Log.d("FragmentCompareList", "ColumnWidth:" + columnWidth);
            int minColumnWidth = (screenWidth - 20 * dpToPixels) / 3;
            if (columnWidth < minColumnWidth) {
                columnWidth = minColumnWidth;
            }

            // Determine the height of the header row.
            int textSize = getResources().getDimensionPixelSize(R.dimen.table_compare_heading_text_size);
            String text = itemsColumnName;
            for (String shop : shops) { // Loop to find column with longest name.
                if (shop.length() > text.length()) {
                    text = shop;
                }
            }
            int paddingLeftRight = 2 * getResources().getDimensionPixelSize(R.dimen.table_padding);
            int numHeaderLines = numberOfLines(textSize, text, columnWidth, paddingLeftRight);
            int viewPaddingTopAndBottom = 2 * (getResources().getDimensionPixelSize(R.dimen.table_padding));
            mPaint.setTextSize(textSize);
            mPaint.getTextBounds(text, 0, text.length(), textBounds);
            int textHeight = textBounds.height(); // Height of a single line of text.
            int headerRowHeight = (numHeaderLines * textHeight) + viewPaddingTopAndBottom +
                    ((numHeaderLines -1) * 10); // Spacing between lines.

            // Populate the first header column of the table. Fixed vertically and horizontally.
            tableFieldType = TABLE_FIELD_FIXED_HEADER;
            TableLayout headerFirstColumn = view.findViewById(R.id.table_fixed_header);
            headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            headerRow.addView(makeTableRow(itemsColumnName, columnWidth, headerRowHeight, tableFieldType));
            headerFirstColumn.addView(headerRow); // First field in header is fixed horizontally and vertically.

            // Create shop header columns (fixed vertically, horizontally scrollable)
            tableFieldType = TABLE_FIELD_SCROLLABLE_HEADER;
            TableLayout headerShopNames = view.findViewById(R.id.table_header);
            headerRow = new TableRow(getContext());
            headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            headerRow.setGravity(Gravity.CENTER_VERTICAL);
            headerRow.setBackground(getResources().getDrawable(R.drawable.table_compare_scrollable_header_background, null));
            for (String shop : shops) {
                headerRow.addView(makeTableRow(shop, columnWidth, headerRowHeight, tableFieldType));
            }
            headerShopNames.addView(headerRow);

            // Get the item info data to populate the table.
            dbAdapter.open();
            List<Object[]> records = dbAdapter.getItemInfoForTable(itemIds, globalObject.getCurrency());
            dbAdapter.close();

            // If item info has been found for the items list.
            if (records.size() > 0) {
                // Initialise the item name column (fixed horizontally)
                TableLayout fixedColumn = view.findViewById(R.id.item_fixed_column);
                fixedColumn.setLayoutParams(new TableRow.LayoutParams(columnWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                // Rest of item data columns.
                TableLayout scrollableColumns = view.findViewById(R.id.shops_scrollable_columns);
                // Create SparseArray to store item group info for current item.
                SparseArray<String> sparseArrayInfo = new SparseArray<>();
                // Get the text size for the data rows.
                textSize = getResources().getDimensionPixelSize(R.dimen.table_data_text_size);

                // Create variable to store number of lines in a row for current item. Minimum is 1.
                int numDataLines = 1;
                int tempLines;
                int dataRowHeight;
                int recordsPosition = 0;

                // Set variable to the item name of the first item group. Done so that item row will
                // not be created until all of the records in the item group have been processed.
                if (records.size() > 0) {
                    lastItem = records.get(0)[1].toString();
                }

                // Determine the height of a single line of text for the table data.
                mPaint.setTextSize(textSize);
                mPaint.getTextBounds(lastItem, 0, text.length(), textBounds);
                textHeight = textBounds.height();

                // for each record of item info (grouped by item), create a row in the table.
                for (Object[] record : records) {

                    // Keep track of the records processed;
                    recordsPosition++;

                    itemName = record[1].toString();
                    shopName = record[3].toString();
                    brandName = record[4].toString();
                    quantityUnit = record[2].toString();
                    numberOfUnits = 1;

                    // Item group has changed, process the previous item group.
                    if (!itemName.equals(lastItem)) {
                        // Determine the max number of lines required for the item name.
                        tempLines = numberOfLines(textSize, lastItem, columnWidth, paddingLeftRight);

                        if (tempLines > numDataLines) {
                            numDataLines = tempLines;
                        }

                        // Now that we know max number of lines required for the whole row determine
                        // the height of the row in pixels.
                        dataRowHeight = (numDataLines * textHeight) + viewPaddingTopAndBottom +
                                ((numDataLines - 1) * 10); // No. of pixels between lines

                        // Set the field type for the item name fixed column.
                        tableFieldType = TABLE_FIELD_FIXED_COLUMN;
                        // Setup field for the first column (item name) which is fixed horizontally
                        TextView fixedTextView = makeTableRow(lastItem, columnWidth, dataRowHeight, tableFieldType);
                        fixedTextView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        fixedTextView.setGravity(Gravity.CENTER_VERTICAL);
                        fixedColumn.addView(fixedTextView);
                        // Finished processing the last item. It's info data row will need to
                        // be added to the scrollableColumns table layout before the next row is
                        // initialised and setup.
                        tableFieldType = TABLE_FIELD_DATA;
                        // Create and initialise a new row to hold shop, brand and price info for the last item.
                        dataRow = new TableRow(getContext());
                        dataRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        dataRow.setGravity(Gravity.CENTER);

                        for (int i = 0; i < shops.size(); i++) {
                            dataRow.addView(makeTableRow(sparseArrayInfo.get(i, ""), columnWidth,
                                    dataRowHeight, tableFieldType));
                        }
                        scrollableColumns.addView(dataRow);

                        // Clear the array ready to hold info for the next item.
                        sparseArrayInfo.clear();

                        // Reset the number of data lines for the next item.
                        numDataLines = 1;
                    }

                    // Process the current info record.
                    // Loop through the header shop names, if the shop name of the current info record
                    // matches the header shop name then store brand and price so it can be added to
                    // the item's info row once we have finished processing the current item group.
                    for (int i = 0; i < shops.size(); i++) {

                        if (shopName.equals(shops.get(i))) {

                            // Get total price of item for the item's current shop.
                            if (quantityUnit.equals("U")) {
                                // Find the item from the global item list and get it's number of units
                                itemId = ((Integer) record[0]);

                                for (Item item : globalObject.getItemArrayList()) {

                                    if (item.getItemId() == itemId) {

                                        if (item.getQuantity() == null) {
                                            Log.d("FragmentCompareList", "Quantity is null");
                                        }
                                        if (!item.getQuantity().equals("") && item.getQuantity() != null) {
                                            numberOfUnits = Integer.parseInt(item.getQuantity());
                                        }
                                        break;
                                    }
                                }

                                price = ((Float) record[5]) * numberOfUnits;
                            } else {
                                price = ((Float) record[5]);
                            }

                            // Add the shop info (brand & price) to the sparseArray to be saved until
                            // all records for the current item group has been processed.
                            if (brandName.length() == 0) {
                                sparseArrayInfo.put(i, NumberFormat.getCurrencyInstance().format(price));
                            } else {
                                sparseArrayInfo.put(i, brandName + "\n" +
                                        NumberFormat.getCurrencyInstance().format(price));

                                // Determine the number of lines required for the brand name.
                                tempLines = numberOfLines(textSize, brandName, columnWidth, paddingLeftRight);
                                // Add a line to hold the price
                                tempLines++;

                                // If the number of lines required for the item's current shop info is
                                // greater than the current number of lines required by the
                                // item row, then set the row's number of lines to the new number of
                                // lines required.
                                if (tempLines > numDataLines) {
                                    numDataLines = tempLines;
                                }
                            }

                            // add the price to the shop's running total.
                            float total = sparseArrayShopTotals.get(i, 0F) + price;
                            sparseArrayShopTotals.setValueAt(i, total);
                            break;
                        }
                    }

                    // Process the last item group.
                    if (records.size() == recordsPosition) {
                        // Determine the max number of lines required for the item name.
                        tempLines = numberOfLines(textSize, itemName, columnWidth, paddingLeftRight);
                        if (tempLines > numDataLines) {
                            numDataLines = tempLines;
                        }
                        // Now that we know max number of lines required for the whole row determine
                        // the height of the row in pixels.
                        //dataRowHeight = (numDataLines * textSize) + viewPaddingTopAndBottom +
                        //        ((numDataLines - 1) * (4 * dpToPixels)); // No. of pixels between lines
                        dataRowHeight = (numDataLines * textHeight) + viewPaddingTopAndBottom +
                                ((numDataLines - 1) * 10); // No. of pixels between lines
                        // Set the field type for the item name fixed column.
                        tableFieldType = TABLE_FIELD_FIXED_COLUMN;
                        // Setup field for the first column (item name) which is fixed horizontally
                        TextView fixedTextView = makeTableRow(itemName, columnWidth, dataRowHeight, tableFieldType);
                        fixedTextView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        fixedColumn.setGravity(Gravity.CENTER_VERTICAL);
                        fixedColumn.addView(fixedTextView);
                        // Create and initialise a new row to hold shop, brand and price info for the last item.
                        dataRow = new TableRow(getContext());
                        dataRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        dataRow.setGravity(Gravity.CENTER_VERTICAL);
                        // Submit the last item info row to the scrollableColumns table layout.
                        tableFieldType = TABLE_FIELD_DATA;
                        for (int i=0; i<shops.size(); i++) {
                            dataRow.addView(makeTableRow(sparseArrayInfo.get(i, ""), columnWidth,
                                    dataRowHeight, tableFieldType));
                        }
                        scrollableColumns.addView(dataRow);
                    }

                    lastItem = itemName;
                }
            }

            // Setup the shop totals layout.

            // Firstly create the 'TOTALS' label field.
            textSize = getResources().getDimensionPixelSize(R.dimen.table_compare_heading_text_size);
            mPaint.setTextSize(textSize);
            mPaint.getTextBounds(text, 0, text.length(), textBounds);
            textHeight = textBounds.height();

            int totalsRowHeight = textHeight + viewPaddingTopAndBottom;


            tableFieldType = TABLE_FIELD_FIXED_TOTALS;
            TableLayout shopTotalFixedView = view.findViewById(R.id.shop_total_fixed_view);
            totalsRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            totalsRow.addView(makeTableRow(getResources().getString(R.string.table_compare_shop_totals),
                    columnWidth, totalsRowHeight, tableFieldType));
            shopTotalFixedView.addView(totalsRow);

            // Now create the scrollable totals value fields.
            TableLayout shopTotalScrollableViews = view.findViewById(R.id.shop_total_scrollable_views);
            totalsRow = new TableRow(getContext());
            totalsRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int i = 0; i < sparseArrayShopTotals.size(); i++) {
                String totalCost = NumberFormat.getCurrencyInstance().format(sparseArrayShopTotals.get(i, 0F));
                totalsRow.addView(makeTableRow(totalCost, columnWidth, totalsRowHeight, tableFieldType));
            }
            shopTotalScrollableViews.addView(totalsRow);

            // Create necessary listeners to allow the three horizontal scroll views to be kept in sync.
            final HorizontalScrollView scrollViewHeader = view.findViewById(R.id.header_scroll_view);
            final HorizontalScrollView scrollViewData = view.findViewById(R.id.data_scroll_view);
            final HorizontalScrollView scrollViewTotals = view.findViewById(R.id.shop_totals_scroll_view);
            scrollViewData.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int x, int y, int oldx, int oldy) {
                    scrollViewHeader.scrollTo(x, y);
                    scrollViewTotals.scrollTo(x, y);
                }
            });
            scrollViewHeader.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int x, int y, int oldx, int oldy) {
                    scrollViewData.scrollTo(x, y);
                    scrollViewTotals.scrollTo(x, y);
                }
            });
            scrollViewTotals.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int x, int y, int oldx, int oldy) {
                    scrollViewHeader.scrollTo(x, y);
                    scrollViewData.scrollTo(x, y);
                }
            });
        }

        return view;
    }

    private int numberOfLines(int textSize, String text, int columnWidth, int paddingLeftRight) {

        int maxTextWidth = columnWidth - paddingLeftRight;
        Paint mPaint = new Paint();
        mPaint.setTextSize(textSize);

        // See if text fits into one line.
        if (mPaint.measureText(text, 0, text.length()) < maxTextWidth) {
            return 1;
        }

        ArrayList<String> currentLine = new ArrayList<>(); // Stores chunks of text that fit into a single line.
        ArrayList<String> lines = new ArrayList<>(); // Stores each of the created lines of text.
        String[] chunks = text.split("\\s"); // Splits the source text into chunks that were separated by spaces.
        // For each chunk of text split by spaces...
        for (String chunk : chunks) {

            if (mPaint.measureText(chunk) < maxTextWidth) {
                processFitChunk(chunk, maxTextWidth, mPaint, lines, currentLine);
            } else {
                // The chunk is too big it needs to be split.
                List<String> splitChunks = new ArrayList<>();
                int start = 0;
                // Go through the chunk letter by letter to see where the chunk needs to be split.
                for (int i=1; i<=chunk.length(); i++) {
                    String subStr = chunk.substring(start, i);
                    if (mPaint.measureText(subStr) >= maxTextWidth) {
                        // this one doesn't fit, take the previous sub string which does fit and store.
                        splitChunks.add(chunk.substring(start, i - 1));
                        start = i - 1;
                    }
                    if (i == chunk.length()) {
                        // The current sub string fits add to the lines array list.
                        splitChunks.add(chunk.substring(start, i));
                    }
                }
                // Now process the split chunks that now fit into the max text width.
                for (String splitChunk : splitChunks) {
                    processFitChunk(splitChunk, maxTextWidth, mPaint, lines, currentLine);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(TextUtils.join(" ", currentLine));
        }

        // Return the number of lines we have created
        return lines.size();
    }

    private void processFitChunk(String chunk, int maxTextWidth, Paint paint, ArrayList<String> lines,
                                 ArrayList<String> currentLine) {

        Paint mPaint = new Paint();
        mPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.table_data_text_size));

        // Add the chunk to the current line array
        currentLine.add(chunk);
        // Add the currentLine chunks together and see if they are still less than maxTextWidth
        String currentLineStr = TextUtils.join(" ", currentLine);

        if (paint.measureText(currentLineStr) >= maxTextWidth) {
            // Text is too long remove chunk from currentLine array.
            currentLine.remove(currentLine.size() - 1);
            // Add the other chunks together that does fit into a single line and store
            lines.add(TextUtils.join(" ", currentLine));
            // Empty the currentLine array ready to start processing the next line.
            currentLine.clear();
            // Now add the current chunk to currentLine as we know it fits.
            currentLine.add(chunk);
        }
    }

    private TextView makeTableRow(String text, int columnWidth, int rowHeight, String tableFieldType) {
        TextView recyclableTextView = new TextView(getContext());

        recyclableTextView.setPadding(
                getResources().getDimensionPixelSize(R.dimen.table_padding),
                0,
                getResources().getDimensionPixelSize(R.dimen.table_padding),
                0);

        switch (tableFieldType) {
            case TABLE_FIELD_FIXED_HEADER:
                recyclableTextView.setBackground(getResources()
                        .getDrawable(R.drawable.table_compare_fixed_header_background, null));
                recyclableTextView.setTextAppearance(R.style.TableCompareHeaderTextStyle);
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case TABLE_FIELD_SCROLLABLE_HEADER:
                recyclableTextView.setTextAppearance(R.style.TableCompareHeaderTextStyle);
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case TABLE_FIELD_FIXED_COLUMN:
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL);
                recyclableTextView.setTextAppearance(R.style.TableDataTextStyle);
                break;
            case TABLE_FIELD_DATA:
                recyclableTextView.setBackground(getResources()
                        .getDrawable(R.drawable.table_compare_data_background, null));
                recyclableTextView.setTextAppearance(R.style.TableDataTextStyle);
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case TABLE_FIELD_FIXED_TOTALS:
                recyclableTextView.setTextAppearance(R.style.TableCompareTotalsTextStyle);
                recyclableTextView.setGravity(Gravity.CENTER_VERTICAL);
                break;
        }

        recyclableTextView.setLineSpacing(10.0f, 0.70f); // 0.70 decreases spacing to nil.
        recyclableTextView.setText(text);                               // 10.0 sets line spacing to 10 pixels.
        recyclableTextView.setWidth(columnWidth);
        recyclableTextView.setHeight(rowHeight);
        return recyclableTextView;
    }
}

