package com.coderrr1ck.backend.payment;

import com.coderrr1ck.backend.user.User;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<?> processPayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        if(paymentRequest.getPaymentMode() == PaymentMode.ONLINE) {
            return ResponseEntity.ok(paymentService.processOnlinePayment(user,paymentRequest));
        } else {
                return ResponseEntity.ok(paymentService.processOfflinePayment(user,paymentRequest));
        }
    }



    @PostMapping("/callback/razorpay") // just to verify from razorpay
    // if payment was captured,webhook will finally update db
    public ResponseEntity<Void> paymentCallback(
            @RequestBody @Valid PaymentVerificationRequest request
    ){
            boolean isSuccessful = paymentService.validatePaymentRequestCallback(request);
            return isSuccessful ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }


//    webhook endpoint to verify Razorpay payments automatically for online payments
    @PostMapping("/webhook/razorpay/verify")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        return paymentService.handleRazorpayWebhook(payload,signature);
    }

}
