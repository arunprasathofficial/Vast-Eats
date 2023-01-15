package com.mad.vasteats.models;

public class CartItem {

    String rowId, cartFoodId, cartFoodName, cartFoodImage, cartFoodQty, cartFoodRate, cartFoodTotal;

    public CartItem() {
    }

    public CartItem(String rowId, String cartFoodId, String cartFoodName, String cartFoodImage,
                    String cartFoodQty, String cartFoodRate, String cartFoodTotal) {
        this.rowId = rowId;
        this.cartFoodId = cartFoodId;
        this.cartFoodName = cartFoodName;
        this.cartFoodImage = cartFoodImage;
        this.cartFoodQty = cartFoodQty;
        this.cartFoodRate = cartFoodRate;
        this.cartFoodTotal = cartFoodTotal;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getCartFoodId() {
        return cartFoodId;
    }

    public void setCartFoodId(String cartFoodId) {
        this.cartFoodId = cartFoodId;
    }

    public String getCartFoodName() {
        return cartFoodName;
    }

    public void setCartFoodName(String cartFoodName) {
        this.cartFoodName = cartFoodName;
    }

    public String getCartFoodImage() {
        return cartFoodImage;
    }

    public void setCartFoodImage(String cartFoodImage) {
        this.cartFoodImage = cartFoodImage;
    }

    public String getCartFoodQty() {
        return cartFoodQty;
    }

    public void setCartFoodQty(String cartFoodQty) {
        this.cartFoodQty = cartFoodQty;
    }

    public String getCartFoodRate() {
        return cartFoodRate;
    }

    public void setCartFoodRate(String cartFoodRate) {
        this.cartFoodRate = cartFoodRate;
    }

    public String getCartFoodTotal() {
        return cartFoodTotal;
    }

    public void setCartFoodTotal(String cartFoodTotal) {
        this.cartFoodTotal = cartFoodTotal;
    }
}
