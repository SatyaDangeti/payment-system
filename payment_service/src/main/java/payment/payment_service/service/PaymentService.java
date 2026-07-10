package payment.payment_service.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PaymentEventProducer producer;
    private final ObjectMapper objectMapper;

    public PaymentResponse createPayment(
            PaymentRequest request,
            String idempotencyKey) {

        log.info(
                "Starting payment creation. orderId={}, amount={}, idempotencyKey={}",
                request.getOrderId(),
                request.getAmount(),
                idempotencyKey
        );

        // 1. Check existing idempotency key
        if (idempotencyKey != null
                && idempotencyKeyRepository.existsById(idempotencyKey)) {

            log.info("Idempotency hit. key={}", idempotencyKey);

            IdempotencyKey existing = idempotencyKeyRepository
                    .findById(idempotencyKey)
                    .orElseThrow();

            try {

                PaymentResponse cachedResponse =
                        objectMapper.readValue(
                                existing.getResponse(),
                                PaymentResponse.class
                        );

                log.info(
                        "Returning cached payment response. paymentId={}, orderId={}",
                        cachedResponse.getId(),
                        cachedResponse.getOrderId()
                );

                return cachedResponse;

            } catch (JsonProcessingException e) {

                log.error(
                        "Failed to parse idempotency response. key={}",
                        idempotencyKey,
                        e
                );

                throw new RuntimeException(
                        "Failed to parse idempotency response",
                        e
                );
            }
        }

        // 2. Technical failure simulation for Retry + DLQ
        if (request.getAmount() == 9999) {

            log.error(
                    "Simulated payment processing failure. orderId={}",
                    request.getOrderId()
            );

            throw new RuntimeException(
                    "Simulated payment processing failure"
            );
        }

        // 3. Determine payment status
        PaymentStatus paymentStatus =
                request.getAmount() > 1000
                        ? PaymentStatus.FAILED
                        : PaymentStatus.SUCCESS;

        log.info(
                "Payment status determined. orderId={}, amount={}, status={}",
                request.getOrderId(),
                request.getAmount(),
                paymentStatus
        );

        // 4. Create payment
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(paymentStatus)
                .createdAt(Instant.now())
                .build();

        // 5. Save payment
        paymentRepository.save(payment);

        log.info(
                "Payment saved successfully. paymentId={}, orderId={}, status={}",
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus()
        );

        // 6. Create payment response
        PaymentResponse response = PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();

        // 7. Save idempotency response
        if (idempotencyKey != null) {

            try {

                String responseJson =
                        objectMapper.writeValueAsString(response);

                IdempotencyKey key = IdempotencyKey.builder()
                        .id(idempotencyKey)
                        .response(responseJson)
                        .createdAt(Instant.now())
                        .build();

                idempotencyKeyRepository.save(key);

                log.info(
                        "Idempotency response saved. key={}, paymentId={}",
                        idempotencyKey,
                        payment.getId()
                );

            } catch (JsonProcessingException e) {

                log.error(
                        "Failed to save idempotency response. key={}",
                        idempotencyKey,
                        e
                );

                throw new RuntimeException(
                        "Failed to save idempotency response",
                        e
                );
            }
        }

        // 8. Create payment event
        PaymentEvent event = PaymentEvent.builder()
                .orderId(payment.getOrderId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .build();

        // 9. Publish payment event
        log.info(
                "Publishing payment event to Kafka. orderId={}, status={}",
                payment.getOrderId(),
                payment.getStatus()
        );

        producer.publish(event);

        log.info(
                "Payment event published successfully. orderId={}, status={}",
                payment.getOrderId(),
                payment.getStatus()
        );

        return response;
    }

    @Cacheable(value = "payments", key = "#id")
    public PaymentResponse getPayment(UUID id) {

        log.info("Fetching payment. paymentId={}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found: " + id
                        )
                );

        log.info(
                "Payment fetched successfully. paymentId={}, orderId={}, status={}",
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus()
        );

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}