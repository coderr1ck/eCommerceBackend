package com.coderrr1ck.backend.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
        private Integer id;
        private String fullName;
        private String phone;
        private String line1;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String line2;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String landmark;

        private String city;
        private String state;
        private String country;
        private String pincode;
}

