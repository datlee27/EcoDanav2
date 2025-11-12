package com.ecodana.evodanavn1.controller.customer;

import com.ecodana.evodanavn1.model.BankAccount;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BankAccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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

        List<BankAccount> bankAccounts = bankAccountService.getBankAccountsByUserId(user.getId());
        model.addAttribute("bankAccounts", bankAccounts);
        model.addAttribute("currentUser", user);
        
        return "customer/bank-accounts";
    }

    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("bankAccount", new BankAccount());
        model.addAttribute("currentUser", user);
        
        return "customer/bank-account-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        BankAccount bankAccount = bankAccountService.getBankAccountById(id).orElse(null);
        if (bankAccount == null || !bankAccount.getUser().getId().equals(user.getId())) {
            return "redirect:/customer/bank-accounts";
        }

        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("currentUser", user);
        
        return "customer/bank-account-form";
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
                    redirectAttributes.addFlashAttribute("error", "Không có quyền chỉnh sửa tài khoản này!");
                    return "redirect:/customer/bank-accounts";
                }
                bankAccount.setUser(user);
            }

            bankAccountService.saveBankAccount(bankAccount, qrCodeFile);
            redirectAttributes.addFlashAttribute("success", "Lưu thông tin tài khoản thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/customer/bank-accounts";
    }

    @PostMapping("/set-default/{id}")
    public String setAsDefault(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            bankAccountService.setAsDefault(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Đã đặt làm tài khoản mặc định!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/customer/bank-accounts";
    }

    @PostMapping("/delete/{id}")
    public String deleteBankAccount(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            bankAccountService.deleteBankAccount(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/customer/bank-accounts";
    }
}
