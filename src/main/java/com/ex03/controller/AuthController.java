// controller/AuthController.java
package com.ex03.controller;


import com.ex03.dto.request.LoginRequest;
import com.ex03.dto.request.LogoutRequest;
import com.ex03.dto.response.LoginResponse;
import com.ex03.entity.RefreshToken;
import com.ex03.entity.User;
import com.ex03.repository.UserRepository;
import com.ex03.service.RefreshTokenService;
import com.ex03.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username đã tồn tại");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Đăng ký thành công");
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = tokenService.generateAccessToken(auth.getName());

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), request.getDeviceId());

        return ResponseEntity.ok(new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                refreshToken.getDeviceId()
        ));
    }

    // Lấy Access Token mới bằng Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        RefreshToken rt = refreshTokenService.findByToken(refreshToken);
        refreshTokenService.verifyExpiration(rt);

        User user = userRepository.findById(rt.getUserId()).orElseThrow();
        String newAccessToken = tokenService.generateAccessToken(user.getUsername());

        return ResponseEntity.ok(new LoginResponse(newAccessToken, rt.getToken(), rt.getDeviceId()));
    }

    // Logout thiết bị hiện tại
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request,
                                    @RequestHeader("Authorization") String authHeader) {
        String username = tokenService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();

        refreshTokenService.logoutByDevice(user.getId(), request.getDeviceId());
        return ResponseEntity.ok("Đăng xuất thiết bị thành công");
    }

    // Logout tất cả thiết bị
    @PostMapping("/logoutAllDevices")
    public ResponseEntity<?> logoutAllDevices(@RequestHeader("Authorization") String authHeader) {
        String username = tokenService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();

        refreshTokenService.logoutAllDevices(user.getId());
        return ResponseEntity.ok("Đã đăng xuất tất cả thiết bị");
    }
}