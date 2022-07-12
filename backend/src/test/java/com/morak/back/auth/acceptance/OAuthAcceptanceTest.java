package com.morak.back.auth.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.morak.back.AcceptanceTest;
import com.morak.back.auth.ui.dto.SigninRequest;
import com.morak.back.auth.ui.dto.SigninResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class OAuthAcceptanceTest extends AcceptanceTest {

    private static final String CODE = "edac8c342b9f5f115d33";

    /**
     * when: 사용자가 본인의 code를 이용해 로그인을 요청한다.
     * then: 모락 엑세스 토큰을 응답한다.
     */
    @DisplayName("로그인하면 모락 엑세스 토큰을 발급한다.")
    @Test
    public void signin() {
        // given
        SigninRequest request = new SigninRequest(CODE);
        
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post("/auth/signin")
                .then().log().all().extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.body().jsonPath().getObject(".", SigninResponse.class).getToken()).isNotNull()
        );
    }
}
