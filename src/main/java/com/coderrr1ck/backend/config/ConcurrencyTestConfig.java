//package com.coderrr1ck.backend.config;
//
//import com.coderrr1ck.backend.product.Product;
//import com.coderrr1ck.backend.product.ProductRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Configuration
//public class ConcurrencyTestConfig {
//
//    private static final Logger log =
//            LoggerFactory.getLogger(ConcurrencyTestConfig.class);
//
//    /*
//     * Product stock should be 1
//     */
//    private static final UUID PRODUCT_ID = UUID.fromString("d0d12bf6-e215-40ec-a645-4d400fa1ac45");
//
//    private static final int INITIAL_STOCK = 3;
//
//    /*
//     * REAL USERS FROM DB
//     */
//    private static final List<String> USER_IDS = List.of(
//            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VydGVzdDFAZ21haWwuY29tIiwiaWF0IjoxNzc4NDExODE4LCJleHAiOjE3Nzg0MTU0MTh9.r2K5pDBd38AUBKa5ejFLgy62lTQ2zVQ_O5UNqSlIeKs",
//            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VydGVzdDJAZ21haWwuY29tIiwiaWF0IjoxNzc4NDExODM2LCJleHAiOjE3Nzg0MTU0MzZ9.p7ZoKpWnnAXppdmnWHgrZSCsLkEmaM_MWGUUeMpfLco",
//            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VydGVzdDNAZ21haWwuY29tIiwiaWF0IjoxNzc4NDExODU3LCJleHAiOjE3Nzg0MTU0NTd9.2Chv3nAzCZjpYwsMqIg6M-Na8EeI7a4EAZgc4pTKBpk",
//            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VydGVzdDRAZ21haWwuY29tIiwiaWF0IjoxNzc4NDExNzYyLCJleHAiOjE3Nzg0MTUzNjJ9.QJtGDjZW3HX20cAqMpQ3asYTQpC5beDYo94XwXF3gC8",
//            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VydGVzdDVAZ21haWwuY29tIiwiaWF0IjoxNzc4NDExNzk1LCJleHAiOjE3Nzg0MTUzOTV9.aXJpfl49Wvu7GjcVCOxcNBrsBaaFRRLm46yVePoigbo"
//    );
//
//    private final ProductRepository productRepository;
//
//    public ConcurrencyTestConfig(ProductRepository productRepository) {
//        this.productRepository = productRepository;
//    }
//
//    @Bean
//    public CommandLineRunner concurrencyRunner() {
//
//        return args -> {
//
//            log.info("=== STARTING CONCURRENCY TEST ===");
//
//            ExecutorService executor =
//                    Executors.newFixedThreadPool(USER_IDS.size());
//
//            CountDownLatch latch =
//                    new CountDownLatch(USER_IDS.size());
//
//            AtomicInteger cartSuccess = new AtomicInteger();
//            AtomicInteger cartFailure = new AtomicInteger();
//            AtomicInteger orderSuccess = new AtomicInteger();
//            AtomicInteger orderFailure = new AtomicInteger();
//
//            HttpClient client = HttpClient.newHttpClient();
//
//            for (String userId : USER_IDS) {
//
//                executor.submit(() -> {
//
//                    try {
//
//                        /*
//                         * ---------------------------------------
//                         * STEP 1 -> ADD PRODUCT TO CART
//                         * ---------------------------------------
//                         */
//
//                        String cartPayload = """
//                                {
//                                  "productId": "%s"
//                                }
//                                """.formatted(PRODUCT_ID);
//
//                        HttpRequest cartRequest =
//                                HttpRequest.newBuilder()
//                                        .uri(
//                                                URI.create(
//                                                        "http://localhost:8080/api/v1/cart"
//                                                )
//                                        )
//                                        .header("Content-Type", "application/json")
//                                        .header("Authorization", "Bearer "+userId)
//                                        .POST(
//                                                HttpRequest.BodyPublishers
//                                                        .ofString(cartPayload)
//                                        )
//                                        .build();
//
//                        HttpResponse<String> cartResponse =
//                                client.send(
//                                        cartRequest,
//                                        HttpResponse.BodyHandlers.ofString()
//                                );
//
//                        System.out.println("[CART RESPONSE]"+cartResponse.body());
//
//                        if (cartResponse.statusCode() < 200 ||
//                                cartResponse.statusCode() >= 400) {
//
//                            log.error(
//                                    "[CART FAILED] userId={} status={} body={}",
//                                    userId,
//                                    cartResponse.statusCode(),
//                                    cartResponse.body()
//                            );
//                            cartFailure.incrementAndGet();
//                        }
//
//                        if(cartResponse.statusCode() == 200 || cartResponse.statusCode() == 201){
//                            log.info(
//                                    "[CART ADDED] userId={} status={} body={}",
//                                    userId,
//                                    cartResponse.statusCode(),
//                                    cartResponse.body()
//                            );
//
//                            cartSuccess.incrementAndGet();
//
//                        }
//
//                        /*
//                         * ---------------------------------------
//                         * STEP 2 -> PLACE ORDER CONCURRENTLY
//                         * ---------------------------------------
//                         */
//
//                        String orderPayload = """
//                            {
//                              "idempotencyKey": "%s",
//                              "addressId": "%s"
//                            }
//                            """.formatted(UUID.randomUUID(),
//                                UUID.randomUUID());
//
//                        HttpRequest orderRequest =
//                                HttpRequest.newBuilder()
//                                        .uri(
//                                                URI.create(
//                                                        "http://localhost:8080/api/v1/orders"
//                                                )
//                                        )
//                                        .header("Content-Type", "application/json")
//
//                                        .header("Authorization", "Bearer "+userId)
//                                        .POST(
//                                                HttpRequest.BodyPublishers
//                                                        .ofString(orderPayload)
//                                        )
//                                        .build();
//
//                        HttpResponse<String> orderResponse =
//                                client.send(
//                                        orderRequest,
//                                        HttpResponse.BodyHandlers.ofString()
//                                );
//
//
//                        System.out.println("[ORDER RESPONSE]"+orderResponse.body());
//
//                        int status = orderResponse.statusCode();
//
//                        if (status >= 200 && status < 300) {
//
//                            int sc = orderSuccess.incrementAndGet();
//
//                            log.info(
//                                    "[ORDER SUCCESS] userId={} successCount={}",
//                                    userId,
//                                    sc
//                            );
//
//                        } else {
//
//                            int fc = orderFailure.incrementAndGet();
//
//                            log.warn(
//                                    "[ORDER FAILED] userId={} failureCount={} status={} body={}",
//                                    userId,
//                                    fc,
//                                    status,
//                                    orderResponse.body()
//                            );
//                        }
//
//                    } catch (Exception e) {
//
//                        int fc = orderFailure.incrementAndGet();
//
//                        log.error(
//                                "[ERROR] userId={} failureCount={} error={}",
//                                userId,
//                                fc,
//                                e.getMessage()
//                        );
//
//                    } finally {
//                        latch.countDown();
//                    }
//                });
//            }
//
//            latch.await();
//
//            executor.shutdown();
//            executor.awaitTermination(1, TimeUnit.MINUTES);
//
//            /*
//             * FINAL STOCK CHECK
//             */
//
//            List<Product> products =
//                    productRepository.findAllById(List.of(PRODUCT_ID));
//
//            int finalStock =
//                    products.isEmpty()
//                            ? -1
//                            : products.get(0).getAvailableStock();
//
//            int cartOk = cartSuccess.get();
//            int orderOk = orderSuccess.get();
//            int orderFail = orderFailure.get();
//            int cartFail = cartFailure.get();
//
//            log.info("=== TEST FINISHED ===");
//
//            log.info("Cart Success  : {}", cartOk);
//            log.info("Cart Failure  : {}", cartFail);
//            log.info("Order Success : {}", orderOk);
//            log.info("Order Failure : {}", orderFail);
//            log.info("Final Stock   : {}", finalStock);
//
//            /*
//             * EXPECTED:
//             *
//             * orderSuccess = 1
//             * orderFailure = 4
//             * finalStock = 0
//             */
//
//            if (orderOk > INITIAL_STOCK) {
//
//                log.error(
//                        "OVERSOLD DETECTED! successOrders={} initialStock={}",
//                        orderOk,
//                        INITIAL_STOCK
//                );
//            }
//
//            if (finalStock < 0) {
//
//                log.error(
//                        "NEGATIVE STOCK DETECTED! finalStock={}",
//                        finalStock
//                );
//            }
//
//            if (orderOk + finalStock != INITIAL_STOCK) {
//
//                log.error(
//                        "STOCK INCONSISTENCY! successOrders + finalStock = {}",
//                        orderOk + finalStock
//                );
//
//            } else {
//
//                log.info(
//                        "STOCK INVARIANT HOLDS -> {} + {} = {}",
//                        orderOk,
//                        finalStock,
//                        INITIAL_STOCK
//                );
//            }
//        };
//    }
//}