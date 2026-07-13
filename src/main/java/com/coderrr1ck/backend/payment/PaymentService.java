package com.coderrr1ck.backend.payment;

import com.coderrr1ck.backend.order.*;
import com.coderrr1ck.backend.product.ProductRepository;
import com.coderrr1ck.backend.product.ProductService;
import com.coderrr1ck.backend.user.User;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class PaymentService {

    @Value("${razorpay.key-id:rzp_test_1DP5mmOlF5G5ag}")
    private String razorpayKeyId;
    @Value("${razorpay.key-secret:your_razorpay_key_secret}")
    private String razorpayKeySecret;
    @Value("${razorpay.webhook-secret:r8t8kp@t3l11}")
    private String webhookSecret;

    public PaymentService(RazorpayClient razorpayClient,
                          PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          ProductRepository productRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public Map<String, String> processOnlinePayment(User user, PaymentRequest paymentRequest) {

            Order savedOrder = validatePaymentRequestBeforeProcessing(paymentRequest);
            Optional<Payment> existingPayment = paymentRepository.findByOrder(savedOrder);

            if (existingPayment.isPresent()) {
                Payment existing = existingPayment.get();
                validateExistingPayment(existing, paymentRequest);

                if (existing.getPaymentMode() == PaymentMode.COD) {
                    throw new InvalidPaymentRequest("Cash on delivery already selected for this order.");
                }

                if (existing.getPaymentStatus() == PaymentStatus.PENDING || existing.getPaymentStatus() == PaymentStatus.FAILED) {
                    paymentRepository.save(existing);
                    return buildPaymentResponse(existing.getGatewayOrderRefId(), existing.getTotalAmount(), existing);
                }

            }

            BigDecimal amount = savedOrder.getSubTotal();
            long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", PaymentCurrency.INR.toString());
            orderRequest.put("payment_capture", 1);

        try {
            com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);
            String rzpOrderId = rzpOrder.get("id");
            Payment newPayment = createOnlinePayment(user, savedOrder, rzpOrderId);
            return buildPaymentResponse(rzpOrderId, amount, newPayment);
        }catch (Exception e){
            e.printStackTrace();
            throw new InvalidPaymentRequest("Unable to process payment at the moment. Please try again later.");
        }
    }



    public PaymentResponse processOfflinePayment(User user,PaymentRequest paymentRequest) {
        Order savedOrder = validatePaymentRequestBeforeProcessing(paymentRequest);
        Optional<Payment> existingPayment = paymentRepository.findByOrder(savedOrder);
        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            validateExistingPayment(existing, paymentRequest);
            if (existing.getPaymentMode() == PaymentMode.ONLINE) {
                throw new InvalidPaymentRequest("Online payment method already selected for this order.");
            }
            return new PaymentResponse(existing.getPaymentId(), existing.getOrder().getOrderId(), existing.getPaymentStatus(),existing.getPaymentMode());
        }

        Payment newPayment = createOfflinePayment(user,savedOrder);
        return new PaymentResponse(newPayment.getPaymentId(), newPayment.getOrder().getOrderId(), newPayment.getPaymentStatus(),newPayment.getPaymentMode());
    }

    public Order validatePaymentRequestBeforeProcessing(PaymentRequest paymentRequest) {
        if (paymentRequest == null || paymentRequest.getOrderId() == null) {
            throw new InvalidPaymentRequest("Invalid payment request.");
        }
        Order savedOrder = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(paymentRequest.getOrderId().toString()));

        if(!savedOrder.getStatus().equals(OrderStatus.PLACED) ){
            throw new InvalidPaymentRequest("Cannot process payment for order with status : " + savedOrder.getStatus());
        }

        if(!savedOrder.getOrderPaymentStatus().equals(OrderPaymentStatus.PAYMENT_PENDING)){
            throw new InvalidPaymentRequest("Cannot process payment for order with status : " + savedOrder.getOrderPaymentStatus());
        }

        return savedOrder;
    }

    private void validateExistingPayment(Payment existing, PaymentRequest paymentRequest) {
        if (!existing.getOrder().getOrderId().equals(paymentRequest.getOrderId())) {
            throw new InvalidPaymentRequest("Invalid request:" +
                    " payment id" + existing.getPaymentId() +
                    " is not associated with order id " + paymentRequest.getOrderId());
        }
        if (existing.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentAlreadyCompleted(existing.getPaymentId().toString());
        }
        if(existing.getPaymentStatus() == PaymentStatus.REFUNDED){
            throw new InvalidPaymentRequest("Payment already refunded for this order");
        }
    }

    @Transactional
    public ResponseEntity<Void> handleRazorpayWebhook(String payload, String signature) {
    try {
            if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            JSONObject payloadJsonObj = new JSONObject(payload);
            String eventType = payloadJsonObj.getString("event");

            JSONObject paymentEntity =
                    payloadJsonObj.getJSONObject("payload")
                            .getJSONObject("payment")
                            .getJSONObject("entity");

            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");

        Payment payment = paymentRepository
                .findByGatewayOrderRefId(orderId)
                .orElseThrow(()-> new InvalidPaymentRequest("Payment not found for order id: " + orderId));

        if (payment.getPaymentStatus().equals(PaymentStatus.SUCCESS) || payment.getPaymentStatus().equals(PaymentStatus.REFUNDED)) {
            log.info("Received webhook for order {} with payment status {}. No action needed.", payment.getOrder().getOrderId(), payment.getPaymentStatus());
            return ResponseEntity.ok().build();
        }

        Order savedOrder = payment.getOrder();

        if(savedOrder.getOrderPaymentStatus().equals(OrderPaymentStatus.PAYMENT_COMPLETED) || savedOrder.getOrderPaymentStatus().equals(OrderPaymentStatus.PAYMENT_REFUNDED)){
            log.info("Received webhook for order {} with payment status {}. No action needed.", savedOrder.getOrderId(), savedOrder.getOrderPaymentStatus());
            return ResponseEntity.ok().build();
        }

        long amountPaidInPaise = paymentEntity.getLong("amount");
        long expectedAmountInPaise = payment.getTotalAmount().multiply(new BigDecimal(100)).longValue();
        if (amountPaidInPaise != expectedAmountInPaise) {
            throw new InvalidPaymentRequest("Amount mismatch! Potential fraud attempt.");
        }

        if ("payment.captured".equals(eventType)) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.attemptsList.add(PaymentAttempt
                    .builder()
                            .payment(payment)
                            .gatewayOrderRefId(payment.getGatewayOrderRefId())
                            .gatewayPaymentRefId(paymentId)
                            .paymentStatus(PaymentStatus.SUCCESS)
                    .build());
            payment.setPaidAmount(BigDecimal.valueOf(amountPaidInPaise).divide(new BigDecimal(100)));
            savedOrder.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_COMPLETED);
            for(var item : savedOrder.getOrderItems()){
                    int rowsAffected = productRepository.deductStockAtomic(item.getProduct().getProductId(), item.getQuantity());
                    if(rowsAffected == 0){
                        log.error("Failed to deduct stock for product {} during order finalization. Order ID: {}", item.getProduct().getProductId(), savedOrder.getOrderId());
                        throw new InsufficientStockException("Failed to finalize order due to insufficient reserved stock for product: " + item.getProduct().getName());
                    }
                }
            log.info("========================[RAZORPAY WEBHOOK PAYMENT SUCCESS : "+payment.getPaymentId()+" ]===================================");
        }

        if ("payment.failed".equals(eventType)) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.attemptsList.add(PaymentAttempt
                    .builder()
                    .payment(payment)
                    .gatewayOrderRefId(payment.getGatewayOrderRefId())
                    .gatewayPaymentRefId(paymentId)
                    .paymentStatus(PaymentStatus.FAILED)
                    .build());

            log.info("============================[RAZORPAY WEBHOOK PAYMENT FAILED : "+payment.getPaymentId()+"]===============================");
        }

        paymentRepository.save(payment);
        orderRepository.save(savedOrder);
        return ResponseEntity.ok().build();
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    public boolean validatePaymentRequestCallback(PaymentVerificationRequest request) {
        System.out.println(request);
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.orderId());
            attributes.put("razorpay_payment_id", request.paymentId());
            attributes.put("razorpay_signature", request.signature());
            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
            if(!isValid) return false;
            com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(request.paymentId());
            if(!razorpayPayment.get("status").equals("captured")){
                throw new RazorpayException("Payment not captured for payment id: " + request.paymentId());
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            throw new InvalidPaymentRequest("Payment verification failed.");
        }
    }

    public Payment createOnlinePayment(User user, Order savedOrder, String rzpOrderId) {
        Payment onlinePayment = new Payment();
        onlinePayment.setUser(user);
        onlinePayment.setOrder(savedOrder);
        onlinePayment.setPaidAmount(BigDecimal.ZERO);
        onlinePayment.setTotalAmount(savedOrder.getSubTotal());
        onlinePayment.setCurrency(PaymentCurrency.INR);
        onlinePayment.setGatewayOrderRefId(rzpOrderId);
        onlinePayment.setPaymentStatus(PaymentStatus.PENDING);
        onlinePayment.setPaymentGateway(PaymentGateway.RAZORPAY);
        onlinePayment.setPaymentMode(PaymentMode.ONLINE);
        return paymentRepository.save(onlinePayment);
    }

    public Payment createOfflinePayment(User user, Order savedOrder) {
        Payment offlinePayment = new Payment();
        offlinePayment.setUser(user);
        offlinePayment.setOrder(savedOrder);
        offlinePayment.setPaidAmount(BigDecimal.ZERO);
        offlinePayment.setTotalAmount(savedOrder.getSubTotal());
        offlinePayment.setCurrency(PaymentCurrency.INR);
        offlinePayment.setPaymentStatus(PaymentStatus.PENDING);
        offlinePayment.setPaymentMode(PaymentMode.COD);
        return paymentRepository.save(offlinePayment);
    }


    private Map<String, String> buildPaymentResponse(String orderId, BigDecimal amount, Payment payment) {
        Map<String, String> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("rzp_key", razorpayKeyId);
        response.put("amount", amount.toString());
        response.put("currency", PaymentCurrency.INR.toString());
        response.put("paymentMode", payment.getPaymentMode().toString());
        response.put("status", payment.getPaymentStatus().toString());
        return response;
    }


}
