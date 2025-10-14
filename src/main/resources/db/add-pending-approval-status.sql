-- Migration: KHÔNG CẦN THIẾT
-- Database đã có đầy đủ trạng thái cần thiết:
-- - Pending: Dùng cho cả "Chờ thanh toán" và "Chờ Owner duyệt" (phân biệt bằng PaymentStatus)
-- - Cancelled: Dùng cho cả "Đã hủy" và "Đã hoàn tiền" (phân biệt bằng PaymentStatus)

-- Logic phân biệt:
-- 1. Booking chờ thanh toán: Status=Pending, PaymentStatus=Unpaid
-- 2. Booking chờ Owner duyệt: Status=Pending, PaymentStatus=Paid
-- 3. Booking đã hủy/hoàn tiền: Status=Cancelled, PaymentStatus=Refunded

-- Kiểm tra các booking chờ duyệt
SELECT BookingId, BookingCode, Status, PaymentStatus, TotalAmount, CreatedDate
FROM Booking
WHERE Status = 'Pending' AND PaymentStatus = 'Paid'
ORDER BY CreatedDate DESC;
