package com.mad.vasteats.models;

public class Order {
    String deliveryAddress, deliveryFee, orderBy, orderId, orderRating, orderStatus, orderPlacedDateTime, orderTo, orderTotal;

    public Order() {
    }

    public Order(String deliveryAddress, String deliveryFee, String orderBy, String orderId, String orderRating,
                 String orderStatus, String orderPlacedDateTime, String orderTo, String orderTotal) {
        this.deliveryAddress = deliveryAddress;
        this.deliveryFee = deliveryFee;
        this.orderBy = orderBy;
        this.orderId = orderId;
        this.orderRating = orderRating;
        this.orderStatus = orderStatus;
        this.orderPlacedDateTime = orderPlacedDateTime;
        this.orderTo = orderTo;
        this.orderTotal = orderTotal;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderRating() {
        return orderRating;
    }

    public void setOrderRating(String orderRating) {
        this.orderRating = orderRating;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderPlacedDateTime() {
        return orderPlacedDateTime;
    }

    public void setOrderPlacedDateTime(String orderPlacedDateTime) {
        this.orderPlacedDateTime = orderPlacedDateTime;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(String orderTotal) {
        this.orderTotal = orderTotal;
    }
}
