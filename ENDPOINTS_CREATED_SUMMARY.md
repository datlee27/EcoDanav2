# Endpoints Created - Summary

## ✅ **Owner Controller - Payment Flow V2**

### **1. Approve Booking**
```
POST /owner/booking/{bookingId}/approve
```
**Logic:**
- Check payment status = "Held" (staff đã nhận tiền)
- Update booking status → `Approved`
- Set `approvedBy` = current owner
- Set `approvedDate` = now

**Response:** Redirect to owner dashboard with success message

---

### **2. Reject Booking**
```
POST /owner/booking/{bookingId}/reject
Parameters: rejectReason (String)
```
**Logic:**
- Update booking status → `Rejected`
- Set `rejectedBy` = current owner
- Set `rejectedDate` = now
- Set `cancelReason` = rejectReason
- Update payment status → `Refunded` (mark for refund)
- Staff will process refund

**Response:** Redirect to owner dashboard with success message

---

### **3. Confirm Vehicle Return**
```
POST /owner/booking/{bookingId}/confirm-return
Parameters: notes (optional)
```
**Logic:**
- Set `returnConfirmedByOwner` = true
- Set `ownerConfirmDate` = now
- Check if customer also confirmed:
  - If YES (both confirmed):
    - Update booking status → `Completed`
    - Set `actualReturnDate` = now
    - Set `paymentStatus` = "PendingTransferToOwner"
  - If NO: Wait for customer confirmation

**Response:** Redirect to owner dashboard with success message

---

## ⏳ **Staff Controller - Payment Flow V2 (TODO)**

### **1. Confirm Payment Received**
```
POST /staff/booking/{bookingId}/confirm-payment-received
Parameters: notes (optional)
```
**Logic:**
- Update payment: status = `Held`, heldBy = staff, heldDate = now
- Update booking: status = `PendingOwnerApproval`, paymentStatus = "Held"
- Notify owner: "New booking needs approval"

---

### **2. Refund Payment**
```
POST /staff/booking/{bookingId}/refund-payment
Parameters: refundAmount, notes (optional)
```
**Logic:**
- Update payment: status = `Refunded`, refundDate = now, refundedBy = staff
- Update booking: paymentStatus = "Refunded"
- Notify customer: "Your payment has been refunded"

---

### **3. Transfer to Owner**
```
POST /staff/booking/{bookingId}/transfer-to-owner
Parameters: notes (optional)
```
**Logic:**
- Check: Both customer and owner confirmed return
- Update payment: status = `Completed`, transferredToOwner = true, transferDate = now
- Update booking: paymentStatus = "PaidToOwner"
- Notify owner: "You have received payment"

---

### **4. Confirm Pickup**
```
POST /staff/booking/{bookingId}/confirm-pickup
```
**Logic:**
- Update booking: status = `Ongoing`, pickupConfirmedBy = staff, actualPickupDate = now
- Notify customer: "Enjoy your trip!"

---

## ⏳ **Customer Controller - Payment Flow V2 (TODO)**

### **1. Confirm Vehicle Return**
```
POST /customer/booking/{bookingId}/confirm-return
```
**Logic:**
- Set `returnConfirmedByCustomer` = true
- Set `customerReturnDate` = now
- Check if owner also confirmed:
  - If YES (both confirmed):
    - Update booking status → `Completed`
    - Set `actualReturnDate` = now
    - Set `paymentStatus` = "PendingTransferToOwner"
  - If NO: Wait for owner confirmation
- Notify owner: "Customer confirmed return, please check vehicle"

---

## 📊 **Status Flow**

### **Happy Path:**
```
1. Customer pays → Staff confirms → Booking: PendingOwnerApproval, Payment: Held
2. Owner approves → Booking: Approved
3. Staff confirms pickup → Booking: Ongoing
4. Customer confirms return → returnConfirmedByCustomer = true
5. Owner confirms return → returnConfirmedByOwner = true, Booking: Completed
6. Staff transfers to owner → Payment: Completed
```

### **Reject Path:**
```
1. Customer pays → Staff confirms → Booking: PendingOwnerApproval, Payment: Held
2. Owner rejects → Booking: Rejected, Payment: Refunded
3. Staff processes refund → Customer receives money back
```

---

## 🎯 **Next Steps:**

1. ✅ Owner Controller - DONE
2. ⏳ Update Staff Controller with new endpoints
3. ⏳ Add customer confirm return endpoint
4. ⏳ Run database migration
5. ⏳ Test the complete flow
6. ⏳ Update UI to show new buttons

---

## 📝 **Files Modified:**

1. ✅ `Booking.java` - Added 11 new fields
2. ✅ `Payment.java` - Added 8 new fields
3. ✅ `OwnerController.java` - Added 3 new endpoints
4. ⏳ `StaffController.java` - Need to add 4 endpoints
5. ⏳ `BookingController.java` - Need to add 1 endpoint

Bạn có muốn tôi tiếp tục update Staff Controller không? 🚀
