package com.morak.back.auth.application;

import com.morak.back.auth.application.dto.OAuthAccessTokenRequest;
import com.morak.back.auth.application.dto.OAuthAccessTokenResponse;
import com.morak.back.auth.application.dto.OAuthMemberInfoResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GithubOAuthClient implements OAuthClient {

    private static final String CLIENT_ID = "f67a30d27afefe8b241f";
    private static final String CLIENT_SECRET = "51dfab631c2981fd7daa556a11aa19449b8bf583";

    private static final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthAccessTokenResponse getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

        HttpEntity<OAuthAccessTokenRequest> request = new HttpEntity<>(
                new OAuthAccessTokenRequest(CLIENT_ID, CLIENT_SECRET, code),
                headers
        );

        return restTemplate.postForObject(
                "https://github.com/login/oauth/access_token",
                request,
                OAuthAccessTokenResponse.class
                );
    }

    @Override
    public OAuthMemberInfoResponse getMemberInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.getForObject(
                "https://api.github.com/user",
                OAuthMemberInfoResponse.class);
    }
}
