package com.morak.back.appointment.domain;

import com.morak.back.appointment.exception.AppointmentAuthorizationException;
import com.morak.back.auth.domain.Member;
import com.morak.back.core.domain.Code;
import com.morak.back.core.exception.CustomErrorCode;
import com.morak.back.core.exception.DomainLogicException;
import com.morak.back.newdomain.Menu;
import com.morak.back.newdomain.MenuStatus;
import com.morak.back.poll.domain.BaseEntity;
import com.morak.back.team.domain.Team;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@NoArgsConstructor
public class Appointment extends BaseEntity {

    public static final int MINUTES_UNIT = 30;
    private static final int NO_ONE_SELECTED = 0;

    @Embedded
    private Menu menu;

    @Embedded
    @Valid
    private DatePeriod datePeriod;

    @Embedded
    @Valid
    private TimePeriod timePeriod;

    @Embedded
    @Valid
    private DurationMinutes durationMinutes;

    @ElementCollection
    @CollectionTable(
            name = "available_times",
            joinColumns = @JoinColumn(name = "appointment_id")
    )
    private List<AvailableTime> availableTimes = new ArrayList<>();

    @Formula("(SELECT COUNT(DISTINCT aat.member_id) FROM appointment_available_time as aat WHERE aat.appointment_id = id)")
    private Integer count;

    @Builder
    private Appointment(Long id, Team team, Member host, String title, String description, LocalDate startDate,
                        LocalDate endDate, LocalTime startTime, LocalTime endTime, Integer durationHours,
                        Integer durationMinutes, Code code, LocalDateTime closedAt) {
        super(id);
        LocalDateTime now = LocalDateTime.now(); // todo : check this
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

    public void selectAvailableTime(List<LocalDateTime> localDateTimes, Member member) {
        List<AvailableTime> availableTimes = localDateTimes.stream()
                .filter(this::isDateTimeBetween)
                .map(time -> AvailableTime.builder().member(member).startDateTime(time).build())
                .collect(Collectors.toList());

        this.availableTimes.removeIf(availableTime -> availableTime.getMember().equals(member));
        this.availableTimes.addAll(availableTimes);
    }

    private boolean isDateTimeBetween(LocalDateTime dateTime) {
        return this.datePeriod.isBetween(dateTime) && this.timePeriod.isBetween(dateTime);
    }

    public Integer parseHours() {
        return this.durationMinutes.parseHours();
    }

    public Integer parseMinutes() {
        return this.durationMinutes.parseMinutes();
    }

    /*
    DatePeriod를 직접 사용하면 getEndDate()의 minusDay 를 호출할 수 없습니다.
     */
    public boolean isAvailableDateRange(DatePeriod otherDatePeriod) {
        return DatePeriod.of(getStartDate(), getEndDate()).isAvailableRange(otherDatePeriod);
    }

    public boolean isAvailableTimeRange(TimePeriod timePeriod) {
        return true;
//        return this.timePeriod.isAvailableRange(timePeriod);
    }

    public void close(Member member) {
        validateHost(member);
        menu.close();
    }

    private void validateHost(Member member) {
        if (!isHost(member)) {
            throw new AppointmentAuthorizationException(
                    CustomErrorCode.APPOINTMENT_MEMBER_MISMATCHED_ERROR,
                    member.getId() + "번 멤버는 " + getCode() + "코드의 약속잡기의 호스트가 아닙니다."
            );
        }
    }

    public boolean isBelongedTo(Team otherTeam) {
        return menu.getTeam().equals(otherTeam);
    }

    public boolean isHost(Member member) {
        return this.menu.getHost().equals(member);
    }

    public Boolean isClosed() {
        return this.menu.getStatus().isClosed();
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(datePeriod.getStartDate(), timePeriod.getStartTime().getLocalTime());
    }

    public LocalDateTime getEndDateTime() {
        return LocalDateTime.of(datePeriod.getEndDate(), timePeriod.getEndTime().getLocalTime());
    }

    public LocalDate getStartDate() {
        return this.datePeriod.getStartDate();
    }

    public LocalDate getEndDate() {
        LocalDate endDate = this.datePeriod.getEndDate();
        if (this.timePeriod.getEndTime().equals(LocalTime.MIDNIGHT)) {
            endDate = endDate.minusDays(1);
        }
        return endDate;
    }

    public LocalTime getStartTime() {
        return this.timePeriod.getStartTime().getLocalTime();
    }

    public LocalTime getEndTime() {
        return this.timePeriod.getEndTime().getLocalTime();
    }

    public String getCode() {
        return menu.getCode().getCode();
    }

    public Integer getCount() {
        if (this.count == null) {
            return NO_ONE_SELECTED;
        }
        return this.count;
    }

    public String getTitle() {
        return menu.getTitle().getTitle();
    }

    public String getDescription() {
        return menu.getDescription().getDescription();
    }


    public LocalDateTime getClosedAt() {
        return menu.getClosedAt().getClosedAt();
    }


    public Team getTeam() {
        return menu.getTeam();
    }

    public MenuStatus getStatus() {
        return menu.getStatus();
    }

    public Member getHost() {
        return menu.getHost();
    }
}
