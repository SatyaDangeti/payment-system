package payment.payment_service.service;



import java.time.Instant;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import payment.payment_service.dto.PaymentEvent;
import payment.payment_service.dto.PaymentRequest;
import payment.payment_service.dto.PaymentResponse;
import payment.payment_service.exception.ResourceNotFoundException;
import payment.payment_service.kafka.PaymentEventProducer;
import payment.payment_service.model.IdempotencyKey;
import payment.payment_service.model.Payment;
import payment.payment_service.model.PaymentStatus;
import payment.payment_service.repository.IdempotencyKeyRepository;
import payment.payment_service.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PaymentEventProducer producer;
    private final ObjectMapper objectMapper;

    public PaymentResponse createPayment(PaymentRequest request, String idempotencyKey) {

        System.out.println("💳 Starting payment creation");
        System.out.println("🧾 Order ID: " + request.getOrderId());
        System.out.println("💰 Amount: " + request.getAmount());
        System.out.println("🔑 Idempotency Key: " + idempotencyKey);

        if (idempotencyKey != null && idempotencyKeyRepository.existsById(idempotencyKey)) {
            System.out.println("♻️ Idempotency hit for key: " + idempotencyKey);

            IdempotencyKey existing = idempotencyKeyRepository.findById(idempotencyKey).orElseThrow();
            try {
                PaymentResponse cachedResponse = objectMapper.readValue(existing.getResponse(), PaymentResponse.class);
                System.out.println("✅ Returning cached payment response for order: " + cachedResponse.getOrderId());
                return cachedResponse;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse idempotent response", e);
            }
        }

        // Mock saga failure rule:
        // amount <= 1000 => SUCCESS
        // amount > 1000  => FAILED
        PaymentStatus finalStatus = request.getAmount() > 1000
                ? PaymentStatus.FAILED
                : PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(finalStatus)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        System.out.println("✅ Payment saved in DB");
        System.out.println("💵 Payment ID: " + payment.getId());
        System.out.println("📦 Payment Status: " + payment.getStatus());

        PaymentResponse response = PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();

       if (idempotencyKey != null && idempotencyKeyRepository.existsById(idempotencyKey)) {
    System.out.println("♻️ Idempotency hit for key: " + idempotencyKey);

    IdempotencyKey existing = idempotencyKeyRepository.findById(idempotencyKey).orElseThrow();
    try {
        PaymentResponse cachedResponse = objectMapper.readValue(existing.getResponse(), PaymentResponse.class);
        System.out.println("✅ Returning cached payment response for order: " + cachedResponse.getOrderId());
        return cachedResponse;
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to parse idempotent response", e);
    }
}

// Simulate technical failure for retry + DLQ demo
if (request.getAmount() == 9999) {
    System.out.println("💥 Simulated technical failure for order: " + request.getOrderId());
    throw new RuntimeException("Simulated payment processing failure");
}

        PaymentEvent event = PaymentEvent.builder()
                .orderId(payment.getOrderId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .build();

        System.out.println("🚀 Publishing payment event to Kafka for order: " + payment.getOrderId());
        producer.publish(event);
        System.out.println("✅ Payment event published successfully");

        return response;
    }

    @Cacheable(value = "payments", key = "#id")
    public PaymentResponse getPayment(UUID id) {
        System.out.println("🔍 Fetching payment by ID: " + id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));

        System.out.println("✅ Payment fetched from DB: " + payment.getId());

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}