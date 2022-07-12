package com.morak.back.auth.ui.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SigninRequest {

    @NotBlank
    private String code;

    @JsonCreator
    public SigninRequest(String code) {
        this.code = code;
    }
}
