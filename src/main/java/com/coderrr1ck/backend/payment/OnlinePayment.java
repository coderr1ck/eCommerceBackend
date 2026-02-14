package com.coderrr1ck.backend.payment;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class OnlinePayment extends Payment{
    private final PaymentMode paymentMode = PaymentMode.ONLINE;
    private PaymentGateway paymentGateway;
    private String gatewayOrderRefId;
    private String gatewayPaymentId;

    public OnlinePayment() {
        super();
    }
}
