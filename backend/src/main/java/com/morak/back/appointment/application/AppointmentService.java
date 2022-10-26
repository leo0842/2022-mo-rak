package com.morak.back.appointment.application;

import com.morak.back.appointment.domain.Appointment;
import com.morak.back.appointment.domain.AppointmentRepository;
import com.morak.back.appointment.domain.SystemTime;
import com.morak.back.appointment.domain.recommend.RankRecommendation;
import com.morak.back.appointment.domain.recommend.RecommendationCells;
import com.morak.back.appointment.exception.AppointmentAuthorizationException;
import com.morak.back.appointment.exception.AppointmentNotFoundException;
import com.morak.back.appointment.ui.dto.AppointmentAllResponse;
import com.morak.back.appointment.ui.dto.AppointmentCreateRequest;
import com.morak.back.appointment.ui.dto.AppointmentResponse;
import com.morak.back.appointment.ui.dto.AppointmentStatusResponse;
import com.morak.back.appointment.ui.dto.AvailableTimeRequest;
import com.morak.back.appointment.ui.dto.RecommendationResponse;
import com.morak.back.auth.domain.Member;
import com.morak.back.core.application.NotificationService;
import com.morak.back.core.domain.Code;
import com.morak.back.core.domain.CodeGenerator;
import com.morak.back.core.domain.RandomCodeGenerator;
import com.morak.back.core.domain.slack.FormattableData;
import com.morak.back.core.exception.CustomErrorCode;
import com.morak.back.core.support.Generated;
import com.morak.back.core.util.MessageFormatter;
import com.morak.back.core.application.AuthorizationService;
import com.morak.back.team.domain.TeamMember;
import com.morak.back.team.domain.TeamMemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentService {

    private static final CodeGenerator CODE_GENERATOR = new RandomCodeGenerator();

    private final AppointmentRepository appointmentRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationService notificationService;
    private final SystemTime systemTime;
    private final AuthorizationService authorizationService;

    public AppointmentResponse createAppointment(String teamCode, Long memberId, AppointmentCreateRequest request) {
        return authorizationService.withTeamMemberValidation(
                () -> AppointmentResponse.from(appointmentRepository.save(
                        request.toAppointment(
                                Code.generate((length) -> teamCode),
                                memberId,
                                Code.generate(CODE_GENERATOR),
                                systemTime.now()
                        )
                ), memberId), teamCode, memberId
        );
    }

    @Transactional(readOnly = true)
    public List<AppointmentAllResponse> findAppointments(String teamCode, Long memberId) {
        return authorizationService.withTeamMemberValidation(
                () -> appointmentRepository.findAllByTeamCode(teamCode)
                        .stream()
                        .map(AppointmentAllResponse::from)
                        .sorted()
                        .collect(Collectors.toList()), teamCode, memberId
        );
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findAppointment(String teamCode, Long memberId, String appointmentCode) {
        return authorizationService.withTeamMemberValidation(
                () -> {
                    Appointment appointment = findAppointmentInTeam(teamCode, appointmentCode);
                    return AppointmentResponse.from(appointment, memberId);
                }, teamCode, memberId
        );
    }

    private Appointment findAppointmentInTeam(String teamCode, String appointmentCode) {
        Appointment appointment = appointmentRepository.findByCode(appointmentCode).orElseThrow(
                () -> AppointmentNotFoundException.ofAppointment(
                        CustomErrorCode.APPOINTMENT_NOT_FOUND_ERROR,
                        appointmentCode
                )
        );
        validateAppointmentInTeam(teamCode, appointment);
        return appointment;
    }

    public void selectAvailableTimes(String teamCode, Long memberId, String appointmentCode,
                                     List<AvailableTimeRequest> requests) {
        authorizationService.withTeamMemberValidation(
                () -> {
                    Appointment appointment = findAppointmentInTeam(teamCode, appointmentCode);
                    appointment.selectAvailableTime(
                            toStartDateTime(requests),
                            memberId,
                            systemTime.now());
                    return null;
                }, teamCode, memberId
        );
    }

    private Set<LocalDateTime> toStartDateTime(List<AvailableTimeRequest> requests) {
        return requests.stream()
                .map(AvailableTimeRequest::getStart)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> recommendAppointmentTimes(String teamCode, Long memberId,
                                                                  String appointmentCode) {
        return authorizationService.withTeamMemberValidation(
                () -> {
                    Appointment appointment = findAppointmentInTeam(teamCode, appointmentCode);
                    List<Member> members = findMembersByTeamCode(teamCode);

                    RecommendationCells cells = RecommendationCells.of(appointment, toMemberIds(members));
                    List<RankRecommendation> recommendations = cells.recommend(appointment.getAvailableTimes());

                    return toRecommendationResponses(members, recommendations);
                }, teamCode, memberId
        );
    }

    private List<Member> findMembersByTeamCode(String teamCode) {
        return teamMemberRepository.findAllByTeamCode(teamCode)
                .stream()
                .map(TeamMember::getMember)
                .collect(Collectors.toList());
    }

    private List<Long> toMemberIds(List<Member> members) {
        return members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
    }

    private List<RecommendationResponse> toRecommendationResponses(List<Member> members,
                                                                   List<RankRecommendation> recommendations) {
        return recommendations.stream()
                .map(recommendation -> RecommendationResponse.of(recommendation, members))
                .collect(Collectors.toList());
    }

    public void closeAppointment(String teamCode, Long memberId, String appointmentCode) {
        authorizationService.withTeamMemberValidation(
                () -> {
                    Appointment appointment = findAppointmentInTeam(teamCode, appointmentCode);
                    appointment.close(memberId);
//                    notificationService.notifyMenuStatus(team,
//                            MessageFormatter.formatClosed(FormattableData.from(appointment)));
                    return null;
                }, teamCode, memberId
        );
    }

    public void deleteAppointment(String teamCode, Long memberId, String appointmentCode) {
        authorizationService.withTeamMemberValidation(
                () -> {
                    Appointment appointment = findAppointmentInTeam(teamCode, appointmentCode);
                    validateHost(memberId, appointment);
                    appointmentRepository.delete(appointment);
                    return null;
                }, teamCode, memberId
        );
    }

    private void validateHost(Long memberId, Appointment appointment) {
        if (!appointment.isHost(memberId)) {
            throw new AppointmentAuthorizationException(CustomErrorCode.APPOINTMENT_HOST_MISMATCHED_ERROR,
                    memberId + "번 멤버는 " + appointment.getCode() + "코드의 약속잡기의 호스트가 아닙니다.");
        }
    }

    @Generated
    public void notifyClosedBySchedule() {
        List<Appointment> appointmentsToBeClosed = appointmentRepository.findAllToBeClosed(LocalDateTime.now());

        closeAll(appointmentsToBeClosed);
        notifyStatusAll(appointmentsToBeClosed);
    }

    private void closeAll(List<Appointment> appointmentsToBeClosed) {
        appointmentRepository.closeAllByIds(
                appointmentsToBeClosed.stream().map(Appointment::getId).collect(Collectors.toList()));
    }

    private void notifyStatusAll(List<Appointment> appointmentsToBeClosed) {
        Map<String, String> teamMessages = appointmentsToBeClosed.stream().collect(
                Collectors.groupingBy(
                        Appointment::getTeamCode,
                        Collectors.mapping(
                                appointment -> MessageFormatter.formatClosed(FormattableData.from(appointment)),
                                Collectors.joining("\n")
                        )
                )
        );
        notificationService.notifyAllMenuStatus(teamMessages);
    }

    public AppointmentStatusResponse findAppointmentStatus(String teamCode, Long memberId, String appointmentCode) {
        return authorizationService.withTeamMemberValidation(
                () -> {
                    Appointment appointment = findAppointmentInTeam(teamCode, appointmentCode);
                    return new AppointmentStatusResponse(appointment.getStatus());
                }, teamCode, memberId
        );
    }

    private void validateAppointmentInTeam(String teamCode, Appointment appointment) {
        if (!appointment.isBelongedTo(teamCode)) {
            throw new AppointmentAuthorizationException(CustomErrorCode.APPOINTMENT_TEAM_MISMATCHED_ERROR,
                    String.format("%s 코드의 약속잡기는 %s 코드의 팀에 속해있지 않습니다.", appointment.getCode(), teamCode));
        }
    }
}
