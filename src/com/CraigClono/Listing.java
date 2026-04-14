package com.CraigClono;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Listing {
    public int listingId;
    public int userId;
    public String title;
    public String description;
    public BigDecimal price;
    public String sellerName;
    public String sellerPhone;
    public Timestamp createdDate;
    public String imagePath;  // ← NEW: Image path
    public String category;
    public String location;

    public Listing(int listingId, int userId, String title, String description,
                   BigDecimal price, String sellerName, String sellerPhone,
                   String category, String location) {
        this.listingId = listingId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.category = category;
        this.location = location;
        this.imagePath = null;
    }

    public Listing() {}

    @Override
    public String toString() {
        // Format price with rupee symbol
        String formattedPrice = String.format("₹%,.0f", price);
        return title + " - " + formattedPrice + " (by " + sellerName + ")";
    }
}