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
        String systemContent =
            "Bạn là trợ lý ảo của EvoDana, chuyên hỗ trợ về dịch vụ cho thuê xe điện. " +
            "Hãy trả lời thân thiện, chuyên nghiệp và ngắn gọn bằng tiếng Việt.\n\n" +
            "## Quy tắc trả lời:\n" +
            "1. **Phát hiện ý định tìm kiếm xe:** Nếu người dùng muốn tìm xe với các tiêu chí cụ thể (số chỗ, loại xe), hãy trả lời bằng một chuỗi JSON duy nhất, không có văn bản nào khác.\n" +
            "   - **JSON format:** `{\"intent\": \"search\", \"filters\": {\"seats\": <số>, \"type\": \"<loại xe>\"}}`\n" +
            "   - `seats`: số chỗ ngồi (ví dụ: 2, 4, 7).\n" +
            "   - `type`: loại xe. Các giá trị hợp lệ là 'ElectricCar' hoặc 'ElectricMotorcycle'.\n" +
            "   - **Ví dụ 1:** Nếu người dùng nói \"tìm xe 2 chỗ\", bạn chỉ trả về: `{\"intent\": \"search\", \"filters\": {\"seats\": 2}}`\n" +
            "   - **Ví dụ 2:** Nếu người dùng nói \"tôi muốn thuê xe máy điện\", bạn chỉ trả về: `{\"intent\": \"search\", \"filters\": {\"type\": \"ElectricMotorcycle\"}}`\n\n" +
            "2. **Trả lời thông thường:** Đối với các câu hỏi khác, hãy trả lời như bình thường.\n" +
            "   - **Thông tin chính:**\n" +
            "     - Thủ tục: Cần CMND/Bằng lái xe, quy trình online.\n" +
            "     - Giá & Các loại xe (chung chung): Hướng dẫn người dùng xem tại trang <a href='/vehicles' class='text-blue-600 underline'>Danh sách xe</a>.\n" +
            "     - Hỗ trợ thêm: Hướng dẫn người dùng tới <a href='/contact' class='text-blue-600 underline'>trang liên hệ</a>.";

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

        String url = "/vehicles?" + queryParams.toString();
        String link = "<a href='" + url + "' class='text-blue-600 underline'>đây</a>";

        return "Tuyệt vời! Tôi đã tìm thấy các xe có " + description.toString() + ". Bạn có thể xem kết quả tại " + link + ".";
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
