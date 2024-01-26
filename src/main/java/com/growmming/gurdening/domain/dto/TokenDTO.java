package com.growmming.gurdening.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class TokenDTO {

    @Data
    @Builder
    @AllArgsConstructor
    public static class GoogleToken {
        private String googleAccessToken;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ServiceToken {
        private String accessToken;
        private String refreshToken;
    }
}
