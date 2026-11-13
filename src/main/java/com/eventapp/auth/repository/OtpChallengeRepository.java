package com.eventapp.auth.repository;
import com.eventapp.auth.entity.OtpChallenge; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge,UUID>{ Optional<OtpChallenge> findTopByPhoneNumberAndConsumedFalseOrderByCreatedAtDesc(String phone); }
