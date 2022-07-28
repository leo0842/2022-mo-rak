package com.morak.back.appointment.domain;

import static com.morak.back.appointment.domain.Appointment.builder;
import static java.time.LocalDate.now;
import static java.time.LocalTime.of;
import static org.assertj.core.api.Assertions.assertThat;

import com.morak.back.auth.domain.Member;
import com.morak.back.team.domain.Team;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    @Test
    void test() {
        // given
        Appointment appointment = builder()
                .host(new Member())
                .team(new Team())
                .title("스터디 회의 날짜 정하기")
                .description("필참!!")
                .startDate(now().plusDays(1))
                .endDate(now().plusDays(5))
                .startTime(of(14, 0))
                .endTime(of(18, 30))
                .durationHours(1)
                .durationMinutes(0)
                .build();

        System.out.println("appointment.getStatus() = " + appointment.getStatus());

        // when

        // then
    }
    // TODO: 2022/07/28 AvailableTime 추가 후 테스트 필요!!
    @Test
    void 포뮬라를_적용해_count를_불러온다() {
        // when
        Appointment appointment = builder()
                .host(new Member())
                .team(new Team())
                .title("스터디 회의 날짜 정하기")
                .description("필참!!")
                .startDate(now().plusDays(1))
                .endDate(now().plusDays(5))
                .startTime(of(14, 0))
                .endTime(of(18, 30))
                .durationHours(1)
                .durationMinutes(0)
                .build();

        // then
        assertThat(appointment.getCount()).isEqualTo(0);
    }
}
