package com.morak.back.auth.application;

import com.morak.back.auth.domain.Member2;
import com.morak.back.auth.domain.Member2Repository;
import com.morak.back.auth.application.dto.OAuthMemberInfoResponse;
import com.morak.back.auth.application.dto.OAuthAccessTokenResponse;
import com.morak.back.auth.ui.dto.SigninRequest;
import com.morak.back.auth.ui.dto.SigninResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional
public class OAuthService {

    private static final WebClient WEB_CLIENT = WebClient.create();

    private final Member2Repository memberRepository;
    private final OAuthClient oAuthClient;

    public OAuthService(Member2Repository memberRepository, OAuthClient oAuthClient) {
        this.memberRepository = memberRepository;
        this.oAuthClient = oAuthClient;
    }

    public SigninResponse signin(SigninRequest request) {

        // 요청1 - code를 이용해 깃허브로부터 access_token을 받아온다.
//        OAuthAccessTokenResponse tokenResponse = getOAuthTokenResponse(request);
        OAuthAccessTokenResponse tokenResponse = oAuthClient.getAccessToken(request.getCode());
        System.out.println("tokenResponse.getAccessToken() = " + tokenResponse.getAccessToken());

        // 요청2 - access_token을 이용해 깃허브로부터 사용자 정보를 받아온다.
//        OAuthMemberInfoResponse memberResponse = getOAuthMemberResponse(tokenResponse);
        OAuthMemberInfoResponse memberResponse = oAuthClient.getMemberInfo(tokenResponse.getAccessToken());
        System.out.println("memberResponse.getName() = " + memberResponse.getName());

        // 멤버가 있으면 불러오고 없으면 저장 후 불러온다.
        Member2 member = memberRepository.findByOauthId(memberResponse.getOauthId())
                .orElse(memberRepository.save(new Member2(
                        null,
                        memberResponse.getOauthId(),
                        memberResponse.getName(),
                        memberResponse.getProfile_url()
                )));

        // 멤버를 이용해 토큰을 발급한다.

        return null;
    }

    private OAuthAccessTokenResponse getOAuthTokenResponse(SigninRequest request) {
        // TODO: 2022/07/12 예외 처리 필요!!
        return WEB_CLIENT.post()
                .uri("https://github.com/login/oauth/access_token")
                .headers(header -> {
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(toGithubAccessTokenRequest(request.getCode()))
                .retrieve()
                .bodyToMono(OAuthAccessTokenResponse.class)
                .block();
    }

    // TODO: 2022/07/12 toOAuthTokenRequest
    private MultiValueMap<String, String> toGithubAccessTokenRequest(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "f67a30d27afefe8b241f");
        formData.add("client_secret", "51dfab631c2981fd7daa556a11aa19449b8bf583");
        formData.add("code", code);
        return formData;
    }

    private OAuthMemberInfoResponse getOAuthMemberResponse(OAuthAccessTokenResponse tokenResponse) {
        try {
            return WEB_CLIENT.get()
                    .uri("https://api.github.com/user")
                    .headers(header -> header.setBearerAuth(tokenResponse.getAccessToken()))
                    .retrieve()
                    .bodyToMono(OAuthMemberInfoResponse.class)
                    .block();
        } catch (RuntimeException e) {
            // TODO: 2022/07/12 convert to custom exception
            throw new IllegalArgumentException();
        }
    }
}
