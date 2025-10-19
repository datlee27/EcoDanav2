# EcoDana Project

## Thẻ test

| Ngân hàng | Số thẻ | Tên chủ thẻ | Ngày phát hành | Mật khẩu OTP |
| :--- | :--- | :--- | :--- | :--- |
| NCB | 9704198526191432198 | NGUYEN VAN A | 07/15 | 123456 |

## Luồng Booking

1.  **Người dùng đặt xe:** Người dùng tìm và chọn xe muốn thuê, sau đó gửi yêu cầu đặt xe.
2.  **Owner chấp nhận booking:** Chủ xe (owner) nhận được yêu cầu và chấp nhận booking.
3.  **Người dùng đặt cọc:** Người dùng tiến hành thanh toán tiền cọc cho xe đã đặt.
4.  **Admin nhận cọc:** Quản trị viên (admin) xác nhận đã nhận được tiền cọc từ người dùng.
5.  **Trạng thái cho thuê xe ở owner đã confirm booking:** Trạng thái của xe trong hệ thống của owner được cập nhật thành "đã xác nhận booking", hoàn tất quá trình đặt xe.
