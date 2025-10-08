package com.ecodana.evodanavn1.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AIService {

    /**
     * Lấy phản hồi từ AI.
     * TODO: Đây là nơi để tích hợp API của một dịch vụ AI thực sự (ví dụ: OpenAI, Google Gemini).
     * Bạn sẽ cần gọi API của họ bằng cách sử dụng RestTemplate hoặc WebClient.
     *
     * @param userMessage Tin nhắn từ người dùng.
     * @return Phản hồi do AI tạo ra.
     */
    public String getAIResponse(String userMessage) {
        // --- BẮT ĐẦU PHẦN MÔ PHỎNG ---
        // Logic AI dựa trên từ khóa (phiên bản nâng cao hơn)
        String lowerCaseMessage = userMessage.toLowerCase();

        if (lowerCaseMessage.contains("thủ tục") || lowerCaseMessage.contains("giấy tờ")) {
            return "Để thuê xe, bạn cần chuẩn bị CMND/CCCD và Bằng lái xe (nếu loại xe yêu cầu). Quy trình rất đơn giản, chỉ cần chọn xe, đặt lịch và xác nhận thông tin là xong!";
        } else if (lowerCaseMessage.contains("giá") || lowerCaseMessage.contains("chi phí")) {
            return "Giá thuê xe phụ thuộc vào loại xe và thời gian thuê. Bạn có thể xem chi tiết giá của từng xe tại trang <a href='/vehicles' class='text-blue-600 underline'>Danh sách xe</a>.";
        } else if (lowerCaseMessage.contains("hướng dẫn") || lowerCaseMessage.contains("cách thuê")) {
            return "Rất đơn giản! Bạn chỉ cần: 1. Tìm xe bạn thích. 2. Chọn ngày nhận và trả xe. 3. Đặt cọc và chờ xác nhận từ chủ xe. Mọi thứ đều có thể thực hiện online!";
        } else if (lowerCaseMessage.contains("loại xe")) {
            return "Chúng tôi có cả ô tô điện và xe máy điện. Bạn có thể xem tất cả các mẫu xe hiện có tại trang <a href='/vehicles' class='text-blue-600 underline'>Danh sách xe</a> của chúng tôi.";
        } else if (lowerCaseMessage.contains("chào") || lowerCaseMessage.contains("hello")) {
            String[] greetings = {"Chào bạn, tôi có thể giúp gì cho bạn hôm nay?", "Xin chào! Bạn cần hỗ trợ về vấn đề gì ạ?", "EvoDana xin chào, bạn có câu hỏi gì cho tôi không?"};
            return greetings[new Random().nextInt(greetings.length)];
        } else if (lowerCaseMessage.contains("cảm ơn")) {
            return "Rất vui được hỗ trợ bạn! Nếu cần gì thêm, đừng ngần ngại hỏi nhé.";
        }

        // Phản hồi mặc định nếu không có từ khóa nào khớp
        String[] defaultResponses = {
                "Xin lỗi, tôi chưa được huấn luyện về vấn đề này. Bạn có thể hỏi khác đi được không?",
                "Tôi chưa hiểu rõ câu hỏi của bạn. Bạn có thể cung cấp thêm chi tiết không?",
                "Để được hỗ trợ tốt nhất, bạn có thể hỏi về 'thủ tục thuê xe', 'giá thuê', hoặc 'hướng dẫn'."
        };
        return defaultResponses[new Random().nextInt(defaultResponses.length)];
        // --- KẾT THÚC PHẦN MÔ PHỎNG ---
    }
}