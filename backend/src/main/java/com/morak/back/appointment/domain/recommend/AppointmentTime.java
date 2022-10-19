package com.morak.back.appointment.domain.recommend;

import static com.morak.back.appointment.domain.Appointment.MINUTES_UNIT;

import com.morak.back.appointment.domain.AvailableTime;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class AppointmentTime {

    private final LocalDateTime startDateTime;
    private final int durationMinutes;

    public boolean contains(AvailableTime availableTime) {
        LocalDateTime dateTime = availableTime.getStartDateTime();
        return !startDateTime.isAfter(dateTime)
                && dateTime.isBefore(startDateTime.plusMinutes(durationMinutes));
    }

    public int getUnitCount() {
        return durationMinutes / MINUTES_UNIT;
    }

    public LocalDateTime getEndDateTime() {
        return this.startDateTime.plusMinutes(durationMinutes);
    }
}
