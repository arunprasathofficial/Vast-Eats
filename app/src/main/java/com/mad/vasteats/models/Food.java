package com.mad.vasteats.models;

public class Food {
    private String foodId, foodImage, foodName, foodCategory, foodDescription, foodOriginalPrice,
            discountAvailable, foodDiscountedPrice, foodDiscountTitle, foodAvailable, timestamp,
            uid;

    public Food() {
    }

    public Food(String foodId, String foodImage, String foodName, String foodCategory, String foodDescription,
                String foodOriginalPrice, String discountAvailable, String foodDiscountedPrice, String foodDiscountTitle,
                String foodAvailable, String timestamp, String uid) {
        this.foodId = foodId;
        this.foodImage = foodImage;
        this.foodName = foodName;
        this.foodCategory = foodCategory;
        this.foodDescription = foodDescription;
        this.foodOriginalPrice = foodOriginalPrice;
        this.discountAvailable = discountAvailable;
        this.foodDiscountedPrice = foodDiscountedPrice;
        this.foodDiscountTitle = foodDiscountTitle;
        this.foodAvailable = foodAvailable;
        this.timestamp = timestamp;
        this.uid = uid;
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

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getFoodOriginalPrice() {
        return foodOriginalPrice;
    }

    public void setFoodOriginalPrice(String foodOriginalPrice) {
        this.foodOriginalPrice = foodOriginalPrice;
    }

    public String getDiscountAvailable() {
        return discountAvailable;
    }

    public void setDiscountAvailable(String discountAvailable) {
        this.discountAvailable = discountAvailable;
    }

    public String getFoodDiscountedPrice() {
        return foodDiscountedPrice;
    }

    public void setFoodDiscountedPrice(String foodDiscountedPrice) {
        this.foodDiscountedPrice = foodDiscountedPrice;
    }

    public String getFoodDiscountTitle() {
        return foodDiscountTitle;
    }

    public void setFoodDiscountTitle(String foodDiscountTitle) {
        this.foodDiscountTitle = foodDiscountTitle;
    }

    public String getFoodAvailable() {
        return foodAvailable;
    }

    public void setFoodAvailable(String foodAvailable) {
        this.foodAvailable = foodAvailable;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
