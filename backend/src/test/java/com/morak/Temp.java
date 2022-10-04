package com.morak;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Temp {
    /*
    10일, 11일, 12일

    start = 10일
    10일 + 10시간
    10일 + 24시간

    11일 + 10시간

    10시부터 24시까지
    ->
    2022-01-10 10:00:00
    2022-01-13 00:00:00
    10일~10일밤(11일 0시)
    11일~12일밤(12일 0시)
    12일~13일밤(13일 0시)


    13일 10시 ~ 13일 0시
    -----



    10일, 11일, 12일
    0시부터 24시까지
    ->
    2022-01-10 00:00:00
    2022-01-13 00:00:00
    10일~11일0시
    11일~12일0시
    12일~13일0시

    13일 0시 ~ 13일 0시

    0시부터 23시까지
    ->
    2022-01-10 00:00:00
    2022-01-12 23:00:00

     */
    @Test
    void test() {
        LocalDate startDate = LocalDate.of(2022, 1, 10);
        LocalDate endDate = LocalDate.of(2022, 1, 12);
        LocalTime startTime = LocalTime.of(10, 0, 0);
        LocalTime endTime = LocalTime.of(0, 0, 0);

        List<SomePeriod> periods = create(startDate, startTime, endDate, endTime);
        System.out.println(periods);

        SomePeriod availableTime = new SomePeriod(
                LocalDateTime.of(2022, 1, 11, 9, 30, 0),
                LocalDateTime.of(2022, 1, 11, 10, 0, 0)
        );
        for (SomePeriod period : periods) {
            boolean isInside = period.isInside(availableTime);
            System.out.println("isInside = " + isInside);
        }

    }

    private List<SomePeriod> create(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        long endMinute = Duration.between(LocalTime.of(0, 0, 0), endTime).toMinutes();
        if (endTime.equals(LocalTime.MIDNIGHT)) {
            endMinute = 1440; // 60 * 24
        }
        long startMinute = Duration.between(LocalTime.of(0, 0, 0), startTime).toMinutes();

        List<SomePeriod> periods = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {

            LocalDateTime base = LocalDateTime.of(startDate, LocalTime.of(0, 0, 0));
            LocalDateTime startDateTime = base.plusMinutes(startMinute);
            LocalDateTime endDateTime = base.plusMinutes(endMinute);
            periods.add(new SomePeriod(startDateTime, endDateTime));
            startDate = startDate.plusDays(1);
        }
        return periods;
    }

    static class SomePeriod {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        public SomePeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }

        @Override
        public String toString() {
            return "SomePeriod{" +
                    "startDateTime=" + startDateTime +
                    ", endDateTime=" + endDateTime +
                    '}';
        }

        public boolean isInside(SomePeriod other) {
            return !this.startDateTime.isAfter(other.startDateTime) && !this.endDateTime.isBefore(other.endDateTime);
        }
    }



}
