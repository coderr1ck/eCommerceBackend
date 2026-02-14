package com.coderrr1ck.backend.payment;

import com.coderrr1ck.backend.order.Order;
import com.coderrr1ck.backend.order.OrderNotFoundException;
import com.coderrr1ck.backend.order.OrderRepository;
import com.coderrr1ck.backend.order.OrderStatus;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PaymentService {

    @Value("${razorpay.key.id:rzp_test_1DP5mmOlF5G5ag}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret:your_razorpay_key_secret}")
    private String razorpayKeySecret;
    @Value("${razorpay.webhook.secret:your_webhook_secret}")
    private String webhookSecret;

    public PaymentService(RazorpayClient razorpayClient,
                          PaymentRepository paymentRepository,
                          OrderRepository orderRepository) throws RazorpayException {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Map<String, String> processOnlinePayment(PaymentRequest paymentRequest) {
        try {
            Order savedOrder = validatePaymentRequestBeforeProcessing(paymentRequest);
            Optional<Payment> existingPayment = paymentRepository.findById(paymentRequest.getPaymentId());
            if (existingPayment.isPresent()) {
                OnlinePayment existing = (OnlinePayment) existingPayment.get();
                if (!existing.getOrderId().equals(paymentRequest.getOrderId())) {
                    throw new InvalidPaymentRequest("Invalid request:" +
                            " payment id " + paymentRequest.getPaymentId() +
                            " is not associated with order id " + paymentRequest.getOrderId());
                }
                if (existing.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    throw new PaymentAlreadyCompleted(paymentRequest.getPaymentId());
                }
                if (existing.getPaymentStatus() == PaymentStatus.CREATED) {
                    return new HashMap<>() {{
                        put("orderId", existing.getGatewayOrderRefId());
                        put("rzp_key", razorpayKeyId);
                        put("amount", String.valueOf(existing.getPaidAmount().doubleValue()));
                        put("currency", existing.getCurrency().toString());
                    }};
                }
            }

            if (savedOrder.getStatus().equals(OrderStatus.PAYMENT_COMPLETED)) {
                throw new InvalidPaymentRequest("Cannot process payment for order with status: " + savedOrder.getStatus());
            }

            JSONObject orderRequest = new JSONObject();
            BigDecimal amount = paymentRequest.getAmount();
            long amountInPaise = amount
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", PaymentCurrency.INR);
            orderRequest.put("payment_capture", 1);

            com.razorpay.Order order = razorpayClient.orders.create(orderRequest);
            String rzpOrderId = order.get("id");

            if (order.get("status").equals("created")) {
                OnlinePayment onlinePayment = new OnlinePayment();
                onlinePayment.setPaymentId(paymentRequest.getPaymentId());
                onlinePayment.setOrderId(paymentRequest.getOrderId());
                onlinePayment.setPaidAmount(paymentRequest.getAmount());
                onlinePayment.setTotalAmount(savedOrder.getTotalAmount());
                onlinePayment.setCurrency(PaymentCurrency.INR);
                onlinePayment.setGatewayOrderRefId(rzpOrderId);
                onlinePayment.setPaymentStatus(PaymentStatus.CREATED);
                onlinePayment.setPaymentGateway(PaymentGateway.RAZORPAY);
                Payment savedPayment = paymentRepository.save(onlinePayment);
                savedOrder.getPaymentIds().add(savedPayment.getPaymentId());
                orderRepository.save(savedOrder);

                return new HashMap<>() {{
                    put("orderId", rzpOrderId);
                    put("rzp_key", razorpayKeyId);
                    put("amount", String.valueOf(paymentRequest.getAmount()));
                    put("currency", PaymentCurrency.INR.toString());
                }};
            } else {
                throw new RazorpayException("Failed to create order in Razorpay");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidPaymentRequest("Unable to process online payment now. Please try again later.");
        }
    }

    public PaymentResponse processOfflinePayment(PaymentRequest paymentRequest) {
        Order savedOrder = validatePaymentRequestBeforeProcessing(paymentRequest);
        Optional<Payment> existingPayment = paymentRepository.findById(paymentRequest.getPaymentId());
        if (existingPayment.isPresent()) {
            OfflinePayment existing = (OfflinePayment) existingPayment.get();
            if (!existing.getOrderId().equals(paymentRequest.getOrderId())) {
                throw new InvalidPaymentRequest("Invalid request:" +
                        " payment id " + paymentRequest.getPaymentId() +
                        " is not associated with order id " + paymentRequest.getOrderId());
            }
            if (existing.getPaymentStatus() == PaymentStatus.SUCCESS) {
                throw new PaymentAlreadyCompleted(paymentRequest.getPaymentId());
            }
            return new PaymentResponse(existing.getPaymentId(), existing.getOrderId(), existing.getPaymentStatus());
        }

        if (savedOrder.getStatus().equals(OrderStatus.PAYMENT_COMPLETED)) {
            throw new InvalidPaymentRequest("Cannot process payment for order with status: " + savedOrder.getStatus());
        }
        OfflinePayment offlinePayment = new OfflinePayment();
        offlinePayment.setPaymentId(paymentRequest.getPaymentId());
        offlinePayment.setOrderId(paymentRequest.getOrderId());
        offlinePayment.setPaidAmount(paymentRequest.getAmount());
        offlinePayment.setTotalAmount(savedOrder.getTotalAmount());
        offlinePayment.setCurrency(PaymentCurrency.INR);
        offlinePayment.setPaymentStatus(PaymentStatus.CREATED);
        Payment savedPayment = paymentRepository.save(offlinePayment);
        savedOrder.getPaymentIds().add(savedPayment.getPaymentId());
        BigDecimal remainingDueAmount = savedOrder.getDueAmount().subtract(paymentRequest.getAmount());
        if (remainingDueAmount.equals(BigDecimal.ZERO)) {
            savedOrder.setStatus(OrderStatus.PAYMENT_COMPLETED);
        } else {
            savedOrder.setStatus(OrderStatus.PARTIAL_PAYMENT);
        }
        savedOrder.setDueAmount(remainingDueAmount);
        Order updatedOrder = orderRepository.save(savedOrder);
        if (updatedOrder != null) {
            savedPayment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(savedPayment);
        }
        return new PaymentResponse(savedPayment.getPaymentId(), savedPayment.getOrderId(), savedPayment.getPaymentStatus());
    }

    public Order validatePaymentRequestBeforeProcessing(PaymentRequest paymentRequest) {
        if (paymentRequest == null || paymentRequest.getPaymentId() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentRequest("Invalid payment request");
        }

        Order savedOrder = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(paymentRequest.getOrderId()));

        if (paymentRequest.getAmount().compareTo(savedOrder.getDueAmount()) > 0) {
            throw new InvalidPaymentRequest("Payment amount cannot be greater than order due amount");
        }

        List<Payment> successfulPayments = paymentRepository
                .findByOrderIdAndPaymentStatus(paymentRequest.getOrderId(), PaymentStatus.SUCCESS);

        BigDecimal totalPaidTillNow = successfulPayments.stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newTotalPaid = totalPaidTillNow.add(paymentRequest.getAmount());

        if (newTotalPaid.compareTo(savedOrder.getTotalAmount()) > 0) {
            throw new InvalidPaymentRequest("Total paid amount cannot exceed order total amount");
        }
        return savedOrder;
    }

    public ResponseEntity<Void> handleRazorpayWebhook(String payload, String signature) throws RazorpayException {

        if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        JSONObject paymentEntity =
                event.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String orderId = paymentEntity.getString("order_id");
        String paymentId = paymentEntity.getString("id");

//        Payment payment = paymentRepository
//                .findByRazorpayOrderId(orderId)
//                .orElseThrow();

        // 🔁 Idempotency guard
//        if (payment.getStatus() == PaymentStatus.SUCCESS) {
//            return ResponseEntity.ok().build();
//        }
//
//        if ("payment.captured".equals(eventType)) {
//            payment.setStatus(PaymentStatus.SUCCESS);
//            payment.setRazorpayPaymentId(paymentId);
//        }
//
//        if ("payment.failed".equals(eventType)) {
//            payment.setStatus(PaymentStatus.FAILED);
//        }
//
//        payment.setUpdatedAt(Instant.now());
//        paymentRepository.save(payment);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getPaymentStatus(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return ResponseEntity.ok(payment.getPaymentStatus().toString());
    }

    public boolean validatePaymentRequestCallback(@Valid PaymentVerificationRequest request) {
        try {

            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.orderId());
            attributes.put("razorpay_payment_id", request.paymentId());
            attributes.put("razorpay_signature", request.signature());
            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            OnlinePayment savedPayment = paymentRepository.findByGatewayOrderRefId(request.orderId())
                    .orElseThrow(()-> new RazorpayException("Payment not found for order id: " + request.orderId()));

            Order savedOrder = orderRepository.findById(savedPayment.getOrderId())
                    .orElseThrow(()->new OrderNotFoundException(savedPayment.getOrderId()));

            if (isValid) {
                com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(request.paymentId());
                if(!razorpayPayment.get("status").equals("captured")){
                    throw new RazorpayException("Payment not captured for payment id: " + request.paymentId());
                }
                savedPayment.setGatewayPaymentId(request.paymentId());
                savedPayment.setPaymentStatus(PaymentStatus.SUCCESS);
                BigDecimal remainingDueAmount = savedOrder.getDueAmount().subtract(savedPayment.getPaidAmount());
                savedOrder.setDueAmount(remainingDueAmount);
                if (remainingDueAmount.equals(BigDecimal.ZERO)) {
                    savedOrder.setStatus(OrderStatus.PAYMENT_COMPLETED);
                } else {
                    savedOrder.setStatus(OrderStatus.PARTIAL_PAYMENT);
                }
                Payment udatedPayment = paymentRepository.save(savedPayment);
                Order updatedOrder = orderRepository.save(savedOrder);
                if (udatedPayment == null || updatedOrder == null){
                    return false;
                }
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            throw new InvalidPaymentRequest("Payment verification failed.");
        }
    }
}
