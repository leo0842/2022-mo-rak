package com.morak.back.appointment.ui.dto;

import com.morak.back.appointment.domain.AppointmentStatus;
import com.morak.back.newdomain.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AppointmentStatusResponse {

    private MenuStatus status;
}
