package com.morak.back.performance.support;

import static com.morak.back.performance.Fixture.MEMBER_ID1;

import com.morak.back.appointment.domain.Appointment;
import com.morak.back.appointment.domain.AvailableTime;
import com.morak.back.core.domain.Code;
import com.morak.back.core.domain.RandomCodeGenerator;
import com.morak.back.core.domain.menu.MenuStatus;
import com.morak.back.performance.dao.AppointmentDao;
import com.morak.back.performance.dao.DummyAppointment;
import com.morak.back.performance.dao.DummyAppointmentDao;
import com.morak.back.performance.dao.DummyAvailableTime;
import com.morak.back.team.domain.Team;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("performance")
public class DummyAppointmentDummySupport {

    @Autowired
    private DummyAppointmentDao appointmentDao;

    public void 약속잡기_더미데이터를_추가한다(int teamSize, int appointmentSizePerTeam) {
        List<DummyAppointment> appointments = makeDummyAppointments(teamSize, appointmentSizePerTeam);
        appointmentDao.batchInsertAppointment(appointments);
    }

    public void 약속잡기_선택가능시간_더미데이터를_추가한다(int appointmentSize) {
        List<DummyAvailableTime> availableTimes = makeDummyAvailableTime(appointmentSize);
        appointmentDao.batchInsertAvailableTime(availableTimes);
    }

    public List<DummyAppointment> makeDummyAppointments(int teamSize, int appointmentSizePerTeam) {
        return Stream.iterate(1L, i -> i <= teamSize, i -> i + 1)
                .flatMap(teamIndex -> IntStream.rangeClosed(1, appointmentSizePerTeam)
                        .mapToObj(appointmentIndex -> DummyAppointment.builder()
                                .teamCode(Team.builder().id(teamIndex).name("모락").code(Code.generate(new RandomCodeGenerator())).build().getCode())
                                .hostId(MEMBER_ID1.getId())
                                .title("더미 약속잡기" + appointmentIndex)
                                .subTitle("더미 약속잡기 설명")
                                .startDate(LocalDate.now().plusDays(1))
                                .endDate(LocalDate.now().plusDays(8))
                                .startTime(LocalTime.of(16, 0))
                                .endTime(LocalTime.of(20, 0))
                                .durationMinutes(60)
                                .status(MenuStatus.OPEN)
                                .code(Code.generate(new RandomCodeGenerator()).getCode())
                                .closedAt(LocalDateTime.now().plusDays(1))
                                .build())
                        .collect(Collectors.toList())
                        .stream())
                .collect(Collectors.toList());
    }

    public List<DummyAvailableTime> makeDummyAvailableTime(int appointmentSize) {
        List<DummyAvailableTime> availableTimes = new ArrayList<>();
        for (long appointmentIndex = 1; appointmentIndex <= appointmentSize; appointmentIndex++) {
//            Appointment appointment = Appointment.builder()
//                    .id(appointmentIndex)
//                    .startDate(LocalDate.now().plusDays(1))
//                    .endDate(LocalDate.now().plusDays(8))
//                    .startTime(LocalTime.of(16, 0))
//                    .endTime(LocalTime.of(20, 0))
//                    .durationHours(1)
//                    .durationMinutes(0)
//                    .closedAt(LocalDateTime.now().plusDays(1))
//                    .build();
            for (int day = 1; day <= 8; day++) {
                for (int hour = 16; hour <= 19; hour++) {
                    availableTimes.add(
                            DummyAvailableTime.builder()
                                    .appointmentId(appointmentIndex)
                                    .memberId(MEMBER_ID1.getId())
                                    .startDateTime(
                                            LocalDateTime.of(LocalDate.now().plusDays(day), LocalTime.of(hour, 0)))
                                    .build()
                    );
                }
            }
        }
        return availableTimes;
    }
}
