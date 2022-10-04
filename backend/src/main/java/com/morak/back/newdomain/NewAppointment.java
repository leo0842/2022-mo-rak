package com.morak.back.newdomain;

import com.morak.back.appointment.domain.DurationMinutes;
import com.morak.back.auth.domain.Member;
import com.morak.back.core.domain.Code;
import com.morak.back.core.exception.CustomErrorCode;
import com.morak.back.core.exception.DomainLogicException;
import com.morak.back.poll.domain.BaseEntity;
import com.morak.back.team.domain.Team;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@NoArgsConstructor
public class NewAppointment extends BaseEntity {

    @Embedded
    private Menu menu;

    @Embedded
    private DatePeriod datePeriod;

    @Embedded
    private TimePeriod timePeriod;

    @Embedded
    @Valid
    private DurationMinutes durationMinutes;

    @Formula("(SELECT COUNT(DISTINCT aat.member_id) FROM appointment_available_time as aat WHERE aat.appointment_id = id)")
    private Integer count;

    @Builder
    private NewAppointment(Long id, Team team, Member host, String title, String description, LocalDate startDate,
                           LocalDate endDate, LocalTime startTime, LocalTime endTime, Integer durationHours,
                           Integer durationMinutes, Code code, LocalDateTime closedAt) {
        super(id);
        LocalDateTime now = LocalDateTime.now();
        this.menu = new Menu(team, host, code, title, description, MenuStatus.OPEN, closedAt, now);
        this.datePeriod = DatePeriod.of(startDate, endDate);
        this.timePeriod = TimePeriod.of(startTime, endTime);
        this.durationMinutes = DurationMinutes.of(durationHours, durationMinutes);
        validateDurationAndPeriod(this.timePeriod, this.durationMinutes);
    }

    private void validateDurationAndPeriod(TimePeriod timePeriod, DurationMinutes durationMinutes) {
        if (!timePeriod.isLongerThan(durationMinutes)) {
            throw new DomainLogicException(
                    CustomErrorCode.TEMP_ERROR,
                    "진행 시간" + durationMinutes + "은 약속 시간보다 짧아야 합니다"
            );
        }
    }
}
