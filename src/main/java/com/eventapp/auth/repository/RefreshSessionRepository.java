package com.eventapp.auth.repository;
import com.eventapp.auth.entity.RefreshSession; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RefreshSessionRepository extends JpaRepository<RefreshSession,UUID>{ Optional<RefreshSession> findByTokenHash(String hash); List<RefreshSession> findByUserIdAndRevokedAtIsNull(UUID userId); }
