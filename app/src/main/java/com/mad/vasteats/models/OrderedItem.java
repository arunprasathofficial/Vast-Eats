package com.mad.vasteats.models;

public class OrderedItem {
    String foodId, foodImage, foodQty, foodRate, foodName, foodTotal;

    public OrderedItem() {
    }

    public OrderedItem(String foodId, String foodImage, String foodQty, String foodRate, String foodName, String foodTotal) {
        this.foodId = foodId;
        this.foodImage = foodImage;
        this.foodQty = foodQty;
        this.foodRate = foodRate;
        this.foodName = foodName;
        this.foodTotal = foodTotal;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodQty() {
        return foodQty;
    }

    public void setFoodQty(String foodQty) {
        this.foodQty = foodQty;
    }

    public String getFoodRate() {
        return foodRate;
    }

    public void setFoodRate(String foodRate) {
        this.foodRate = foodRate;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodTotal() {
        return foodTotal;
    }

    public void setFoodTotal(String foodTotal) {
        this.foodTotal = foodTotal;
    }
}
