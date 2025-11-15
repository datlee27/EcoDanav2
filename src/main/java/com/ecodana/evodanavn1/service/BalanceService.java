package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BalanceService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current balance for a user
     */
    public BigDecimal getBalance(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
    }

    /**
     * Credit (add) amount to user balance
     * Used when: booking completed, refund rejected
     */
    @Transactional
    public void creditBalance(String userId, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);
        
        user.setBalance(newBalance);
        userRepository.save(user);

        System.out.println(String.format(
            "[BALANCE] Credit - User: %s, Amount: %,.0f₫, Old Balance: %,.0f₫, New Balance: %,.0f₫, Reason: %s",
            userId, amount, currentBalance, newBalance, reason
        ));
    }

    /**
     * Debit (subtract) amount from user balance
     * Used when: withdrawal approved, refund approved
     */
    @Transactional
    public void debitBalance(String userId, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        
        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalStateException(String.format(
                "Insufficient balance. Current: %,.0f₫, Required: %,.0f₫",
                currentBalance, amount
            ));
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        user.setBalance(newBalance);
        userRepository.save(user);

        System.out.println(String.format(
            "[BALANCE] Debit - User: %s, Amount: %,.0f₫, Old Balance: %,.0f₫, New Balance: %,.0f₫, Reason: %s",
            userId, amount, currentBalance, newBalance, reason
        ));
    }

    /**
     * Set balance directly (use with caution)
     * Used for: admin adjustments, initial setup
     */
    @Transactional
    public void setBalance(String userId, BigDecimal amount, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        BigDecimal oldBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        user.setBalance(amount);
        userRepository.save(user);

        System.out.println(String.format(
            "[BALANCE] Set - User: %s, Old Balance: %,.0f₫, New Balance: %,.0f₫, Reason: %s",
            userId, oldBalance, amount, reason
        ));
    }

    /**
     * Check if user has sufficient balance
     */
    public boolean hasSufficientBalance(String userId, BigDecimal amount) {
        BigDecimal balance = getBalance(userId);
        return balance.compareTo(amount) >= 0;
    }
}
