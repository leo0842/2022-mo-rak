package com.morak.back.poll.acceptance;

import static com.morak.back.AuthSupporter.toHeader;
import static com.morak.back.SimpleRestAssured.toObjectList;
import static org.assertj.core.api.Assertions.assertThat;

import com.morak.back.AcceptanceTest;
import com.morak.back.AuthSupporter;
import com.morak.back.SimpleRestAssured;
import com.morak.back.auth.application.TokenProvider;
import com.morak.back.poll.ui.dto.PollCreateRequest;
import com.morak.back.poll.ui.dto.PollItemRequest;
import com.morak.back.poll.ui.dto.PollItemResponse;
import com.morak.back.poll.ui.dto.PollItemResultResponse;
import com.morak.back.poll.ui.dto.PollResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PollAcceptanceTest extends AcceptanceTest {

    private static final String INVALID_ACCESS_TOKEN = "invalid.access.token";
    private String accessToken;

    @Autowired
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUpAccessToken() {
        super.setUp();
        accessToken = 로그인을_해_토큰을_발급받는다(1L);
    }

    @Test
    void 투표를_생성한다() {
        // given
        // when
        ExtractableResponse<Response> response = 투표_생성_요청을_보내면_응답을_받는다(accessToken);
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void 투표_목록을_조회한다() {
        // given
        String path = "/api/groups/MoraK123/polls";

        // when
        ExtractableResponse<Response> response = 투표_목록을_조회_요청을_보내면_응답을_받는다(path);

        // then
        List<PollResponse> responses = toObjectList(response, PollResponse.class);
        Assertions.assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(responses).hasSize(1)
        );
    }

    private ExtractableResponse<Response> 투표_목록을_조회_요청을_보내면_응답을_받는다(String path) {
        return SimpleRestAssured.get(path, toHeader(accessToken));
    }

    @Test
    void 투표를_진행한다() {
        // given
        PollCreateRequest createRequest = new PollCreateRequest("투표_제목", 1, false, LocalDateTime.now(),
                List.of("항목1", "항목2"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        List<PollItemRequest> pollItemRequests = List.of(new PollItemRequest(4L, "눈물이_나기_때문이에요"));

        // when
        ExtractableResponse<Response> response = 투표를_진행한다(accessToken, location, pollItemRequests);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 재투표를_진행한다() {
        // given
        

        PollCreateRequest createRequest = new PollCreateRequest("투표_제목", 1, false, LocalDateTime.now(),
                List.of("항목1", "항목2"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        List<PollItemRequest> pollItemRequests = List.of(new PollItemRequest(4L, "눈물이_나기_때문이에요"));
        투표를_진행한다(accessToken, location, pollItemRequests);

        List<PollItemRequest> rePollItemRequests = List.of(new PollItemRequest(5L, "다시_일어설거에요!"));

        // when
        ExtractableResponse<Response> rePollResponse = 투표를_진행한다(accessToken, location, rePollItemRequests);

        // then
        assertThat(rePollResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 투표_단건을_조회한다() {
        // given
        

        PollCreateRequest createRequest = new PollCreateRequest("앨버의_현재_위치", 1, false, LocalDateTime.now(),
                List.of("트랙룸", "1번_회의실"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .get(location)
                .then().log().all().extract();

        // then
        PollResponse pollResponse = response.body().jsonPath().getObject(".", PollResponse.class);
        Assertions.assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(pollResponse.getTitle()).isEqualTo("앨버의_현재_위치"),
                () -> assertThat(pollResponse.getIsHost()).isTrue()
        );
    }

    @Test
    void 투표_선택_항목을_조회한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("위니의_영어_단어_사용_횟수", 1, false, LocalDateTime.now(),
                List.of("삼십만", "300000"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .get(location + "/items")
                .then().log().all().extract();

        // then
        List<PollItemResponse> pollItemResponses = response.body().jsonPath().getList(".", PollItemResponse.class);
        Assertions.assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(pollItemResponses).hasSize(2),
                () -> assertThat(pollItemResponses.get(0).getId()).isNotNull(),
                () -> assertThat(pollItemResponses.get(0).getSubject()).isEqualTo("삼십만"),
                () -> assertThat(pollItemResponses.get(0).getSelected()).isFalse(),
                () -> assertThat(pollItemResponses.get(0).getDescription()).isBlank()
        );
    }

    @Test
    void 투표를_진행한_상태에서_투표_선택_항목을_조회한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("에덴의_속마음은?", 1, false, LocalDateTime.now(),
                List.of("에덴은_칼퇴하고_싶다.", "에덴은_11시에_퇴근하고_싶다."));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        투표를_진행한다(accessToken, location, List.of(new PollItemRequest(4L, "월요일이기때문!!")));

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .get(location + "/items")
                .then().log().all().extract();

        // then
        List<PollItemResponse> pollItemResponses = response.body().jsonPath().getList(".", PollItemResponse.class);
        Assertions.assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(pollItemResponses).hasSize(2),
                () -> assertThat(pollItemResponses.get(0).getId()).isNotNull(),
                () -> assertThat(pollItemResponses.get(0).getSubject()).isEqualTo("에덴은_칼퇴하고_싶다."),
                () -> assertThat(pollItemResponses.get(0).getSelected()).isTrue(),
                () -> assertThat(pollItemResponses.get(0).getDescription()).isEqualTo("월요일이기때문!!"),
                () -> assertThat(pollItemResponses.get(1).getSubject()).isEqualTo("에덴은_11시에_퇴근하고_싶다."),
                () -> assertThat(pollItemResponses.get(1).getSelected()).isFalse(),
                () -> assertThat(pollItemResponses.get(1).getDescription()).isBlank()
        );
    }

    @Test
    void 무기명_투표_결과를_조회한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("투표_제목", 2, true, LocalDateTime.now(),
                List.of("항목1", "항목2", "항목3"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        List<PollItemRequest> pollItemRequests = List.of(new PollItemRequest(4L, "눈물이_나기_때문이에요"));
        투표를_진행한다(accessToken, location, pollItemRequests);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .get(location + "/result")
                .then().log().all().extract();

        final List<PollItemResultResponse> resultResponses
                = response.body().jsonPath().getList(".", PollItemResultResponse.class);
        // then
        Assertions.assertAll(
                () -> assertThat(resultResponses).hasSize(3),
                () -> assertThat(resultResponses.get(0).getCount()).isEqualTo(1),
                () -> assertThat(resultResponses.get(0).getMembers()).hasSize(1),
                () -> assertThat(resultResponses.get(1).getMembers()).hasSize(0),
                () -> assertThat(resultResponses.get(2).getMembers()).hasSize(0),
                () -> assertThat(resultResponses.get(0).getMembers().get(0).getName()).isBlank(),
                () -> assertThat(resultResponses.get(0).getMembers().get(0).getDescription()).isEqualTo("눈물이_나기_때문이에요")
        );
    }

    @Test
    void 기명_투표_결과를_조회한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("투표_제목", 2, false, LocalDateTime.now(),
                List.of("항목1", "항목2", "항목3"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        List<PollItemRequest> pollItemRequests = List.of(new PollItemRequest(4L, "눈물이_나기_때문이에요"));
        투표를_진행한다(accessToken, location, pollItemRequests);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .get(location + "/result")
                .then().log().all().extract();

        final List<PollItemResultResponse> resultResponses = response.body().jsonPath()
                .getList(".", PollItemResultResponse.class);

        // then
        Assertions.assertAll(
                () -> assertThat(resultResponses).hasSize(3),
                () -> assertThat(resultResponses.get(0).getMembers()).hasSize(1),
                () -> assertThat(resultResponses.get(0).getMembers().get(0).getName()).isEqualTo("eden"),
                () -> assertThat(resultResponses.get(0).getMembers().get(0).getDescription()).isEqualTo("눈물이_나기_때문이에요"),
                () -> assertThat(resultResponses.get(1).getMembers()).hasSize(0),
                () -> assertThat(resultResponses.get(2).getMembers()).hasSize(0)
        );
    }

    @Test
    void 투표를_삭제한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("투표_제목", 2, false, LocalDateTime.now(),
                List.of("항목1", "항목2", "항목3"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .delete(location)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 투표를_마감한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("투표_제목", 2, false, LocalDateTime.now(),
                List.of("항목1", "항목2", "항목3"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        // when
        ExtractableResponse<Response> response = 투표를_마감한다(accessToken, location);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 인증되지않은_사용자가_투표마감시_403을_응답한다() {
        // given
        

        PollCreateRequest request = new PollCreateRequest("투표_제목", 2, false, LocalDateTime.now(),
                List.of("항목1", "항목2", "항목3"));
        String location = 투표를_생성한_뒤_투표_URL을_받는다(accessToken);

        // when
        ExtractableResponse<Response> response = 투표를_마감한다(INVALID_ACCESS_TOKEN, location);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    private String 로그인을_해_토큰을_발급받는다(Long id) {
        return tokenProvider.createToken(String.valueOf(id));
    }

    private ExtractableResponse<Response> 투표를_진행한다(String accessToken, String location,
                                                   List<PollItemRequest> pollItemRequest) {
        return RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .body(pollItemRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .put(location)
                .then().log().all().extract();
    }

    private String 투표를_생성한_뒤_투표_URL을_받는다(String accessToken) {
        return 투표_생성_요청을_보내면_응답을_받는다(accessToken).header("Location");
    }

    private ExtractableResponse<Response> 투표를_마감한다(String accessToken, String location) {
        return RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .patch(location + "/close")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> 투표_생성_요청을_보내면_응답을_받는다(String accessToken) {
        PollCreateRequest request = new PollCreateRequest("투표_제목", 1, false, LocalDateTime.now(),
                List.of("항목1", "항목2"));
        ExtractableResponse<Response> response = SimpleRestAssured.post("/api/groups/MoraK123/polls", request,
                toHeader(accessToken));
        return response;
    }
}
