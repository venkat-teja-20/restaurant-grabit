package com.grabit.enums;

import lombok.Getter;

public enum CommonErrors {
    unknown_error("Something Went Wrong"),
    ACCESS_DENIED("You do not have the permission to access this resource"),
    AUTHENTICATION_FAILED("Error decoding signature"),
    AUTHENTICATION_EXPIRED("Signature has expired"),
    AUTHENTICATION_ERROR("Authentication is required"),
    AUTHENTICATION_REQUIRED("User is not authenticated"),
    Forbidden("You do not have the permission to access this resource"),
    INVALID_ORDER_STATUS("Order Status Provided is Not Valid");

    @Getter
    private String message;

    CommonErrors(String details) {
        this.message = details;
    }
}
