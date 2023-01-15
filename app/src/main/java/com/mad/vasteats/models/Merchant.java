package com.mad.vasteats.models;

public class Merchant {
    private String accountType, deliveryFee, email, fullName, merchantAddress, merchantCity,
            merchantImage, merchantName, merchantOpen, online, phoneNo, timestamp, uid;

    public Merchant() {
    }

    public Merchant(String accountType, String deliveryFee, String email, String fullName, String merchantAddress,
                    String merchantCity, String merchantImage, String merchantName, String merchantOpen, String online,
                    String phoneNo, String timestamp, String uid) {
        this.accountType = accountType;
        this.deliveryFee = deliveryFee;
        this.email = email;
        this.fullName = fullName;
        this.merchantAddress = merchantAddress;
        this.merchantCity = merchantCity;
        this.merchantImage = merchantImage;
        this.merchantName = merchantName;
        this.merchantOpen = merchantOpen;
        this.online = online;
        this.phoneNo = phoneNo;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMerchantAddress() {
        return merchantAddress;
    }

    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public String getMerchantImage() {
        return merchantImage;
    }

    public void setMerchantImage(String merchantImage) {
        this.merchantImage = merchantImage;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantOpen() {
        return merchantOpen;
    }

    public void setMerchantOpen(String merchantOpen) {
        this.merchantOpen = merchantOpen;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
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
