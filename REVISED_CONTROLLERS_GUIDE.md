# 🎯 Controllers Implementation - Sử dụng Database Hiện Có

## ✅ **Chiến lược:**

Sử dụng **BookingApproval table** và **Payment records** thay vì thêm columns mới vào Booking/Payment.

---

## 📝 **Staff Controller - 4 Endpoints:**

### **1. Confirm Payment Received**
```java
@PostMapping("/booking/{bookingId}/confirm-payment-received")
public String confirmPaymentReceived(@PathVariable String bookingId,
                                    @RequestParam(required = false) String notes,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();
        Payment payment = paymentService.findByBookingId(bookingId).orElseThrow();

        // Update payment
        payment.setPaymentStatus(Payment.PaymentStatus.Completed);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setUserId(currentUser);
        payment.setNotes(notes != null ? notes : "Đã nhận chuyển khoản từ khách hàng");
        paymentService.savePayment(payment);

        // Update booking
        booking.setPaymentStatus("Paid");
        booking.setHandledBy(currentUser);
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã xác nhận nhận tiền!");
        return "redirect:/staff/booking/" + bookingId;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/staff/booking/" + bookingId;
    }
}
```

### **2. Confirm Pickup**
```java
@PostMapping("/booking/{bookingId}/confirm-pickup")
public String confirmPickup(@PathVariable String bookingId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();

        // Check if approved
        if (booking.getStatus() != Booking.BookingStatus.Approved) {
            redirectAttributes.addFlashAttribute("error", "Đơn chưa được chủ xe duyệt!");
            return "redirect:/staff/booking/" + bookingId;
        }

        // Update to Ongoing
        booking.setStatus(Booking.BookingStatus.Ongoing);
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã xác nhận giao xe!");
        return "redirect:/staff/booking/" + bookingId;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/staff/booking/" + bookingId;
    }
}
```

### **3. Refund Payment**
```java
@PostMapping("/booking/{bookingId}/refund-payment")
public String refundPayment(@PathVariable String bookingId,
                           @RequestParam(required = false) String notes,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();
        
        // Check if rejected
        if (booking.getStatus() != Booking.BookingStatus.Rejected) {
            redirectAttributes.addFlashAttribute("error", "Chỉ hoàn tiền khi đơn bị từ chối!");
            return "redirect:/staff/booking/" + bookingId;
        }

        // Get original payment
        Payment originalPayment = paymentService.findByBookingId(bookingId).orElseThrow();

        // Create refund payment record
        Payment refund = new Payment();
        refund.setPaymentId(UUID.randomUUID().toString());
        refund.setBookingId(bookingId);
        refund.setAmount(originalPayment.getAmount());
        refund.setPaymentMethod("BankTransfer");
        refund.setPaymentStatus(Payment.PaymentStatus.Completed);
        refund.setPaymentType(Payment.PaymentType.Refund);
        refund.setPaymentDate(LocalDateTime.now());
        refund.setUserId(currentUser);
        refund.setNotes(notes != null ? notes : "Hoàn tiền do chủ xe từ chối");
        refund.setCreatedDate(LocalDateTime.now());
        paymentService.savePayment(refund);

        // Update booking
        booking.setPaymentStatus("Refunded");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã hoàn tiền cho khách hàng!");
        return "redirect:/staff/booking/" + bookingId;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/staff/booking/" + bookingId;
    }
}
```

### **4. Transfer to Owner**
```java
@PostMapping("/booking/{bookingId}/transfer-to-owner")
public String transferToOwner(@PathVariable String bookingId,
                             @RequestParam(required = false) String notes,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();
        
        // Check if completed
        if (booking.getStatus() != Booking.BookingStatus.Completed) {
            redirectAttributes.addFlashAttribute("error", "Chỉ chuyển tiền khi đơn đã hoàn thành!");
            return "redirect:/staff/booking/" + bookingId;
        }

        // Get original payment
        Payment originalPayment = paymentService.findByBookingId(bookingId).orElseThrow();

        // Create transfer payment record
        Payment transfer = new Payment();
        transfer.setPaymentId(UUID.randomUUID().toString());
        transfer.setBookingId(bookingId);
        transfer.setAmount(originalPayment.getAmount());
        transfer.setPaymentMethod("BankTransfer");
        transfer.setPaymentStatus(Payment.PaymentStatus.Completed);
        transfer.setPaymentType(Payment.PaymentType.FinalPayment);
        transfer.setPaymentDate(LocalDateTime.now());
        transfer.setUserId(currentUser);
        transfer.setNotes(notes != null ? notes : "Chuyển tiền cho chủ xe");
        transfer.setCreatedDate(LocalDateTime.now());
        paymentService.savePayment(transfer);

        // Update booking
        booking.setPaymentStatus("PaidToOwner");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã chuyển tiền cho chủ xe!");
        return "redirect:/staff/booking/" + bookingId;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/staff/booking/" + bookingId;
    }
}
```

