package com.eventapp.auth.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="otp_challenges")
public class OtpChallenge {
 @Id private UUID id; @Column(name="phone_number",nullable=false) private String phoneNumber; @Column(name="code_hash",nullable=false) private String codeHash;
 @Column(name="expires_at",nullable=false) private Instant expiresAt; @Column(nullable=false) private int attempts; @Column(nullable=false) private boolean consumed; @Column(name="created_at",nullable=false) private Instant createdAt;
 protected OtpChallenge(){}
 public OtpChallenge(String phone,String hash,Instant expiry){id=UUID.randomUUID();phoneNumber=phone;codeHash=hash;expiresAt=expiry;createdAt=Instant.now();}
 public UUID getId(){return id;} public Instant getCreatedAt(){return createdAt;} public String getPhoneNumber(){return phoneNumber;} public String getCodeHash(){return codeHash;} public Instant getExpiresAt(){return expiresAt;} public int getAttempts(){return attempts;} public void incrementAttempts(){attempts++;} public boolean isConsumed(){return consumed;} public void consume(){consumed=true;}
}
