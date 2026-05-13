package com.example.billreminder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${fonnte.api.url}")
    private String fonnteApiUrl;

    @Value("${fonnte.api.token}")
    private String fonnteApiToken;

    private final RestTemplate restTemplate;

    public WhatsAppService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Mengirim pesan WhatsApp melalui Fonnte API
     *
     * @param phoneNumber nomor telepon tujuan (format: 08xxx atau 628xxx)
     * @param message     isi pesan yang akan dikirim
     */
    public void sendMessage(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("Nomor WhatsApp tidak tersedia, notifikasi WhatsApp tidak dikirim.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", fonnteApiToken);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("target", phoneNumber);
            body.add("message", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    fonnteApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notifikasi WhatsApp berhasil dikirim ke {}", phoneNumber);
            } else {
                log.warn("Gagal mengirim notifikasi WhatsApp ke {}. Response: {}", phoneNumber, response.getBody());
            }
        } catch (Exception e) {
            log.error("Error saat mengirim notifikasi WhatsApp ke {}: {}", phoneNumber, e.getMessage());
        }
    }
}
