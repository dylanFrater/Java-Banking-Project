package com.bank.banking_app.model;

/*
 This stores the profile data
 shown on the settings screen.
*/
public class ProfileInfo {
    private final String fullName;
    private final String username;
    private final String maskedCardNumber;
    private final String routingNumber;
    private final String createdAt;
    private final String accountStatus;

    public ProfileInfo(String fullName,
                       String username,
                       String maskedCardNumber,
                       String routingNumber,
                       String createdAt,
                       String accountStatus) {
        this.fullName = fullName;
        this.username = username;
        this.maskedCardNumber = maskedCardNumber;
        this.routingNumber = routingNumber;
        this.createdAt = createdAt;
        this.accountStatus = accountStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAccountStatus() {
        return accountStatus;
    }
}
