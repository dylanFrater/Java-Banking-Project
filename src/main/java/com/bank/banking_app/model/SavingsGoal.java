package com.bank.banking_app.model;

/*
 This stores one savings goal
 from the database.
*/
public class SavingsGoal {
    private final int id;
    private final String username;
    private final String goalName;
    private final String targetAmount;
    private final String currentAmount;
    private final String status;
    private final String createdAt;

    public SavingsGoal(int id,
                       String username,
                       String goalName,
                       String targetAmount,
                       String currentAmount,
                       String status,
                       String createdAt) {
        this.id = id;
        this.username = username;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getGoalName() {
        return goalName;
    }

    public String getTargetAmount() {
        return targetAmount;
    }

    public String getCurrentAmount() {
        return currentAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return goalName
                + " | Saved $" + currentAmount
                + " of $" + targetAmount
                + " | Status: " + status;
    }
}
