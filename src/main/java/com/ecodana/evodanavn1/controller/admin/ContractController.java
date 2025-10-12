package com.ecodana.evodanavn1.controller.admin;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Contract;
import com.ecodana.evodanavn1.model.Contract.ContractStatus;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.ContractService;
import com.ecodana.evodanavn1.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        if (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin/Staff role required.");
            return "redirect:/login";
        }
        return null;
    }

    @GetMapping
    public String contractManagement(HttpSession session, RedirectAttributes redirectAttributes) {
        // Redirect to dashboard with contracts tab active
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin/Staff role required.");
            return "redirect:/login";
        }
        return "redirect:/admin/dashboard?tab=contracts";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContract(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            Optional<Contract> contractOpt = contractService.getContractById(id);
            if (contractOpt.isPresent()) {
                Contract contract = contractOpt.get();
                Map<String, Object> data = new HashMap<>();
                data.put("contractId", contract.getContractId());
                data.put("contractCode", contract.getContractCode());
                data.put("status", contract.getStatus().name());
                data.put("createdDate", contract.getCreatedDate());
                data.put("signedDate", contract.getSignedDate());
                data.put("completedDate", contract.getCompletedDate());
                data.put("termsAccepted", contract.getTermsAccepted());
                data.put("notes", contract.getNotes());
                data.put("cancellationReason", contract.getCancellationReason());
                
                if (contract.getUser() != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", contract.getUser().getId());
                    userData.put("email", contract.getUser().getEmail());
                    userData.put("fullName", contract.getUser().getFirstName() + " " + contract.getUser().getLastName());
                    userData.put("phoneNumber", contract.getUser().getPhoneNumber());
                    data.put("user", userData);
                }
                
                if (contract.getBooking() != null) {
                    Map<String, Object> bookingData = new HashMap<>();
                    bookingData.put("bookingId", contract.getBooking().getBookingId());
                    bookingData.put("pickupDateTime", contract.getBooking().getPickupDateTime());
                    bookingData.put("returnDateTime", contract.getBooking().getReturnDateTime());
                    bookingData.put("totalAmount", contract.getBooking().getTotalAmount());
                    
                    if (contract.getBooking().getVehicle() != null) {
                        Map<String, Object> vehicleData = new HashMap<>();
                        vehicleData.put("vehicleId", contract.getBooking().getVehicle().getVehicleId());
                        vehicleData.put("model", contract.getBooking().getVehicle().getVehicleModel());
                        vehicleData.put("licensePlate", contract.getBooking().getVehicle().getLicensePlate());
                        bookingData.put("vehicle", vehicleData);
                    }
                    data.put("booking", bookingData);
                }
                
                return ResponseEntity.ok(Map.of("status", "success", "data", data));
            }
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Contract not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createContract(@RequestBody Map<String, String> contractData, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            String userId = contractData.get("userId");
            String bookingId = contractData.get("bookingId");
            
            if (userId == null || bookingId == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "User ID and Booking ID are required"));
            }

            User user = userService.findById(userId);
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "User not found"));
            }
            if (!bookingOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Booking not found"));
            }

            Contract contract = contractService.createContract(user, bookingOpt.get());
            
            if (contractData.containsKey("notes")) {
                contract.setNotes(contractData.get("notes"));
                contractService.updateContract(contract);
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract created successfully", "contractId", contract.getContractId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error creating contract: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateContract(@PathVariable String id, @RequestBody Map<String, String> contractData, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            Optional<Contract> contractOpt = contractService.getContractById(id);
            if (!contractOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Contract not found"));
            }

            Contract contract = contractOpt.get();
            
            if (contractData.containsKey("notes")) {
                contract.setNotes(contractData.get("notes"));
            }
            if (contractData.containsKey("status")) {
                contract.setStatus(ContractStatus.valueOf(contractData.get("status")));
            }

            contractService.updateContract(contract);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error updating contract: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/sign")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signContract(@PathVariable String id, @RequestBody Map<String, String> signatureData, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            String signature = signatureData.get("signatureData");
            String method = signatureData.getOrDefault("signatureMethod", "digital");
            
            Contract contract = contractService.signContract(id, signature, method);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract signed successfully", "signedDate", contract.getSignedDate()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error signing contract: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeContract(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            contractService.completeContract(id);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error completing contract: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelContract(@PathVariable String id, @RequestBody Map<String, String> cancellationData, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            String reason = cancellationData.getOrDefault("reason", "No reason provided");
            contractService.cancelContract(id, reason);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error cancelling contract: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateContractDetails(@PathVariable String id, @RequestBody Map<String, Object> updates, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

        try {
            System.out.println("DEBUG: Updating contract " + id);
            System.out.println("DEBUG: Updates: " + updates);
            
            Optional<Contract> contractOpt = contractService.getContractById(id);
            if (!contractOpt.isPresent()) {
                System.out.println("DEBUG: Contract not found");
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Contract not found"));
            }
            
            Contract contract = contractOpt.get();
            System.out.println("DEBUG: Found contract: " + contract.getContractCode());
            
            // Update status
            if (updates.containsKey("status")) {
                String statusStr = (String) updates.get("status");
                System.out.println("DEBUG: Setting status: " + statusStr);
                contract.setStatus(ContractStatus.valueOf(statusStr));
                
                // Auto-set dates based on status
                if (statusStr.equals("Signed") && contract.getSignedDate() == null) {
                    contract.setSignedDate(java.time.LocalDateTime.now());
                }
                if (statusStr.equals("Completed") && contract.getCompletedDate() == null) {
                    contract.setCompletedDate(java.time.LocalDateTime.now());
                }
            }
            
            // Update notes
            if (updates.containsKey("notes")) {
                String notes = (String) updates.get("notes");
                System.out.println("DEBUG: Setting notes: " + notes);
                contract.setNotes(notes);
            }
            
            // Update terms accepted
            if (updates.containsKey("termsAccepted")) {
                Boolean termsAccepted = (Boolean) updates.get("termsAccepted");
                System.out.println("DEBUG: Setting termsAccepted: " + termsAccepted);
                contract.setTermsAccepted(termsAccepted);
            }
            
            // Update cancellation reason
            if (updates.containsKey("cancellationReason")) {
                String reason = (String) updates.get("cancellationReason");
                System.out.println("DEBUG: Setting cancellationReason: " + reason);
                contract.setCancellationReason(reason);
            }
            
            System.out.println("DEBUG: Saving contract...");
            Contract saved = contractService.updateContract(contract);
            System.out.println("DEBUG: Contract saved successfully: " + saved.getContractId());
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract updated successfully"));
        } catch (Exception e) {
            System.err.println("ERROR: Failed to update contract: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to update contract: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteContract(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !userService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied. Admin only."));
        }

        try {
            contractService.deleteContract(id);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Contract deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error deleting contract: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Controller is working!");
        result.put("total_contracts", contractService.getAllContracts().size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-test-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateTestData(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !userService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied. Admin only."));
        }

        try {
            // Lấy tất cả users và bookings
            List<User> users = userService.getAllUsers();
            List<Booking> bookings = bookingService.getAllBookings();

            if (users.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "No users found in database. Please create users first."));
            }
            if (bookings.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "No bookings found in database. Please create bookings first."));
            }

            int created = 0;
            
            // Tạo 4 contracts mẫu với các status khác nhau
            if (bookings.size() >= 1) {
                Contract c1 = contractService.createContract(users.get(0), bookings.get(0));
                c1.setStatus(ContractStatus.Draft);
                contractService.updateContract(c1);
                created++;
            }

            if (bookings.size() >= 2) {
                Contract c2 = contractService.createContract(users.get(users.size() > 1 ? 1 : 0), bookings.get(1));
                c2.setStatus(ContractStatus.Signed);
                c2.setSignedDate(java.time.LocalDateTime.now().minusDays(1));
                c2.setSignatureData("digital-signature");
                c2.setSignatureMethod("digital");
                c2.setTermsAccepted(true);
                contractService.updateContract(c2);
                created++;
            }

            if (bookings.size() >= 3) {
                Contract c3 = contractService.createContract(users.get(0), bookings.get(2));
                c3.setStatus(ContractStatus.Completed);
                c3.setSignedDate(java.time.LocalDateTime.now().minusDays(4));
                c3.setCompletedDate(java.time.LocalDateTime.now().minusDays(1));
                c3.setSignatureData("digital-signature");
                c3.setSignatureMethod("digital");
                c3.setTermsAccepted(true);
                c3.setNotes("Completed successfully");
                contractService.updateContract(c3);
                created++;
            }

            if (bookings.size() >= 4) {
                Contract c4 = contractService.createContract(users.get(users.size() > 1 ? 1 : 0), bookings.get(3));
                c4.setStatus(ContractStatus.Cancelled);
                c4.setCancellationReason("Customer requested cancellation");
                contractService.updateContract(c4);
                created++;
            }

            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "message", "Created " + created + " test contracts successfully",
                "created", created
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error generating test data: " + e.getMessage()));
        }
    }
}
