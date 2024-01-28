package com.growmming.gurdening.service;

import com.google.gson.Gson;
import com.growmming.gurdening.domain.Member;
import com.growmming.gurdening.domain.Role;
import com.growmming.gurdening.domain.dto.TokenDTO;
import com.growmming.gurdening.domain.dto.TokenDTO.ServiceToken;
import com.growmming.gurdening.domain.dto.UserInfoDTO;
import com.growmming.gurdening.repository.MemberRepository;
import com.growmming.gurdening.token.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.core.RedisTemplate;

@Service
public class OAuth2Service {
    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleRedirectUri;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    public OAuth2Service(@Value("${google.client-id}") String googleClientId,
                        @Value("${google.client-secret}") String googleClientSecret,
                        @Value("${google.redirect-uri}") String googleRedirectUri,
                        MemberRepository memberRepository,
                        TokenProvider tokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleRedirectUri = googleRedirectUri;
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.redisTemplate = redisTemplate;
    }

    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate(); //  HTTP 요청을 보내기 위한 RestTemplate 객체 생성
        Map<String, String> params = Map.of(
                "code", code,
                "scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
                "client_id", googleClientId,
                "client_secret", googleClientSecret,
                "redirect_uri", googleRedirectUri,
                "grant_type", "authorization_code"
        );

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();

            return gson.fromJson(json, TokenDTO.GoogleToken.class)
                    .getGoogleAccessToken(); // 성공인 경우, 응답 바디에서 파싱하여 액세스 토큰 반환
        }

        throw new RuntimeException("구글 엑세스 토큰을 가져오는데 실패했습니다.");
    }

    public ServiceToken loginOrSignUp(String googleAccessToken) {
        UserInfoDTO userInfoDTO = getUserInfoDTO(googleAccessToken);

        if (!userInfoDTO.getVerifiedEmail()) {
            throw new RuntimeException("이메일 인증이 되지 않은 유저입니다.");
        }

        Member member = memberRepository.findByEmail(userInfoDTO.getEmail()).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email(userInfoDTO.getEmail())
                        .name(userInfoDTO.getName())
                        .profileImage(userInfoDTO.getPictureUrl())
                        .role(Role.USER)
                        .build()) // 등록되어 있지 않으면 새롭게 생성하고 정보 가져옴
        );

        TokenDTO.ServiceToken tokenDTO = tokenProvider.createToken(member);

        Long expiration = tokenProvider.getExpiration(tokenDTO.getRefreshToken());

        redisTemplate.opsForValue().set(tokenDTO.getRefreshToken(), "refreshToken", expiration, TimeUnit.MILLISECONDS);
        return tokenDTO;
    }

    public UserInfoDTO getUserInfoDTO(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken; // 사용자 정보를 가져오기 위한 URL 설정

        HttpHeaders headers = new HttpHeaders(); // HTTP 요청의 헤더를 설정하는 객체
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url)); // HTTP GET 요청 객체
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class); // HTTP 요청을 보내고, 그 응답을 ResponseEntity 객체에 저장

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();
            return gson.fromJson(json, UserInfoDTO.class);
        }

        throw new RuntimeException("유저 정보를 가져오는데 실패했습니다.");
    }

    public TokenDTO.ServiceToken refresh(HttpServletRequest request, TokenDTO.ServiceToken dto) {
        String refreshToken = dto.getRefreshToken();

        // refreshToken이 유효하지 않은 경우 예외 발생
        String isValidate = (String)redisTemplate.opsForValue().get(refreshToken);
        if (ObjectUtils.isEmpty(isValidate)) throw new RuntimeException("401 UNAUTHORIZED, 리프레시 토큰이 유효하지 않습니다.");

        // AccessToken 재발급
        return tokenProvider.createAccessTokenByRefreshToken(request, refreshToken);
    }

    public void logout(HttpServletRequest request, @RequestBody TokenDTO.ServiceToken dto, Principal principal) {
        String accessToken = tokenProvider.resolveToken(request);
        Long expiration = tokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
        redisTemplate.delete(dto.getRefreshToken());
    }

}
