package com.ecodana.evodanavn1.controller.admin;

import com.ecodana.evodanavn1.model.RefundRequest;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.RefundRequestRepository;
import com.ecodana.evodanavn1.service.RefundRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/refund-requests")
public class RefundRequestController {

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private RefundRequestService refundRequestService;

    /**
     * Display all refund requests (server-side rendering)
     */
    @GetMapping
    public String listRefundRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get all refund requests
        List<RefundRequest> refundRequests = refundRequestRepository.findAll();

        // Filter by status if provided
        if (status != null && !status.isEmpty() && !status.equals("All")) {
            refundRequests = refundRequests.stream()
                    .filter(r -> r.getStatus().toString().equals(status))
                    .toList();
        }

        // Filter by search term if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            refundRequests = refundRequests.stream()
                    .filter(r -> 
                        (r.getBooking() != null && r.getBooking().getBookingCode().toLowerCase().contains(searchLower)) ||
                        (r.getUser() != null && (r.getUser().getFirstName().toLowerCase().contains(searchLower) ||
                                                r.getUser().getLastName().toLowerCase().contains(searchLower) ||
                                                r.getUser().getEmail().toLowerCase().contains(searchLower)))
                    )
                    .toList();
        }

        // Calculate statistics
        long totalCount = refundRequestRepository.findAll().size();
        long pendingCount = refundRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RefundRequest.RefundStatus.Pending)
                .count();
        long approvedCount = refundRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RefundRequest.RefundStatus.Approved)
                .count();
        long rejectedCount = refundRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RefundRequest.RefundStatus.Rejected)
                .count();
        long urgentCount = refundRequestRepository.findAll().stream()
                .filter(r -> r.isWithinTwoHours() && r.getStatus() == RefundRequest.RefundStatus.Pending)
                .count();

        // Calculate total pending amount
        double totalPendingAmount = refundRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RefundRequest.RefundStatus.Pending)
                .mapToDouble(r -> r.getRefundAmount().doubleValue())
                .sum();

        model.addAttribute("refundRequests", refundRequests);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("urgentCount", urgentCount);
        model.addAttribute("totalPendingAmount", totalPendingAmount);
        model.addAttribute("currentStatus", status != null ? status : "All");
        model.addAttribute("searchTerm", search != null ? search : "");

        return "admin/refund-requests-list";
    }

    /**
     * View refund request details
     */
    @GetMapping("/{id}")
    public String viewRefundRequest(
            @PathVariable String id,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<RefundRequest> refundRequest = refundRequestRepository.findById(id);
        if (refundRequest.isEmpty()) {
            return "redirect:/admin/refund-requests?error=Refund request not found";
        }

        model.addAttribute("refundRequest", refundRequest.get());
        return "admin/refund-request-detail";
    }

    /**
     * Approve refund request
     */
    @PostMapping("/{id}/approve")
    public String approveRefundRequest(
            @PathVariable String id,
            @RequestParam(required = false) String adminNotes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            refundRequestService.approveRefundRequest(id, currentUser.getId(), adminNotes != null ? adminNotes : "");
            redirectAttributes.addFlashAttribute("success", "Refund request approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving refund request: " + e.getMessage());
        }

        return "redirect:/admin/refund-requests";
    }

    /**
     * Reject refund request
     */
    @PostMapping("/{id}/reject")
    public String rejectRefundRequest(
            @PathVariable String id,
            @RequestParam(required = false) String adminNotes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            refundRequestService.rejectRefundRequest(id, currentUser.getId(), adminNotes != null ? adminNotes : "");
            redirectAttributes.addFlashAttribute("success", "Refund request rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting refund request: " + e.getMessage());
        }

        return "redirect:/admin/refund-requests";
    }
}
