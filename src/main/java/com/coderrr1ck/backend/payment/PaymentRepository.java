package com.coderrr1ck.backend.payment;


import com.coderrr1ck.backend.order.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
//    List<Payment> findByOrderIdAndPaymentStatus(UUID orderId, PaymentStatus paymentStatus);

    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByGatewayOrderRefId(String s);
}
