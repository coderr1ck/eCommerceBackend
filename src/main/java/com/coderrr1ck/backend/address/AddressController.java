package com.coderrr1ck.backend.address;

import com.coderrr1ck.backend.user.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/address")
@AllArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest addressRequest,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.saveAddress(user,addressRequest));
    }

    @GetMapping("{id}")
    public ResponseEntity<AddressResponse> getById(
            @PathVariable("id") Integer id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.getAddressById(id,user));
    }

     @PutMapping("{id}")
     public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AddressRequest addressRequest,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.updateAddress(id,addressRequest,user));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable("id") Integer id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        addressService.deleteAddress(id,user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
     public ResponseEntity<List<AddressResponse>> getMyAddresses(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.getMyAddresses(user));
    }
}
