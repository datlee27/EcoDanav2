package com.ecodana.evodanavn1.controller.customer;

import com.ecodana.evodanavn1.model.BankAccount;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BankAccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/customer/bank-accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping
    public String listBankAccounts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Redirect to profile page with bank-accounts tab
        return "redirect:/profile#bank-accounts-section";
    }

    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Redirect to profile page with bank-accounts tab
        return "redirect:/profile#bank-accounts-section";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Redirect to profile page with bank-accounts tab
        return "redirect:/profile#bank-accounts-section";
    }

    @PostMapping("/save")
    public String saveBankAccount(
            @ModelAttribute BankAccount bankAccount,
            @RequestParam(value = "qrCodeFile", required = false) MultipartFile qrCodeFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Set user if new account
            if (bankAccount.getBankAccountId() == null) {
                bankAccount.setBankAccountId(UUID.randomUUID().toString());
                bankAccount.setUser(user);
            } else {
                // Verify ownership for existing account
                BankAccount existing = bankAccountService.getBankAccountById(bankAccount.getBankAccountId()).orElse(null);
                if (existing == null || !existing.getUser().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("bank_error", "Không có quyền chỉnh sửa tài khoản này!");
                    return "redirect:/profile#bank-accounts-section";
                }
                bankAccount.setUser(user);
            }

            bankAccountService.saveBankAccount(bankAccount, qrCodeFile);
            String message = (bankAccount.getBankAccountId() == null) ? 
                "Thêm tài khoản ngân hàng thành công!" : 
                "Cập nhật tài khoản ngân hàng thành công!";
            redirectAttributes.addFlashAttribute("bank_success", message);
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("bank_error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile#bank-accounts-section";
    }

    @PostMapping("/set-default/{id}")
    public String setAsDefault(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            bankAccountService.setAsDefault(id, user.getId());
            redirectAttributes.addFlashAttribute("bank_success", "Đã đặt làm tài khoản mặc định!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("bank_error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile#bank-accounts-section";
    }

    @PostMapping("/delete/{id}")
    public String deleteBankAccount(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            bankAccountService.deleteBankAccount(id, user.getId());
            redirectAttributes.addFlashAttribute("bank_success", "Xóa tài khoản ngân hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("bank_error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile#bank-accounts-section";
    }

    @GetMapping("/api/list")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getBankAccountsApi(HttpSession session) {
        try {
            User user = (User) session.getAttribute("currentUser");
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(error);
            }

            System.out.println("Getting bank accounts for user: " + user.getId());
            List<BankAccount> bankAccounts = bankAccountService.getBankAccountsByUserId(user.getId());
            System.out.println("Found " + bankAccounts.size() + " bank accounts");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bankAccounts", bankAccounts);
            response.put("count", bankAccounts.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addBankAccountApi(
            @RequestParam String bankName,
            @RequestParam String accountNumber,
            @RequestParam String accountHolder,
            @RequestParam(required = false) MultipartFile qrCodeImage,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("currentUser");
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(error);
            }

            // Create new bank account
            BankAccount bankAccount = new BankAccount();
            bankAccount.setBankAccountId(UUID.randomUUID().toString());
            bankAccount.setUser(user);
            bankAccount.setBankName(bankName);
            bankAccount.setAccountNumber(accountNumber);
            bankAccount.setAccountHolderName(accountHolder);
            bankAccount.setDefault(false);

            // Save bank account with QR code if provided
            bankAccountService.saveBankAccount(bankAccount, qrCodeImage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm tài khoản ngân hàng thành công!");
            response.put("bankAccountId", bankAccount.getBankAccountId());
            response.put("bankAccount", bankAccount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi thêm tài khoản: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * API endpoint for admin to get bank accounts of a specific customer
     * Used in refund request modal to load customer's bank accounts
     */
    @GetMapping("/api/list-by-user/{userId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getBankAccountsByUserIdApi(@PathVariable String userId, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(error);
            }

            // Check if current user is admin (optional - can be removed if not needed)
            // For now, allow any authenticated user to view bank accounts

            System.out.println("Getting bank accounts for user: " + userId);
            List<BankAccount> bankAccounts = bankAccountService.getBankAccountsByUserId(userId);
            System.out.println("Found " + bankAccounts.size() + " bank accounts");
            
            return ResponseEntity.ok(bankAccounts);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
}
