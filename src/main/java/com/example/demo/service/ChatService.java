package com.example.demo.service;

import com.example.demo.dto.request.BillItem;
import com.example.demo.dto.request.ChatRequest;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;

import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ChatService {
    private final ChatClient chatClient;

    private final ChatMemoryRepository chatMemoryRepository;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemoryRepository chatMemoryRepository) {

        ChatMemory chatMemory= MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(30)
                .build();
        this.chatMemoryRepository = chatMemoryRepository;
        chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(ChatRequest chatRequest) {

        String conversationId="conversation1";
        SystemMessage systemMessage = new SystemMessage("""
                You are QuocBao
                You should response with a super funny voice
                """);
        UserMessage userMessage = new UserMessage((chatRequest.message()));
        Prompt prompt = new Prompt(systemMessage, userMessage);
//        trả về dữ liệu có cấu trúc
       return chatClient.prompt(prompt)
               .advisors(advisorSpec -> advisorSpec.param(ChatMemory.DEFAULT_CONVERSATION_ID,conversationId))
               .call().content();

    }

    public List<BillItem> chatWithImage(MultipartFile file, String message){
        Media media =Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0D)
                .build();
        return  chatClient.prompt().options(chatOptions).user(promptUserSpec ->
                promptUserSpec.media(media).text(message)).call().entity(new ParameterizedTypeReference<List<BillItem>>() {

        });

    }
}
