package com.morak.back.newdomain;

import com.morak.back.auth.domain.Member;
import com.morak.back.poll.domain.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "new_appointment_available_time")
public class NewAvailableTime extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDateTime startDateTime;

    @Builder
    private NewAvailableTime(Long id, Member member,
                          LocalDateTime startDateTime, LocalDateTime ignored) {
        LocalDateTime now = LocalDateTime.now();
        this.id = id;
        this.startDateTime = startDateTime;
        this.member = member;
    }
}
