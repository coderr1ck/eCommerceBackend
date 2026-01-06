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
//import java.net.http.*;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Configuration
//public class ConcurrencyTestConfig {
//
//    private static final Logger log = LoggerFactory.getLogger(ConcurrencyTestConfig.class);
//
//    private static final String TEST_PRODUCT_ID = "6956c7e9692829bfa08cabb9";
//    private static final String TEST_USER_ID = "6952c69556383bfbedac8572";
//    private static final int INITIAL_STOCK = 5;   // set to the real initial stock in DB
//    private static final int THREADS = 20;        // concurrent requests
//
//    private final ProductRepository productRepository;
//
//    public ConcurrencyTestConfig(ProductRepository productRepository) {
//        this.productRepository = productRepository;
//    }
//
//    @Bean
//    public CommandLineRunner orderConcurrencyRunner() {
//        return args -> {
//            log.info("=== Starting concurrency test for product {} with initialStock={} ===",
//                    TEST_PRODUCT_ID, INITIAL_STOCK);
//
//            ExecutorService pool = Executors.newFixedThreadPool(THREADS);
//            AtomicInteger successCount = new AtomicInteger(0);
//            AtomicInteger failureCount = new AtomicInteger(0);
//
//            String url = "http://localhost:8080/api/v1/orders";
//            String payload = """
//                    {
//                      "userId": "%s",
//                      "items": [
//                        { "productId": "%s", "quantity": 1 }
//                      ]
//                    }
//                    """.formatted(TEST_USER_ID, TEST_PRODUCT_ID);
//
//            for (int i = 0; i < THREADS; i++) {
//                pool.submit(() -> {
//                    HttpClient client = HttpClient.newHttpClient();
//                    HttpRequest request = HttpRequest.newBuilder()
//                            .uri(URI.create(url))
//                            .header("Content-Type", "application/json")
//                            .POST(HttpRequest.BodyPublishers.ofString(payload))
//                            .build();
//                    try {
//                        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
//                        int status = resp.statusCode();
//                        if (status >= 200 && status < 300) {
//                            int sc = successCount.incrementAndGet();
//                            log.info("[OK] {} -> status={} successCount={}", Thread.currentThread().getName(), status, sc);
//                        } else {
//                            int fc = failureCount.incrementAndGet();
//                            log.warn("[FAIL] {} -> status={} body={} failureCount={}",
//                                    Thread.currentThread().getName(), status, resp.body(), fc);
//                        }
//                    } catch (Exception e) {
//                        int fc = failureCount.incrementAndGet();
//                        log.error("[ERROR] {} -> exception={}, failureCount={}",
//                                Thread.currentThread().getName(), e.toString(), fc);
//                    }
//                });
//            }
//
//            pool.shutdown();
//            pool.awaitTermination(1, TimeUnit.MINUTES);
//
//            // Read final stock from DB
//            List<Product> products =
//                    productRepository.findAllById(List.of(TEST_PRODUCT_ID));
//            int finalStock = products.isEmpty() ? -1 : products.get(0).getStock();
//
//            int ok = successCount.get();
//            int fail = failureCount.get();
//
//            log.info("=== Concurrency test finished ===");
//            log.info("Successful orders: {}", ok);
//            log.info("Failed orders:     {}", fail);
//            log.info("Final stock for {}: {}", TEST_PRODUCT_ID, finalStock);
//
//            // Simple concurrency sanity check:
//            if (ok > INITIAL_STOCK) {
//                log.error("CONCURRENCY ISSUE: successCount ({}) > initialStock ({})", ok, INITIAL_STOCK);
//            }
//            if (finalStock < 0) {
//                log.error("CONCURRENCY ISSUE: finalStock is negative ({})", finalStock);
//            }
//            if (ok + finalStock > INITIAL_STOCK) {
//                log.error("CONCURRENCY ISSUE: ok + finalStock ({}) > initialStock ({})",
//                        ok + finalStock, INITIAL_STOCK);
//            } else {
//                log.info("Stock invariant holds: ok + finalStock = {} (<= initialStock {})",
//                        ok + finalStock, INITIAL_STOCK);
//            }
//        };
//    }
//}
