package com.zenfulcode.commercify.commercify.integration.mobilepay;

import com.zenfulcode.commercify.commercify.api.responses.AuthResponse;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
import com.zenfulcode.commercify.commercify.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MobilePayAuthService {
    private final MobilePayService mobilePayService;
    private final Map<String, String> stateToPhoneMap = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper mapper;

    public MobilePayLoginResponse initiateLogin(MobilePayLoginRequest request) {
        String state = UUID.randomUUID().toString();
        stateToPhoneMap.put(state, request.phoneNumber());

        String redirectUrl = mobilePayService.getLoginUrl(request.phoneNumber(),
                request.returnUrl(), state);

        return new MobilePayLoginResponse(redirectUrl, state);
    }

    public AuthResponse handleCallback(String state, String code) {
        String phoneNumber = stateToPhoneMap.remove(state);
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Invalid state parameter");
        }

        MobilePayUserInfo userInfo = mobilePayService.getUserInfo(code);

        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> createUserFromMobilePay(userInfo, phoneNumber));

        String token = jwtService.generateToken(mapper.apply(user));
        return AuthResponse.UserAuthenticated(mapper.apply(user), token,
                jwtService.getExpirationTime());
    }

    private UserEntity createUserFromMobilePay(MobilePayUserInfo info, String phone) {
        UserEntity user = UserEntity.builder()
                .email(info.email())
                .phoneNumber(phone)
                .firstName(info.firstName())
                .lastName(info.lastName())
                .roles(List.of("USER"))
                .build();
        return userRepository.save(user);
    }
}