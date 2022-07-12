package com.morak.back.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.morak.back.auth.ui.dto.SigninRequest;
import com.morak.back.auth.ui.dto.SigninResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OAuthServiceTest {

    private static final String CODE = "36db09f39c23d2e7cb5e";

    @Autowired
    private OAuthService oAuthService;

    @Test
    public void 사용자_code를_이용해_모락_엑세스_토큰을_발급한다() {
        // given
        SigninRequest request = new SigninRequest(CODE);

        // when
        SigninResponse response = oAuthService.signin(request);

        // then
        assertThat(response.getToken()).isNotNull();
    }
}
