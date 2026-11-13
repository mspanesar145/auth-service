package com.eventapp.auth.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="refresh_sessions", indexes=@Index(name="ix_refresh_user",columnList="user_id"))
public class RefreshSession {
 @Id private UUID id; @Column(name="user_id",nullable=false) private UUID userId; @Column(name="token_hash",nullable=false,unique=true,length=128) private String tokenHash;
 @Column(name="device_name") private String deviceName; @Column(name="expires_at",nullable=false) private Instant expiresAt; @Column(name="revoked_at") private Instant revokedAt; @Column(name="created_at",nullable=false) private Instant createdAt;
 protected RefreshSession(){} public RefreshSession(UUID uid,String hash,String device,Instant expiry){id=UUID.randomUUID();userId=uid;tokenHash=hash;deviceName=device;expiresAt=expiry;createdAt=Instant.now();}
 public UUID getUserId(){return userId;} public String getTokenHash(){return tokenHash;} public Instant getExpiresAt(){return expiresAt;} public Instant getRevokedAt(){return revokedAt;} public void revoke(){revokedAt=Instant.now();} public UUID getId(){return id;} public String getDeviceName(){return deviceName;} public Instant getCreatedAt(){return createdAt;}
}
