package com.morak.back.newdomain;

import static com.morak.back.appointment.domain.Appointment.MINUTES_UNIT;

import com.morak.back.appointment.domain.DurationMinutes;
import com.morak.back.appointment.exception.AppointmentDomainLogicException;
import com.morak.back.core.exception.CustomErrorCode;
import java.time.Duration;
import java.time.LocalTime;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@Embeddable
@ToString
public class TimePeriod {

    private LocalTime startTime;

    private LocalTime endTime;

    private TimePeriod(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static TimePeriod of(LocalTime startTime, LocalTime endTime) {
        endTime = endTime.minusMinutes(MINUTES_UNIT);
        validateChronology(startTime, endTime);
        validateMinutes(startTime, endTime);
        return new TimePeriod(startTime, endTime);
    }

    private static void validateChronology(LocalTime startTime, LocalTime endTime) {
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

    private static void validateMinutes(LocalTime startTime, LocalTime endTime) {
        if (isNotDividedByUnit(startTime) || isNotDividedByUnit(endTime)) {
            throw new AppointmentDomainLogicException(
                CustomErrorCode.APPOINTMENT_NOT_DIVIDED_BY_MINUTES_UNIT_ERROR,
                String.format(
                    "약속잡기 시작/마지막 시간(%s, %s)은 %d분 단위여야 합니다.",
                    startTime, endTime, MINUTES_UNIT
                )
            );
        }

    }

    private static boolean isNotDividedByUnit(LocalTime time) {
        return time.getMinute() % MINUTES_UNIT != 0;
    }

//    public boolean isAvailableRange(TimePeriod timePeriod) {
//        LocalTime selectedStartTime = timePeriod.startTime;
//        if (selectedStartTime.isBefore(this.startTime)) {
//            return false;
//        }
//
//        if (isMidnight(this.endTime)) {
//            return true;
//        }
//
//        LocalTime selectedEndTime = timePeriod.endTime;
//        return !isOutOfEndTime(selectedEndTime);
//    }
//
//    private boolean isOutOfEndTime(LocalTime selectedEndTime) {
//        return selectedEndTime.isAfter(this.endTime) || isMidnight(selectedEndTime);
//    }

    public boolean isLongerThan(DurationMinutes durationMinutes) {
        Duration duration = Duration.between(startTime, endTime).plusMinutes(MINUTES_UNIT);
        return duration.toMinutes() >= durationMinutes.getDurationMinutes();
    }
}
