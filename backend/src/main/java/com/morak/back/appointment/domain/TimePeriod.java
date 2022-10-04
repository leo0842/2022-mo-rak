package com.morak.back.appointment.domain;

import com.morak.back.appointment.exception.AppointmentDomainLogicException;
import com.morak.back.core.exception.CustomErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;

@Getter
@NoArgsConstructor
@Embeddable
@ToString
public class TimePeriod {

    private static final int MINUTES_UNIT = 30;

    @AttributeOverrides(
            @AttributeOverride(name = "localTime", column = @Column(name = "start_time"))
    )
    private AppointmentTime startTime;

    @AttributeOverrides(
            @AttributeOverride(name = "localTime", column = @Column(name = "end_time"))
    )
    private AppointmentTime endTime;

    private TimePeriod(AppointmentTime startTime, AppointmentTime endTime) {
        validateChronology(startTime, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static TimePeriod of(LocalTime startTime, LocalTime endTime) {
        return new TimePeriod(
                new AppointmentTime(startTime),
                new AppointmentTime(endTime.minusMinutes(MINUTES_UNIT))
        );
    }

    private static void validateChronology(AppointmentTime startTime, AppointmentTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new AppointmentDomainLogicException(
                    CustomErrorCode.APPOINTMENT_TIME_REVERSE_CHRONOLOGY_ERROR,
                    String.format(
                            "약속잡기 마지막 시간(%s)은 시작 시간(%s) 이후여야 합니다.",
                            endTime, startTime
                    )
            );
        }
    }

    public boolean isLongerThan(DurationMinutes durationMinutes) {
        Duration duration = startTime.getDuration(endTime).plusMinutes(MINUTES_UNIT);
        return duration.toMinutes() >= durationMinutes.getDurationMinutes();
    }

    public boolean isAvailableRange(TimePeriod timePeriod) {
        throw new NotImplementedException();
    }

    public boolean isBetween(LocalDateTime dateTime) {
        LocalTime localTime = dateTime.toLocalTime();
        return !startTime.isAfter(localTime) && !endTime.isBefore(localTime);
    }
}
