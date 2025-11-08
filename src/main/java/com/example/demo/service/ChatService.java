package com.example.demo.service;

import com.example.demo.domain.Job;

import com.example.demo.dto.response.chatai.ChatResponse;
import com.example.demo.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final JobRepository jobRepository;
    private final ChatMemoryRepository chatMemoryRepository;

    // ===== CHAT TEXT =====
    public ChatResponse chat(String message, String conversationId) {

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(30)
                .build();

        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();

        String systemPrompt = """
                Bạn là trợ lý AI tìm việc thông minh của Vietnam.
                Nếu người dùng nhắc tên công việc, vị trí, hoặc skill, trả về JSON:
                {"action": "search_jobs", "keywords": ["keyword1","keyword2"]}

                Không trả JSON nếu không nhắc tìm việc.
                Trả lời ngắn gọn, thân thiện nếu chat bình thường.
                """;

        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .call()
                .content();

        if (aiResponse.contains("\"action\"") && aiResponse.contains("search_jobs")) {
            return handleJobSearch(aiResponse);
        }

        return ChatResponse.builder()
                .type("text")
                .message(aiResponse)
                .build();
    }


    // ===== SEARCH JOBS =====
    private ChatResponse handleJobSearch(String aiResponse) {
        List<String> keywords = extractKeywords(aiResponse);

        List<Job> jobs = searchJobsByKeywords(keywords);

        if (jobs.isEmpty()) {
            return ChatResponse.builder()
                    .type("text")
                    .message("Không tìm thấy công việc phù hợp. Hãy mô tả rõ hơn 😊")
                    .build();
        }

        List<ChatResponse.JobSuggestion> suggestions = jobs.stream()
                .map(j -> ChatResponse.JobSuggestion.builder()
                        .id(j.getId())
                        .name(j.getName())
                        .companyName(j.getCompany() != null ? j.getCompany().getName() : "Không rõ")
                        .location(j.getLocation())
                        .logo(j.getCompany() != null ? j.getCompany().getLogo() : "")
                        .build())
                .toList();

        return ChatResponse.builder()
                .type("jobs")
                .message("✨ Tìm thấy " + suggestions.size() + " công việc phù hợp!")
                .jobs(suggestions)
                .build();
    }

    private List<String> extractKeywords(String jsonResponse) {
        try {
            int start = jsonResponse.indexOf("[");
            int end = jsonResponse.indexOf("]");
            if (start == -1 || end == -1) return List.of();

            return List.of(jsonResponse.substring(start + 1, end)
                    .replace("\"", "")
                    .split(","));
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Job> searchJobsByKeywords(List<String> keywords) {
        return keywords.stream()
                .map(this::normalizeKeyword) // Chuẩn hóa keyword
                .flatMap(k -> jobRepository
                        .searchJobs(k)
                        .stream())
                .distinct()
                .limit(5)
                .toList();
    }
    private String normalizeKeyword(String keyword) {
        if (keyword == null) return "";

        String normalized = keyword.trim().toLowerCase();

        // Chuẩn hóa tên thành phố
        if (normalized.matches(".*h[oồ]\\s*ch[ií]\\s*minh.*") ||
                normalized.equals("hochiminh") ||
                normalized.equals("hcm") ||
                normalized.contains("Hồ Chí Minh") ||
                normalized.contains("tphcm")) {
            return "HOCHIMINH";
        }

        if (normalized.matches(".*[dđ][àa]\\s*n[ăa]ng.*") ||
                normalized.equals("danang") ||
                normalized.equals("Đà Nẵng"))
        {
            return "DANANG";
        }

        if (normalized.matches(".*h[àa]\\s*n[ộo]i.*") ||
                normalized.equals("hanoi")  ||
                normalized.equals("Hà Nội"))
                {
            return "HANOI";
        }

        // Giữ nguyên keyword nếu không phải địa điểm
        return keyword.trim();
    }
    // ===== CHAT PDF CV =====
    public ChatResponse chatWithFile(MultipartFile file, String message, String conversationId) {

        try {
            String fileContent = extractPdfContent(file);

            String prompt = """
                    Đây là nội dung CV: 
                    %s
                    Hãy phân tích CV và trích xuất kỹ năng chính, trả về JSON:
                    {"action": "search_jobs", "keywords": ["skill1","skill2"]}
                    """.formatted(fileContent);

            return chat(prompt, conversationId);

        } catch (Exception e) {
            return ChatResponse.builder()
                    .type("text")
                    .message("Không thể đọc file PDF: " + e.getMessage())
                    .build();
        }
    }

    private String extractPdfContent(MultipartFile file) throws IOException {

        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader reader = new PagePdfDocumentReader(file.getResource(), config);

        return reader.get().stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n"));
    }
}