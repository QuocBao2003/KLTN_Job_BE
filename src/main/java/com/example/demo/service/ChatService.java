package com.example.demo.service;

import com.example.demo.domain.Job;

import com.example.demo.domain.Skill;
import com.example.demo.dto.response.chatai.ChatResponse;
import com.example.demo.repository.JobRepository;
import com.example.demo.util.Enum.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Bạn là trợ lý AI tìm việc thông minh của Vietnam. Bạn thân thiện, nhiệt tình và luôn sẵn sàng giúp đỡ.
        
        === QUY TẮC TRẢ LỜI ===
        
        1. KHI NGƯỜI DÙNG CHAT BÌNH THƯỜNG (chào hỏi, hỏi thông tin chung, tâm sự):
           - Trả lời tự nhiên, thân thiện như một người bạn
           - KHÔNG trả về JSON
           - Có thể giới thiệu dịch vụ tìm việc nếu phù hợp
           
           Ví dụ:
           - "Xin chào" → "Chào bạn! 👋 Mình là trợ lý AI tìm việc. Bạn đang tìm công việc gì không?"
           - "Bạn là ai?" → "Mình là trợ lý AI giúp bạn tìm việc làm IT tại Việt Nam. Bạn cần tìm vị trí nào không?"
           - "Hôm nay thế nào?" → "Cảm ơn bạn! Mình sẵn sàng giúp bạn tìm công việc mơ ước. Bạn quan tâm đến vị trí nào?"
        
        2. KHI NGƯỜI DÙNG MUỐN TÌM VIỆC:
           Các từ khóa trigger: tìm việc, job, công việc, tuyển dụng, hiring, vị trí, tìm kiếm
           
           → Phân tích yêu cầu và trả về JSON format:
           {
               "action": "search_jobs",
               "locations": ["HANOI"],
               "skills": ["Backend Developer"],
               "professions": ["Software Engineering"]
           }
        
        === HƯỚNG DẪN PHÂN TÍCH CHI TIẾT ===
        
        A. PROFESSIONS (Ngành nghề - JobProfession):
           Hệ thống có 4 ngành chính, PHẢI map chính xác:
           
           1. "Software Engineering" - Lập trình phần mềm
              Từ khóa: lập trình, developer, dev, coding, phần mềm, software
           
           2. "Software Testing" - Kiểm thử phần mềm  
              Từ khóa: test, tester, testing, kiểm thử, QA, quality assurance
           
           3. "Data Science" - Khoa học dữ liệu
              Từ khóa: data, dữ liệu, phân tích, analyst, scientist, engineer data
           
           4. "Information Security" - An ninh thông tin
              Từ khóa: security, bảo mật, cyber, IT security, information security
        
        B. SKILLS (Kỹ năng/Vị trí cụ thể):
           Danh sách skills theo từng profession:
           
           Software Engineering:
           - "Backend Developer" (backend, back-end, BE, server-side)
           - "Front End Developer" (frontend, front-end, FE, giao diện)
           - "Fullstack Developer" (fullstack, full-stack, full stack)
           - "Mobile Developer" (mobile, android, ios, flutter, react native)
           - "IT Phần mềm" (IT, phần mềm)
           
           Software Testing:
           - "Automation Tester" (automation, tự động hóa)
           - "Software Tester" (tester, kiểm thử)
           - "Manual Tester" (manual, thủ công)
           - "QA Engineer" (QA, quality)
           
           Data Science:
           - "Data Engineer" (data engineer, kỹ sư dữ liệu)
           - "Data Analyst" (data analyst, phân tích dữ liệu)
           - "Data Scientist" (data scientist, khoa học dữ liệu)
           
           Information Security:
           - "Chuyên viên IT Security"
           - "Chuyên viên Cyber Security"
           - "Mã hóa và bảo mật dữ liệu"
           - "Kỹ thuật IT"
        
        C. LOCATIONS (Địa điểm):
           Các tỉnh/thành phố chính:
           
           * Hồ Chí Minh: HCM, Hồ Chí Minh, Ho Chi Minh, Sài Gòn, Saigon, 
             TP.HCM, TPHCM, TP HCM, tp hcm, hcm, Thành phố Hồ Chí Minh
             → "HOCHIMINH"
           
           * Hà Nội: Hà Nội, Hanoi, HN, hà nội, thủ đô
             → "HANOI"
           
           * Đà Nẵng: Đà Nẵng, Da Nang, Danang, DN
             → "DANANG"
           
           * Huế: Huế, Hue
             → "HUE"
           
           * Cần Thơ: Cần Thơ, Can Tho
             → "CANTHO"
           
           * Hải Phòng: Hải Phòng, Hai Phong
             → "HAIPHONG"
           
           * Bình Dương: Bình Dương, Binh Duong
             → "BINHDUONG"
           
           * Đồng Nai: Đồng Nai, Dong Nai
             → "DONGNAI"
           
           * Các tỉnh khác: Viết hoa, bỏ dấu, bỏ khoảng trắng
             VD: "Quảng Nam" → "QUANGNAM"
        
        === VÍ DỤ PHÂN TÍCH THỰC TẾ ===
        
        Input: "Tìm việc backend ở Hà Nội"
        Output: {
            "action": "search_jobs",
            "locations": ["HANOI"],
            "skills": ["Backend Developer"],
            "professions": ["Software Engineering"]
        }
        
        Input: "Công việc data scientist HCM"
        Output: {
            "action": "search_jobs",
            "locations": ["HOCHIMINH"],
            "skills": ["Data Scientist"],
            "professions": ["Data Science"]
        }
        
        Input: "Tìm việc làm có vị trí data science HCM"
        Output: {
            "action": "search_jobs",
            "locations": ["HOCHIMINH"],
            "skills": [],
            "professions": ["Data Science"]
        }
        
        Input: "Job frontend developer Đà Nẵng"
        Output: {
            "action": "search_jobs",
            "locations": ["DANANG"],
            "skills": ["Front End Developer"],
            "professions": ["Software Engineering"]
        }
        
        Input: "Tuyển dụng QA tester Sài Gòn"
        Output: {
            "action": "search_jobs",
            "locations": ["HOCHIMINH"],
            "skills": ["Software Tester"],
            "professions": ["Software Testing"]
        }
        
        Input: "Tìm việc automation test tại Hải Phòng"
        Output: {
            "action": "search_jobs",
            "locations": ["HAIPHONG"],
            "skills": ["Automation Tester"],
            "professions": ["Software Testing"]
        }
        
        Input: "Việc làm mobile developer"
        Output: {
            "action": "search_jobs",
            "locations": [],
            "skills": ["Mobile Developer"],
            "professions": ["Software Engineering"]
        }
        
        Input: "Công việc bảo mật thông tin TP HCM"
        Output: {
            "action": "search_jobs",
            "locations": ["HOCHIMINH"],
            "skills": [],
            "professions": ["Information Security"]
        }
        
        Input: "Tìm job fullstack"
        Output: {
            "action": "search_jobs",
            "locations": [],
            "skills": ["Fullstack Developer"],
            "professions": ["Software Engineering"]
        }
        
        Input: "Data analyst Cần Thơ"
        Output: {
            "action": "search_jobs",
            "locations": ["CANTHO"],
            "skills": ["Data Analyst"],
            "professions": ["Data Science"]
        }
        
        Input: "Tìm việc IT Bình Dương"
        Output: {
            "action": "search_jobs",
            "locations": ["BINHDUONG"],
            "skills": ["IT Phần mềm"],
            "professions": ["Software Engineering"]
        }
        
        Input: "Cyber security Hà Nội"
        Output: {
            "action": "search_jobs",
            "locations": ["HANOI"],
            "skills": ["Chuyên viên Cyber Security"],
            "professions": ["Information Security"]
        }
        
        === QUY TẮC LOGIC ===
        
        1. Ưu tiên map SKILLS trước (chi tiết hơn):
           - Nếu có "backend" → skills: ["Backend Developer"] + professions: ["Software Engineering"]
           - Nếu có "data analyst" → skills: ["Data Analyst"] + professions: ["Data Science"]
        
        2. Nếu chỉ nói chung:
           - "Tìm việc lập trình" → skills: [], professions: ["Software Engineering"]
           - "Việc làm data" → skills: [], professions: ["Data Science"]
        
        3. Location:
           - PHẢI đọc kỹ toàn bộ câu
           - HCM/Sài Gòn/TP.HCM → LUÔN là "HOCHIMINH"
           - Nếu không nhắc địa điểm → locations: []
        
        4. Nếu không rõ ràng:
           - Trả JSON với mảng rỗng thay vì đoán
           - Backend hệ thống sẽ tự search tất cả
        
        === LƯU Ý QUAN TRỌNG ===
        - Chỉ trả JSON khi người dùng muốn TÌM VIỆC
        - Chat thường (xin chào, hỏi han) → KHÔNG trả JSON
        - PHẢI match chính xác tên skills và professions trong database
        - Professions chỉ có 4 giá trị: Software Engineering, Software Testing, Data Science, Information Security
        - Skills phải khớp chính xác với danh sách đã cho
        - Location phải viết hoa chuẩn (HANOI, HOCHIMINH, DANANG...)
        """;

        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .call()
                .content();
        System.out.println("=== AI Response ===");
        System.out.println(aiResponse);
        System.out.println("==================");
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
        SearchCriteria criteria = parseSearchCriteria(aiResponse);

        List<Job> jobs = searchJobsByCriteria(criteria);

        if (jobs.isEmpty()) {
            return ChatResponse.builder()
                    .type("text")
                    .message("Không tìm thấy công việc phù hợp. Hãy thử với từ khóa khác 😊")
                    .build();
        }

        List<ChatResponse.JobSuggestion> suggestions = jobs.stream()
                .map(this::mapToJobSuggestion)
                .toList();

        String message = buildSearchResultMessage(criteria, suggestions.size());

        return ChatResponse.builder()
                .type("jobs")
                .message(message)
                .jobs(suggestions)
                .build();
    }
    private String normalizeSkillName(String skill) {
        if (skill == null) return "";

        String normalized = skill.trim().toLowerCase();

        // Map các variant về tên chuẩn trong DB
        Map<String, String> skillMap = Map.ofEntries(
                // Software Engineering
                Map.entry("backend", "Backend Developer"),
                Map.entry("back-end", "Backend Developer"),
                Map.entry("backend developer", "Backend Developer"),

                Map.entry("frontend", "Front End Developer"),
                Map.entry("front-end", "Front End Developer"),
                Map.entry("front end developer", "Front End Developer"),

                Map.entry("fullstack", "Fullstack Developer"),
                Map.entry("full-stack", "Fullstack Developer"),
                Map.entry("fullstack developer", "Fullstack Developer"),

                Map.entry("mobile", "Mobile Developer"),
                Map.entry("mobile developer", "Mobile Developer"),

                // Software Testing
                Map.entry("automation tester", "Automation Tester"),
                Map.entry("software tester", "Software Tester"),
                Map.entry("manual tester", "Manual Tester"),
                Map.entry("qa engineer", "QA Engineer"),
                Map.entry("qa", "QA Engineer"),

                // Data Science
                Map.entry("data engineer", "Data Engineer"),
                Map.entry("data analyst", "Data Analyst"),
                Map.entry("data scientist", "Data Scientist")
        );

        return skillMap.getOrDefault(normalized, skill);
    }

    // ✅ Thêm method normalize profession
    private String normalizeProfessionName(String profession) {
        if (profession == null) return "";

        String normalized = profession.trim().toLowerCase();

        Map<String, String> professionMap = Map.ofEntries(
                Map.entry("software engineering", "Software Engineering"),
                Map.entry("lập trình", "Software Engineering"),
                Map.entry("phần mềm", "Software Engineering"),

                Map.entry("software testing", "Software Testing"),
                Map.entry("kiểm thử", "Software Testing"),
                Map.entry("testing", "Software Testing"),

                Map.entry("data science", "Data Science"),
                Map.entry("dữ liệu", "Data Science"),
                Map.entry("data", "Data Science"),

                Map.entry("information security", "Information Security"),
                Map.entry("bảo mật", "Information Security"),
                Map.entry("security", "Information Security")
        );

        return professionMap.getOrDefault(normalized, profession);
    }
    private SearchCriteria parseSearchCriteria(String jsonResponse) {
        SearchCriteria criteria = new SearchCriteria();

        try {
            // Extract locations array
            List<String> locations = extractJsonArray(jsonResponse, "locations");
            criteria.setLocations(locations);

            // Extract skills array
            List<String> skills = extractJsonArray(jsonResponse, "skills");
            criteria.setSkills(skills);

            // Extract professions array
            List<String> professions = extractJsonArray(jsonResponse, "professions");
            criteria.setProfessions(professions);

        } catch (Exception e) {
            System.err.println("Error parsing search criteria: " + e.getMessage());
        }

        return criteria;
    }

    private List<String> extractJsonArray(String json, String fieldName) {
        try {
            // Pattern để tìm: "fieldName": ["value1", "value2"]
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\\[(.*?)\\]";
            Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = p.matcher(json);

            if (m.find()) {
                String arrayContent = m.group(1).trim();

                // Nếu array rỗng
                if (arrayContent.isEmpty()) {
                    return new ArrayList<>();
                }

                // Split và clean
                return Arrays.stream(arrayContent.split(","))
                        .map(s -> s.trim()
                                .replaceAll("^\"|\"$", "") // Remove quotes
                                .trim())
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error extracting field " + fieldName + ": " + e.getMessage());
        }

        return new ArrayList<>();
    }

    private List<Job> searchJobsByCriteria(SearchCriteria criteria) {
        Set<Job> resultSet = new HashSet<>();

        System.out.println("=== Search Criteria ===");
        System.out.println("Locations: " + criteria.getLocations());
        System.out.println("Skills: " + criteria.getSkills());
        System.out.println("Professions: " + criteria.getProfessions());

        // Tìm theo location
        if (!criteria.getLocations().isEmpty()) {
            criteria.getLocations().stream()
                    .map(this::normalizeLocation)
                    .forEach(loc -> {
                        System.out.println("Searching by location: " + loc);
                        List<Job> jobs = jobRepository.findByLocation(loc);
                        System.out.println("Found " + jobs.size() + " jobs");
                        resultSet.addAll(jobs);
                    });
        }

        // Tìm theo skill
        if (!criteria.getSkills().isEmpty()) {
            criteria.getSkills().stream()
                    .map(this::normalizeSkillName) // ✅ Thêm normalize
                    .forEach(skill -> {
                        System.out.println("Searching by skill: " + skill);
                        List<Job> jobs = jobRepository.findBySkillName(skill);
                        System.out.println("Found " + jobs.size() + " jobs");
                        resultSet.addAll(jobs);
                    });
        }

        // Tìm theo profession
        if (!criteria.getProfessions().isEmpty()) {
            criteria.getProfessions().stream()
                    .map(this::normalizeProfessionName) // ✅ Thêm normalize
                    .forEach(profession -> {
                        System.out.println("Searching by profession: " + profession);
                        List<Job> jobs = jobRepository.findByProfessionName(profession);
                        System.out.println("Found " + jobs.size() + " jobs");
                        resultSet.addAll(jobs);
                    });
        }

        // Nếu không có tiêu chí nào
        if (criteria.isEmpty()) {
            System.out.println("No criteria, returning recent jobs");
            return jobRepository.findTop10ByStatusOrderByCreatedAtDesc(JobStatus.APPROVED);
        }

        System.out.println("Total unique jobs found: " + resultSet.size());

        return resultSet.stream()
                .sorted(Comparator.comparing(Job::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private ChatResponse.JobSuggestion mapToJobSuggestion(Job job) {
        // Fetch skills từ relationship
        List<String> skillNames = job.getSkills() != null
                ? job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return ChatResponse.JobSuggestion.builder()
                .id(job.getId())
                .name(job.getName())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : "Không rõ")
                .location(job.getLocation())
                .logo(job.getCompany() != null ? job.getCompany().getLogo() : "")
                .salary(calculateSalary(job))
                .level(job.getLevel() != null ? job.getLevel().toString() : "")
                .skills(skillNames)
                .build();
    }

    private double calculateSalary(Job job) {
        if (job.getMinSalary() != null && job.getMaxSalary() != null) {
            return (job.getMinSalary() + job.getMaxSalary()) / 2;
        }
        return job.getMinSalary() != null ? job.getMinSalary() : 0;
    }

    private String buildSearchResultMessage(SearchCriteria criteria, int count) {
        List<String> parts = new ArrayList<>();

        if (!criteria.getLocations().isEmpty()) {
            parts.add("tại " + String.join(", ", criteria.getLocations()));
        }
        if (!criteria.getSkills().isEmpty()) {
            parts.add("yêu cầu " + String.join(", ", criteria.getSkills()));
        }
        if (!criteria.getProfessions().isEmpty()) {
            parts.add("ngành " + String.join(", ", criteria.getProfessions()));
        }

        String criteriaText = parts.isEmpty() ? "" : " " + String.join(", ", parts);
        return "✨ Tìm thấy " + count + " công việc" + criteriaText + "!";
    }

    private String normalizeLocation(String location) {
        if (location == null) return "";

        String normalized = location.trim().toLowerCase();

        if (normalized.matches(".*(h[oồ]\\s*ch[ií]\\s*minh|hcm|sài gòn|saigon|tphcm).*")) {
            return "HOCHIMINH";
        }
        if (normalized.matches(".*(hà\\s*nội|hanoi).*")) {
            return "HANOI";
        }
        if (normalized.matches(".*(đà\\s*nẵng|danang).*")) {
            return "DANANG";
        }

        return location.toUpperCase().replaceAll("\\s+", "");
    }

    // ===== CHAT PDF CV =====
    public ChatResponse chatWithFile(MultipartFile file, String message, String conversationId) {

        try {
            String fileContent = extractPdfContent(file);

            String prompt = """
                    Đây là nội dung CV: 
                    %s
                    
                    Hãy phân tích CV và trích xuất:
                    - Kỹ năng chính (skills)
                    - Vị trí mong muốn (professions)
                    - Địa điểm làm việc nếu có (locations)
                    
                    Trả về JSON:
                    {"action": "search_jobs", "locations": [], "skills": ["skill1","skill2"], "professions": ["profession1"]}
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

    // ===== INNER CLASS =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SearchCriteria {
        private List<String> locations = new ArrayList<>();
        private List<String> skills = new ArrayList<>();
        private List<String> professions = new ArrayList<>();

        public boolean isEmpty() {
            return locations.isEmpty() && skills.isEmpty() && professions.isEmpty();
        }
    }
}