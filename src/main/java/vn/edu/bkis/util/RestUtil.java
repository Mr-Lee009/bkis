package vn.edu.bkis.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Map;

public class RestUtil {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * Goi API create payment cua provider voi payload JSON.
     *
     * @param endpoint endpoint create payment
     * @param payload  payload request
     * @return response body dang map
     */
    public static Map<String, Object> postJson(String endpoint, Map<String, Object> payload) {
        // Step 1: chuyen payload sang JSON va set header application/json.
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = objectMapper.writeValueAsString(payload);

            // Step 2: goi HTTP POST sang endpoint MoMo.
            ResponseEntity<String> response = restTemplate.postForEntity(URI.create(endpoint),
                    new HttpEntity<>(json, headers), String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("MoMo returned empty response.");
            }
            return objectMapper.readValue(body, MAP_TYPE);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Không thể gọi MoMo create payment API.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể parse phản hồi MoMo.", ex);
        }
    }

    /**
     * Parse metadata tu config_json.
     *
     * @param json chuoi json metadata
     * @return map metadata, neu json null thi map rong
     */
    public static Map<String, Object> parseMetadata(String json) {
        // Step 1: doc json cau hinh custom cua gateway.
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
