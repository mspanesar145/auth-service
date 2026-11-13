package com.eventapp.auth.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="auth_users", uniqueConstraints=@UniqueConstraint(name="uk_auth_phone",columnNames="phone_number"))
public class AuthUser {
 @Id private UUID id; @Column(name="phone_number",nullable=false,length=20) private String phoneNumber;
 @Column(name="phone_verified",nullable=false) private boolean phoneVerified;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 protected AuthUser() {}
 public AuthUser(String phone){id=UUID.randomUUID();phoneNumber=phone;status=Status.PENDING_VERIFICATION;createdAt=Instant.now();}
 public UUID getId(){return id;} public String getPhoneNumber(){return phoneNumber;} public boolean isPhoneVerified(){return phoneVerified;} public void setPhoneVerified(boolean v){phoneVerified=v;} public Status getStatus(){return status;} public void setStatus(Status s){status=s;}
 public enum Status { PENDING_VERIFICATION, ACTIVE, DISABLED, DELETION_PENDING, DELETED }
}
