package com.coderrr1ck.backend.payment;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;



public class OfflinePayment extends Payment{
    private PaymentMode paymentMode = PaymentMode.OFFLINE;
}
