package com.zipsoon.api.domain.user;

import com.zipsoon.api.domain.auth.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private String name;
    private String imageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 새로운 사용자를 생성합니다.
     * 
     * @param email 이메일
     * @param name 이름
     * @param role 역할
     * @return 생성된 사용자
     */
    public static User of(String email, String name, Role role) {
        return User.builder()
            .email(email)
            .name(name)
            .role(role)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}