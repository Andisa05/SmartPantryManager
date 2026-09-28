package com.andisa.smartpantrymanager.model;

/**
 * Represents a single ingredient the user currently has at home.
 * Mirrors the plain "model" class pattern used throughout the module
 * (e.g. the Contact class): private fields with a full set of
 * getters and setters, and no persistence logic of its own -
 * persistence is handled entirely by PantryDataSource.
 */
public class PantryItem {

    private int id;
    private String name;
    private double quantity;
    private String unit;
    // Stored as epoch millis. -1 means "no expiry date set".
    private long expiryDate;

    public PantryItem() {
        id = -1;
        expiryDate = -1L;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean hasExpiryDate() {
        return expiryDate > 0;
    }
}
