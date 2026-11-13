package com.eventapp.auth.service;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.springframework.context.annotation.Profile; import org.springframework.stereotype.Service;
@Service @Profile("!prod")
public class LoggingOtpDeliveryService implements OtpDeliveryService {
 private static final Logger log=LoggerFactory.getLogger(LoggingOtpDeliveryService.class);
 public void send(String phone,String otp){log.warn("DEVELOPMENT ONLY OTP for {}: {}. Never enable this delivery implementation in production.",phone,otp);}
}
