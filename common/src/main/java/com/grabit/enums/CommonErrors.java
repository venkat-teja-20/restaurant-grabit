package com.grabit.enums;

import lombok.Getter;

public enum CommonErrors {
    unknown_error("Something Went Wrong"),
    INVALID_ORDER_STATUS("Order Status Provided is Not Valid");

    @Getter
    private String message;

    CommonErrors(String details) {
        this.message = details;
    }
}
