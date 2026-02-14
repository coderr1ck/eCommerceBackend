package com.coderrr1ck.backend.payment;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment,String> {
    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByOrderIdAndPaymentStatus(String orderId, PaymentStatus paymentStatus);

    Optional<OnlinePayment> findByGatewayOrderRefId(String s);
}
