# WebSocket & Email Notification Setup

## 📦 Dependencies đã thêm

### 1. WebSocket
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. Email (đã có sẵn)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 🔌 WebSocket Configuration

### Files đã tạo:

1. **WebSocketConfig.java** - Cấu hình WebSocket
2. **WebSocketNotificationService.java** - Service gửi thông báo realtime

### Cách sử dụng:

#### Trong Controller (sau khi thanh toán thành công):

```java
@Autowired
private WebSocketNotificationService webSocketNotificationService;

// Gửi thông báo cho Owner
webSocketNotificationService.notifyOwnerNewBooking(
    booking.getBookingCode(),
    booking.getUser().getFirstName() + " " + booking.getUser().getLastName(),
    booking.getTotalAmount().doubleValue()
);
```

#### Trong OwnerBookingController (sau khi duyệt/từ chối):

```java
// Khi duyệt
webSocketNotificationService.notifyCustomerBookingStatus(
    booking.getUser().getId(),
    booking.getBookingCode(),
    "APPROVED",
    "Đơn đặt xe của bạn đã được chấp nhận!"
);

// Khi từ chối
webSocketNotificationService.notifyCustomerBookingStatus(
    booking.getUser().getId(),
    booking.getBookingCode(),
    "REJECTED",
    "Đơn đặt xe của bạn đã bị từ chối. Tiền đã được hoàn lại."
);

// Thông báo Staff về hoàn tiền
webSocketNotificationService.notifyStaffRefund(
    booking.getBookingCode(),
    booking.getUser().getFirstName() + " " + booking.getUser().getLastName(),
    booking.getTotalAmount().doubleValue(),
    rejectionReason
);
```

---

## 🌐 Frontend WebSocket Integration

### Thêm vào HTML (Owner Dashboard):

```html
<!-- SockJS and STOMP libraries -->
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

<script>
    var stompClient = null;
    
    function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);
            
            // Subscribe to owner notifications
            stompClient.subscribe('/topic/owner/notifications', function(notification) {
                var data = JSON.parse(notification.body);
                showNotification(data);
            });
        });
    }
    
    function showNotification(data) {
        // Show browser notification
        if (Notification.permission === "granted") {
            new Notification("Booking mới!", {
                body: data.message,
                icon: "/images/logo.png"
            });
        }
        
        // Show in-app notification
        var notifDiv = document.createElement('div');
        notifDiv.className = 'notification-toast';
        notifDiv.innerHTML = `
            <div class="bg-green-500 text-white p-4 rounded-lg shadow-lg">
                <p class="font-bold">${data.message}</p>
                <p class="text-sm">Mã: ${data.bookingCode} - ${data.amount.toLocaleString()} ₫</p>
            </div>
        `;
        document.body.appendChild(notifDiv);
        
        // Auto remove after 5 seconds
        setTimeout(() => notifDiv.remove(), 5000);
        
        // Reload page to show new booking
        setTimeout(() => location.reload(), 2000);
    }
    
    // Request notification permission
    if (Notification.permission !== "granted") {
        Notification.requestPermission();
    }
    
    // Connect on page load
    connect();
</script>
```

---

## 📧 Email Notification Setup

### Cấu hình trong `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### Tạo EmailService:

```java
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendBookingApprovedEmail(String to, String bookingCode, String customerName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Đơn đặt xe " + bookingCode + " đã được duyệt");
            helper.setText(buildApprovedEmailContent(bookingCode, customerName), true);
            
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendBookingRejectedEmail(String to, String bookingCode, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Đơn đặt xe " + bookingCode + " đã bị từ chối");
            helper.setText(buildRejectedEmailContent(bookingCode, reason), true);
            
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String buildApprovedEmailContent(String bookingCode, String customerName) {
        return "<html><body>" +
               "<h2>Xin chào " + customerName + ",</h2>" +
               "<p>Đơn đặt xe <strong>" + bookingCode + "</strong> của bạn đã được chấp nhận!</p>" +
               "<p>Vui lòng đến nhận xe đúng giờ đã đặt.</p>" +
               "<p>Trân trọng,<br>EcoDana Team</p>" +
               "</body></html>";
    }
    
    private String buildRejectedEmailContent(String bookingCode, String reason) {
        return "<html><body>" +
               "<h2>Thông báo từ chối đơn đặt xe</h2>" +
               "<p>Đơn đặt xe <strong>" + bookingCode + "</strong> của bạn đã bị từ chối.</p>" +
               "<p><strong>Lý do:</strong> " + reason + "</p>" +
               "<p>Tiền đã được hoàn lại vào tài khoản của bạn.</p>" +
               "<p>Trân trọng,<br>EcoDana Team</p>" +
               "</body></html>";
    }
}
```

### Sử dụng trong Controller:

```java
@Autowired
private EmailService emailService;

// Khi duyệt
emailService.sendBookingApprovedEmail(
    booking.getUser().getEmail(),
    booking.getBookingCode(),
    booking.getUser().getFirstName() + " " + booking.getUser().getLastName()
);

// Khi từ chối
emailService.sendBookingRejectedEmail(
    booking.getUser().getEmail(),
    booking.getBookingCode(),
    rejectionReason
);
```

---

## 🚀 Bước tiếp theo

1. **Reload Maven project** để tải dependency WebSocket
2. **Cấu hình email** trong `application.properties`
3. **Tạo EmailService.java** theo template trên
4. **Thêm WebSocket code** vào `pending-bookings.html`
5. **Test thông báo realtime**

---

## 📝 Ghi chú

- WebSocket sẽ tự động reconnect nếu mất kết nối
- Email cần App Password nếu dùng Gmail (không dùng password thường)
- Thông báo browser cần user cho phép
- Có thể tắt auto-reload trong `pending-bookings.html` nếu đã có WebSocket
