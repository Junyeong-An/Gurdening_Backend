package com.growmming.gurdening.controller;

import com.growmming.gurdening.domain.dto.TokenDTO;
import com.growmming.gurdening.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/oauth2")
@RestController
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oAuth2Service;

    @GetMapping("callback/google")
    public TokenDTO.ServiceToken googleCallback(@RequestParam(name = "code") String code) {
        String googleAccessToken = oAuth2Service.getGoogleAccessToken(code);
        return loginOrSignUp(googleAccessToken);
    }

    public TokenDTO.ServiceToken loginOrSignUp(String googleAccessToken) {
        return oAuth2Service.loginOrSignUp(googleAccessToken);
    }
}
