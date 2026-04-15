package com.bank.banking_app;

import com.bank.banking_app.model.User;
import com.bank.banking_app.service.UserService;
import com.bank.banking_app.util.AccountUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingAppApplication.class, args);

		try {
			User user = new User(
					"jdoe2",
					"password123",
					"1234",
					"John Doe",
					AccountUtil.generateCardNumber(),
					AccountUtil.generateCVC(),
					AccountUtil.generateExpirationDate()
			);

			new UserService().createUser(user);
			System.out.println("User created successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
