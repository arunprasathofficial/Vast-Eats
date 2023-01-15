package com.mad.vasteats.models;

public class Category {
    private String categoryId, categoryName, activeStatus, uid;

    public Category() {
    }

    public Category(String categoryId, String categoryName, String activeStatus, String uid) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.activeStatus = activeStatus;
        this.uid = uid;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
