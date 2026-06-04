package com.ex03.repository;

import com.ex03.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    @Transactional
    void deleteAllByUserId(Long userId);

    @Transactional
    void deleteAllByExpiryDateBefore(Instant now);
}
