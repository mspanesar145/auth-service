package com.eventapp.auth.repository;
import com.eventapp.auth.entity.AuthUser; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AuthUserRepository extends JpaRepository<AuthUser,UUID>{ Optional<AuthUser> findByPhoneNumber(String phone); }
