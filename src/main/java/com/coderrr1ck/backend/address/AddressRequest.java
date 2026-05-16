package com.coderrr1ck.backend.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z\\s'.-]*$",
            message = "Please provide a valid full name"
    )
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "Please provide a valid phone number"
    )
    private String phoneNumber;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s,.-/#()]*$",
            message = "Invalid characters in address line 1"
    )
    private String line1;

    private String line2;

    private String landmark;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100)
    @Pattern(
            regexp = "^[a-zA-Z\\s.-]+$",
            message = "Please provide a valid city name"
    )
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 100)
    @Pattern(
            regexp = "^[a-zA-Z\\s.-]+$",
            message = "Please provide a valid state name"
    )
    private String state;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100)
    @Pattern(
            regexp = "^[a-zA-Z\\s.-]+$",
            message = "Please provide a valid country name"
    )
    private String country;

    @NotBlank(message = "Postal code is required")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Please provide a valid 6-digit PIN code"
    )
    private String postalCode;

    private boolean defaultAddress;
}