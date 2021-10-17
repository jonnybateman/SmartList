package com.development.smartlist;

class Item {

    private int itemId;
    private String itemName;
    private int catId;
    private String catName;
    private String quantity;
    private Boolean divider;
    private Boolean checked;
    private Boolean selected;
    private int position;

    // Constructor
    public Item(int itemId, String itemName, int catId, String catName, String quantity,
                 Boolean divider, Boolean checked, Boolean selected, int position) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.catId = catId;
        this.catName = catName;
        this.quantity = quantity;
        this.divider = divider;
        this.checked = checked;
        this.selected = selected;
        this.position = position;
    }

    // This will be used by the list ArrayAdapter to display the contents of an object in the
    // list widget.
    @Override
    public String toString() {
        return this.itemName + "\n" + this.quantity;
    }

    public int getItemId() { return this.itemId; }

    public String getItemName() { return this.itemName; }

    public int getCatId() { return this.catId; }

    public String getCatName() { return this.catName; }

    public String getQuantity() { return this.quantity; }

    public Boolean getDivider() { return this.divider; }

    public Boolean getItemChecked() { return this.checked; }

    public void setItemChecked(Boolean check) { this.checked = check; }

    public Boolean getItemSelected() { return this.selected; }

    public void setItemSelected(Boolean select) { this.selected = select; }

    public void setItemPosition(int itemPosition) { this.position = itemPosition; }

    public int getItemPosition() { return this.position; }

}
