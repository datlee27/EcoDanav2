package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.BankAccount;
import com.ecodana.evodanavn1.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private static final String UPLOAD_DIR = "uploads/qr-codes/";

    public List<BankAccount> getBankAccountsByUserId(String userId) {
        return bankAccountRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    public Optional<BankAccount> getDefaultBankAccount(String userId) {
        return bankAccountRepository.findDefaultBankAccountByUserId(userId);
    }

    @Transactional
    public BankAccount saveBankAccount(BankAccount bankAccount, MultipartFile qrCodeFile) throws IOException {
        // Generate ID if new
        if (bankAccount.getBankAccountId() == null) {
            bankAccount.setBankAccountId(UUID.randomUUID().toString());
        }

        // Handle QR code file upload
        if (qrCodeFile != null && !qrCodeFile.isEmpty()) {
            String fileName = saveQRCodeFile(qrCodeFile, bankAccount.getBankAccountId());
            bankAccount.setQrCodeImagePath(fileName);
        }

        // If this is set as default, unset other defaults
        if (bankAccount.isDefault()) {
            unsetDefaultBankAccounts(bankAccount.getUser().getId());
        }

        bankAccount.setUpdatedDate(LocalDateTime.now());
        return bankAccountRepository.save(bankAccount);
    }

    @Transactional
    public void setAsDefault(String bankAccountId, String userId) {
        // First unset all defaults for this user
        unsetDefaultBankAccounts(userId);
        
        // Set the specified account as default
        Optional<BankAccount> bankAccountOpt = bankAccountRepository.findById(bankAccountId);
        if (bankAccountOpt.isPresent()) {
            BankAccount bankAccount = bankAccountOpt.get();
            if (bankAccount.getUser().getId().equals(userId)) {
                bankAccount.setDefault(true);
                bankAccount.setUpdatedDate(LocalDateTime.now());
                bankAccountRepository.save(bankAccount);
            }
        }
    }

    private void unsetDefaultBankAccounts(String userId) {
        List<BankAccount> userBankAccounts = bankAccountRepository.findByUserIdOrderByCreatedDateDesc(userId);
        for (BankAccount account : userBankAccounts) {
            if (account.isDefault()) {
                account.setDefault(false);
                account.setUpdatedDate(LocalDateTime.now());
                bankAccountRepository.save(account);
            }
        }
    }

    private String saveQRCodeFile(MultipartFile file, String bankAccountId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".jpg";
        String fileName = "qr_" + bankAccountId + "_" + System.currentTimeMillis() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }

    public void deleteBankAccount(String bankAccountId, String userId) {
        Optional<BankAccount> bankAccountOpt = bankAccountRepository.findById(bankAccountId);
        if (bankAccountOpt.isPresent()) {
            BankAccount bankAccount = bankAccountOpt.get();
            if (bankAccount.getUser().getId().equals(userId)) {
                // Delete QR code file if exists
                if (bankAccount.getQrCodeImagePath() != null) {
                    try {
                        Path filePath = Paths.get(UPLOAD_DIR + bankAccount.getQrCodeImagePath());
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        // Log error but don't fail the deletion
                        System.err.println("Could not delete QR code file: " + e.getMessage());
                    }
                }
                bankAccountRepository.delete(bankAccount);
            }
        }
    }

    public Optional<BankAccount> getBankAccountById(String bankAccountId) {
        return bankAccountRepository.findById(bankAccountId);
    }
}
