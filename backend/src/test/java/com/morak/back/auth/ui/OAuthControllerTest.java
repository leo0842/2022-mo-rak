package com.morak.back.auth.ui;

import static com.morak.back.ApiDocumentUtils.getDocumentRequest;
import static com.morak.back.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.morak.back.ControllerTest;
import com.morak.back.auth.application.OAuthService;
import com.morak.back.auth.ui.dto.ChangeNameRequest;
import com.morak.back.auth.ui.dto.MemberResponse;
import com.morak.back.auth.ui.dto.SigninRequest;
import com.morak.back.auth.ui.dto.SigninResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class OAuthControllerTest extends ControllerTest {

    @Autowired
    private OAuthService oAuthService;

    @Test
    void 로그인을_한다() throws Exception {
        // given
        SigninRequest request = new SigninRequest("signin-code");
        SigninResponse signinResponse = new SigninResponse("access-token");

        given(oAuthService.signin(any(SigninRequest.class))).willReturn(signinResponse);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        response.andExpect(status().isCreated())
                .andDo(document(
                        "auth/signin",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @Test
    void 자기자신을_조회한다() throws Exception {
        // given
        given(oAuthService.findMember(anyLong()))
                .willReturn(new MemberResponse(1L, "member-name", "https://profile-url.com"));

        // when
        ResultActions response = mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer aaaaaa.bbbbbb.ccccccc")
        );

        // then
        response.andExpect(status().isOk())
                .andDo(document(
                        "auth/get-me",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @Test
    void 본인_이름을_변경한다() throws Exception {
        // given
        ChangeNameRequest request = new ChangeNameRequest("변경하려는 이름");

        doNothing().when(oAuthService).changeName(anyLong(), any(ChangeNameRequest.class));

        // when
        ResultActions response = mockMvc.perform(patch("/api/auth/me/name")
                .header("Authorization", "Bearer aaaaaa.bbbbbb.ccccccc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        response.andExpect(status().isOk())
                .andDo(document(
                        "auth/change-name",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }
}
