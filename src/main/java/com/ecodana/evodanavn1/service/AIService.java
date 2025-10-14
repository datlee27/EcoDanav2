package com.ecodana.evodanavn1.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Service
public class AIService {

    private final WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${ai.service.model}")
    private String aiModel;

    @Value("${ai.service.temperature}")
    private double aiTemperature;

    @Value("${ai.service.max_tokens}")
    private int aiMaxTokens;

    @Value("${ai.service.stream}")
    private boolean aiStream;

    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
    }

    public String getAIResponse(String userMessage) {
        // --- BẮT ĐẦU LỜI NHẮC HỆ THỐNG ---
        String systemContent = """
            Bạn là trợ lý ảo của EvoDana, chuyên cho thuê xe điện. Luôn trả lời bằng tiếng Việt, thân thiện và chính xác.

            **QUY TẮC TUYỆT ĐỐI:**

            1.  **PHÁT HIỆN Ý ĐỊNH TÌM KIẾM (QUAN TRỌNG NHẤT):**
                - Khi người dùng muốn tìm kiếm, lọc, hoặc hỏi về xe với các tiêu chí cụ thể, **CHỈ** trả về một chuỗi JSON duy nhất.
                - **Định dạng JSON:** `{"intent": "search", "filters": {...}}`
                - **Các `filters` hợp lệ:**
                  - `type`: 'ElectricCar' (ô tô điện), 'ElectricMotorcycle' (xe máy điện).
                  - `seats`: số chỗ ngồi (số nguyên).
                  - `price_range`: một mảng hai phần tử `[min, max]`. Nếu chỉ có một giá trị, coi đó là `max`. Ví dụ: "dưới 500k" -> `[0, 500000]`.
                  - `pickup_date`, `return_date`: ngày tháng theo định dạng 'YYYY-MM-DD'.
                - **VÍ DỤ:**
                  - "Tìm xe 4 chỗ" -> `{"intent": "search", "filters": {"seats": 4}}`
                  - "Tìm ô tô điện 7 chỗ, giá dưới 1 triệu" -> `{"intent": "search", "filters": {"type": "ElectricCar", "seats": 7, "price_range": [0, 1000000]}}`

            2.  **TRẢ LỜI CÁC CÂU HỎI KHÁC (NGẮN GỌN):**
                -   **Thủ tục thuê xe?**: Trả lời: "Bạn cần CCCD/Bằng lái xe và đăng ký online."
                -   **Giá/Loại xe?**: Trả lời: "Bạn xem chi tiết tại <a href='/vehicles' class='text-blue-600 underline'>Danh sách xe</a>."
                -   **Hỗ trợ khác?**: Trả lời: "Bạn vui lòng vào <a href='/contact' class='text-blue-600 underline'>trang liên hệ</a> để được hỗ trợ."
            """;

        // --- KẾT THÚC LỜI NHẮC HỆ THỐNG ---

        // Tạo danh sách tin nhắn bao gồm cả lời nhắc hệ thống và tin nhắn của người dùng
        List<Message> messages = Arrays.asList(
                new Message("system", systemContent),
                new Message("user", userMessage)
        );

        AIRequest request = new AIRequest(
                aiModel,
                messages, // Sử dụng danh sách tin nhắn mới
                aiTemperature,
                aiMaxTokens,
                aiStream
        );

        try {
            AIResponse response = webClient.post()
                    .uri(aiServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request), AIRequest.class)
                    .retrieve()
                    .bodyToMono(AIResponse.class)
                    .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String rawContent = response.getChoices().get(0).getMessage().getContent();
                // Thử phân tích phản hồi dưới dạng JSON
                try {
                    JsonNode rootNode = objectMapper.readTree(rawContent);
                    if (rootNode.has("intent") && "search".equals(rootNode.get("intent").asText())) {
                        return buildSearchResponse(rootNode.get("filters"));
                    }
                } catch (Exception e) {
                    // Không phải JSON hoặc JSON không hợp lệ, coi như là văn bản bình thường
                }
                return rawContent; // Trả về nội dung gốc nếu không phải intent tìm kiếm
            } else {
                return "Xin lỗi, tôi không nhận được phản hồi hợp lệ từ AI.";
            }
        } catch (Exception e) {
            // Ghi lại lỗi để gỡ lỗi
            System.err.println("Error calling AI service: " + e.getMessage());
            return "Xin lỗi, đã xảy ra lỗi khi kết nối với dịch vụ AI.";
        }
    }

    private String buildSearchResponse(JsonNode filtersNode) {
        if (filtersNode == null || filtersNode.isEmpty()) {
            return "Được thôi, bạn có thể xem tất cả các xe của chúng tôi tại <a href='/vehicles' class='text-blue-600 underline'>trang danh sách xe</a>.";
        }

        StringJoiner queryParams = new StringJoiner("&");
        StringJoiner description = new StringJoiner(" và ");

        if (filtersNode.has("seats")) {
            int seats = filtersNode.get("seats").asInt();
            queryParams.add("seats=" + seats);
            description.add(seats + " chỗ ngồi");
        }

        if (filtersNode.has("type")) {
            String type = filtersNode.get("type").asText();
            queryParams.add("type=" + type);
            description.add(type.equals("ElectricCar") ? "ô tô điện" : "xe máy điện");
        }

        if (filtersNode.has("price_range")) {
            JsonNode priceNode = filtersNode.get("price_range");
            if (priceNode.isArray() && priceNode.size() > 0) {
                // Lấy giá trị cuối cùng trong mảng làm giá tối đa
                long maxPrice = priceNode.get(priceNode.size() - 1).asLong();
                queryParams.add("budget=" + maxPrice); // Giả sử param là 'budget'
                description.add("giá dưới " + String.format("%,d", maxPrice) + "đ");
            }
        }

        if (filtersNode.has("pickup_date")) {
            String pickupDate = filtersNode.get("pickup_date").asText();
            queryParams.add("pickupDate=" + pickupDate);
            description.add("nhận từ ngày " + pickupDate);
        }

        if (filtersNode.has("return_date")) {
            String returnDate = filtersNode.get("return_date").asText();
            queryParams.add("returnDate=" + returnDate);
            description.add("trả trước ngày " + returnDate);
        }

        String url = "/vehicles?" + queryParams.toString();
        String link = "<a href='" + url + "' class='text-blue-600 underline'>đây</a>";

        return "Đã tìm thấy xe " + description.toString() + ". Xem kết quả tại " + link + ".";
    }


    // --- CÁC LỚP DỮ LIỆU (Request/Response) KHÔNG THAY ĐỔI ---

    private static class AIRequest {
        private String model;
        private List<Message> messages;
        private double temperature;
        @JsonProperty("max_tokens")
        private int maxTokens;
        private boolean stream;

        public AIRequest(String model, List<Message> messages, double temperature, int maxTokens, boolean stream) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.maxTokens = maxTokens;
            this.stream = stream;
        }

        public String getModel() { return model; }
        public List<Message> getMessages() { return messages; }
        public double getTemperature() { return temperature; }
        public int getMaxTokens() { return maxTokens; }
        public boolean isStream() { return stream; }
    }

    private static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    private static class AIResponse {
        private List<Choice> choices;
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
    }

    private static class Choice {
        private Message message;
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }
}
