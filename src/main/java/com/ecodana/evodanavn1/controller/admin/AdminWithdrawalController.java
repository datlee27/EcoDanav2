package com.ecodana.evodanavn1.controller.admin;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.WithdrawalRequest;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.WithdrawalRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/withdrawals")
public class AdminWithdrawalController {

    private final WithdrawalRequestService withdrawalRequestService;
    private final UserService userService;

    @Autowired
    public AdminWithdrawalController(WithdrawalRequestService withdrawalRequestService, UserService userService) {
        this.withdrawalRequestService = withdrawalRequestService;
        this.userService = userService;
    }

    private String checkAdminAuthentication(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        if (!userService.isAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin role required.");
            return "redirect:/login";
        }
        return null;
    }

    @GetMapping
    public String showWithdrawalRequests(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAdminAuthentication(session, redirectAttributes);
        if (redirect != null) return redirect;

        List<WithdrawalRequest> pendingRequests = withdrawalRequestService.getAllPendingWithdrawalRequests();
        model.addAttribute("pendingWithdrawalRequests", pendingRequests);
        model.addAttribute("currentPage", "withdrawal-requests"); // Để highlight tab admin dashboard

        return "admin/withdrawal-requests"; // Tên template HTML cho admin
    }

    @PostMapping("/{id}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveWithdrawalRequest(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !userService.isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        String adminNotes = payload.getOrDefault("adminNotes", "Approved by admin.");

        try {
            withdrawalRequestService.approveWithdrawalRequest(id, adminNotes);
            return ResponseEntity.ok(Map.of("success", true, "message", "Yêu cầu rút tiền đã được duyệt thành công."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi khi duyệt yêu cầu rút tiền: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectWithdrawalRequest(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !userService.isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        String adminNotes = payload.getOrDefault("adminNotes", "Rejected by admin.");
        if (adminNotes.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Lý do từ chối không được để trống."));
        }

        try {
            withdrawalRequestService.rejectWithdrawalRequest(id, adminNotes);
            return ResponseEntity.ok(Map.of("success", true, "message", "Yêu cầu rút tiền đã bị từ chối."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi khi từ chối yêu cầu rút tiền: " + e.getMessage()));
        }
    }

    /**
     * API AJAX để lấy danh sách yêu cầu rút tiền đang chờ xử lý.
     * Dùng cho việc cập nhật bảng động trên frontend.
     */
    @GetMapping("/pending-ajax")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getPendingWithdrawalRequestsAjax(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !userService.isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<WithdrawalRequest> pendingRequests = withdrawalRequestService.getAllPendingWithdrawalRequests();
        List<Map<String, Object>> response = pendingRequests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("ownerName", req.getOwner().getFirstName() + " " + req.getOwner().getLastName());
            map.put("ownerEmail", req.getOwner().getEmail());
            map.put("amount", req.getAmount());
            map.put("requestDate", req.getRequestDate());
            map.put("status", req.getStatus().name());
            map.put("ownerBalance", req.getOwner().getBalance()); // Thêm số dư hiện tại của owner
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
