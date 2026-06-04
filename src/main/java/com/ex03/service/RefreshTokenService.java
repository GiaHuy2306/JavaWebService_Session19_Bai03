package com.ex03.service;

import com.ex03.entity.RefreshToken;
import com.ex03.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration-days}")
    private long expirationDays;

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String deviceId) {
        String resolvedDeviceId = (deviceId != null && !deviceId.isBlank())
                ? deviceId
                : UUID.randomUUID().toString();

        // Xóa token cũ của thiết bị này nếu có
        refreshTokenRepository.findByUserIdAndDeviceId(userId, resolvedDeviceId)
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setDeviceId(resolvedDeviceId);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plus(expirationDays, ChronoUnit.DAYS));

        return refreshTokenRepository.save(rt);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn");
        }
        return token;
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));
    }

    @Transactional
    public void logoutByDevice(Long userId, String deviceId) {
        refreshTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void logoutAllDevices(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }
}
