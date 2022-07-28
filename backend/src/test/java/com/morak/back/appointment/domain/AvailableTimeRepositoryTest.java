package com.morak.back.appointment.domain;

import static com.morak.back.appointment.domain.AvailableTime.builder;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.morak.back.auth.domain.Member;
import com.morak.back.auth.domain.MemberRepository;
import java.util.List;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class AvailableTimeRepositoryTest {

    @Autowired
    private AvailableTimeRepository availableTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Member member;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        member = memberRepository.findById(1L).orElseThrow();
        appointment = appointmentRepository.findByCode("FEsd23C1").orElseThrow();
    }

    @Test
    void 약속잡기_가능_시간을_저장한다() {
        // given
        AvailableTime availableTime = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().plusDays(1))
                .endDateTime(now().plusDays(1).plusMinutes(30))
                .build();

        // when
        AvailableTime savedAvailableTime = availableTimeRepository.save(availableTime);

        // then
        assertThat(savedAvailableTime.getId()).isNotNull();
    }

    @Test
    void 약속잡기_가능_시작_시점이_현재보다_과거일_경우_예외를_던진다() {
        // given
        AvailableTime availableTime = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().minusDays(1))
                .endDateTime(now().minusDays(1).plusMinutes(30))
                .build();

        // when & then
        assertThatThrownBy(() -> availableTimeRepository.save(availableTime))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void 약속잡기_가능_시간을_모두_저장한다() {
        // given
        AvailableTime availableTime1 = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().plusDays(1))
                .endDateTime(now().plusDays(1).plusMinutes(30))
                .build();

        AvailableTime availableTime2 = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().plusDays(2))
                .endDateTime(now().plusDays(2).plusMinutes(30))
                .build();

        // when
        List<AvailableTime> availableTimes = availableTimeRepository.saveAll(List.of(availableTime1, availableTime2));

        // then
        Assertions.assertAll(
                () -> assertThat(availableTimes).hasSize(2),
                () -> assertThat(availableTimes).allMatch(availableTime -> availableTime.getId() != null)
        );
    }

    @Test
    void 같은_약속잡기_가능_시간을_저장하는_경우_예외를_던진다() {
        // given
        AvailableTime availableTime1 = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().withNano(0).plusDays(1))
                .endDateTime(now().withNano(0).plusDays(1).plusMinutes(30))
                .build();

        AvailableTime availableTime2 = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().withNano(0).plusDays(1))
                .endDateTime(now().withNano(0).plusDays(1).plusMinutes(30))
                .build();

        // when & then
        assertThatThrownBy(() -> availableTimeRepository.saveAll(List.of(availableTime1, availableTime2)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 멤버_id와_약속잡기_id로_약속잡기_가능_시간을_모두_삭제한다() {
        // given
        AvailableTime availableTime1 = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().plusDays(1))
                .endDateTime(now().plusDays(1).plusMinutes(30))
                .build();

        AvailableTime availableTime2 = builder()
                .member(member)
                .appointment(appointment)
                .startDateTime(now().plusDays(2))
                .endDateTime(now().plusDays(2).plusMinutes(30))
                .build();

        availableTimeRepository.saveAll(List.of(availableTime1, availableTime2));

        // when
        availableTimeRepository.deleteAllByMemberIdAndAppointmentId(member.getId(), appointment.getId());

        // then
        List<AvailableTime> availableTimes = availableTimeRepository.findAllByMemberIdAndAppointmentId(
                member.getId(), appointment.getId());
        assertThat(availableTimes).hasSize(0);
    }
}
