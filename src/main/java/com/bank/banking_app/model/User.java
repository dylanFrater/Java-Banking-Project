package com.bank.banking_app.model;

/*
 This is a basic user model.
 A lot of user data is still
 read directly from the database.
*/
public class User {
    private String username;
    private String password;
    private String pin;
    private String fullName;
    private String cardNumber;
    private String cvc;
    private String expirationDate;

    public User(String username, String password, String pin, String fullName,
                String cardNumber, String cvc, String expirationDate) {
        this.username = username;
        this.password = password;
        this.pin = pin;
        this.fullName = fullName;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.expirationDate = expirationDate;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPin() {
        return pin;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
