package com.morak.back.appointment.domain;

import com.morak.back.auth.domain.Member;
import java.time.LocalDateTime;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
//@Table(name = "appointment_available_time")
public class AvailableTime {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDateTime startDateTime;

    @Builder
    private AvailableTime(Long id, Member member,
                          LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.member = member;
        this.startDateTime = startDateTime;
    }
}
