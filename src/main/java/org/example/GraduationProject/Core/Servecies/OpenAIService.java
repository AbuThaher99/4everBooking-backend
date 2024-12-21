package org.example.GraduationProject.Core.Servecies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model.name}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatbotResponse(String userInput) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", modelName);
            requestBody.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "user").put("content", userInput))
            );

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choicesArray.getJSONObject(0);
            JSONObject messageObject = firstChoice.getJSONObject("message");

            return messageObject.getString("content");

        } catch (Exception e) {
            return "حدث خطأ أثناء الاتصال بخدمة OpenAI: " + e.getMessage();
        }
    }
}
