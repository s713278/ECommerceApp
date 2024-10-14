package com.app.payloads.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileSignInRequest {

    @Schema(description = "Customer's password", example = "StrongP@ssword123")
    @NotBlank(message = "First name is required.")
    @JsonProperty("password")
    private String password;

    @NotNull @Schema(description = "Customer's mobile number", example = "9876543210")
    @JsonProperty("mobile_number")
    private Long mobile;
    

}
