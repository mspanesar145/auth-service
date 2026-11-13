package com.eventapp.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Generic SMS gateway adapter. Configure an endpoint compatible with {to, message} JSON.
 */
@Service
@Profile("prod")
public class HttpSmsOtpDeliveryService implements OtpDeliveryService {
    private final RestClient client;
    private final String endpoint;
    private final String apiKey;

    public HttpSmsOtpDeliveryService(@Value("${sms.gateway-url}") String endpoint, @Value("${sms.api-key}") String apiKey) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.client = RestClient.create();
    }

    @Override
    public void send(String phone, String otp) {
        client.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + apiKey).body(Map.of("to", phone, "message", "Your verification code is " + otp)).retrieve().toBodilessEntity();
    }
}
