package com.coderrr1ck.backend.payment;

import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<?> processPayment(
            @Valid @RequestBody PaymentRequest paymentRequest
    ) {
        if(paymentRequest.isOnlinePayment()){
            return ResponseEntity.ok(paymentService.processOnlinePayment(paymentRequest));
        } else {
                return ResponseEntity.ok(paymentService.processOfflinePayment(paymentRequest));
        }
    }

//    polling endpoint to check and confirm payment status for online payments
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<String> checkPaymentStatus(
            @PathVariable String paymentId
    ) {
        return paymentService.getPaymentStatus(paymentId);
    }

    @PostMapping("/callback/razorpay")
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
            @RequestHeader("X-Razorpay-Signature") String signature) throws RazorpayException {

        return paymentService.handleRazorpayWebhook(payload,signature);
    }

}