---

## 📝 **Owner Controller - 3 Endpoints:**

### **1. Approve Booking**
```java
@PostMapping("/booking/{bookingId}/approve")
public String approveBooking(@PathVariable String bookingId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();

        // Check if payment is completed
        if (!"Paid".equals(booking.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("error", "Khách hàng chưa thanh toán!");
            return "redirect:/owner/dashboard";
        }

        // Create approval record
        BookingApproval approval = new BookingApproval();
        approval.setApprovalId(UUID.randomUUID().toString());
        approval.setBookingId(bookingId);
        approval.setStaffId(currentUser.getUserId()); // Owner ID
        approval.setApprovalStatus(BookingApproval.ApprovalStatus.Approved);
        approval.setApprovalDate(LocalDateTime.now());
        approval.setNote("Chủ xe đã duyệt đơn");
        bookingApprovalRepository.save(approval);

        // Update booking
        booking.setStatus(Booking.BookingStatus.Approved);
        booking.setPaymentStatus("Approved");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn đặt xe!");
        return "redirect:/owner/dashboard";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/owner/dashboard";
    }
}
```

### **2. Reject Booking**
```java
@PostMapping("/booking/{bookingId}/reject")
public String rejectBooking(@PathVariable String bookingId,
                           @RequestParam String rejectReason,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();

        // Create rejection record
        BookingApproval approval = new BookingApproval();
        approval.setApprovalId(UUID.randomUUID().toString());
        approval.setBookingId(bookingId);
        approval.setStaffId(currentUser.getUserId()); // Owner ID
        approval.setApprovalStatus(BookingApproval.ApprovalStatus.Rejected);
        approval.setApprovalDate(LocalDateTime.now());
        approval.setRejectionReason(rejectReason);
        bookingApprovalRepository.save(approval);

        // Update booking
        booking.setStatus(Booking.BookingStatus.Rejected);
        booking.setPaymentStatus("Refunding");
        booking.setCancelReason(rejectReason);
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã từ chối đơn đặt xe!");
        return "redirect:/owner/dashboard";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/owner/dashboard";
    }
}
```

### **3. Confirm Return**
```java
@PostMapping("/booking/{bookingId}/confirm-return")
public String confirmReturn(@PathVariable String bookingId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();

        // Check if ongoing
        if (booking.getStatus() != Booking.BookingStatus.Ongoing) {
            redirectAttributes.addFlashAttribute("error", "Xe chưa được giao!");
            return "redirect:/owner/dashboard";
        }

        // Update to completed
        booking.setStatus(Booking.BookingStatus.Completed);
        booking.setPaymentStatus("PendingTransferToOwner");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã xác nhận nhận xe!");
        return "redirect:/owner/dashboard";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/owner/dashboard";
    }
}
```

---

## 📝 **Customer Controller - 1 Endpoint:**

### **Confirm Return (Optional)**
```java
@PostMapping("/{bookingId}/confirm-return")
public String confirmReturn(@PathVariable String bookingId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) return "redirect:/login";

    try {
        Booking booking = bookingService.findById(bookingId).orElseThrow();

        // Just add a note or update status
        booking.setPaymentStatus("CustomerConfirmedReturn");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã xác nhận trả xe!");
        return "redirect:/booking/my-bookings";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/booking/my-bookings";
    }
}
```

---

## 🎯 **Tóm tắt:**

✅ **KHÔNG CẦN migration**
✅ Sử dụng `BookingApproval` table cho approve/reject
✅ Tạo multiple `Payment` records cho từng transaction
✅ Sử dụng `Booking.PaymentStatus` (String) để track trạng thái
✅ Đơn giản, dễ maintain

Bạn có muốn tôi implement ngay vào code không? 🚀
