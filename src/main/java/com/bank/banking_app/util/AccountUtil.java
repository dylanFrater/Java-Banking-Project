package com.bank.banking_app.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class AccountUtil {
    private static final Random RANDOM = new Random();

    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }
        return cardNumber.toString();
    }

    public static String generateCVC() {
        StringBuilder cvc = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            cvc.append(RANDOM.nextInt(10));
        }
        return cvc.toString();
    }

    public static String generateExpirationDate() {
        LocalDate futureDate = LocalDate.now().plusYears(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        return futureDate.format(formatter);
    }
}
