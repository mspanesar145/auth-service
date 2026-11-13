package com.eventapp.auth.service;

public interface OtpDeliveryService {
    void send(String phoneNumber, String otp);
}
