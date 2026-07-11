package payment.payment_service.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import payment.payment_service.dto.PaymentRequest;
import payment.payment_service.dto.PaymentResponse;
import payment.payment_service.kafka.PaymentEventProducer;
import payment.payment_service.model.IdempotencyKey;
import payment.payment_service.model.PaymentStatus;
import payment.payment_service.repository.IdempotencyKeyRepository;
import payment.payment_service.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private PaymentEventProducer producer;

    private PaymentService paymentService;

  
   @BeforeEach
   void setUp() {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    paymentService = new PaymentService(
            paymentRepository,
            idempotencyKeyRepository,
            producer,
            objectMapper
    );
}

    @Test
    void shouldCreateSuccessfulPaymentWhenAmountIs900() {

        PaymentRequest request = PaymentRequest.builder()
                .orderId(UUID.randomUUID())
                .amount(900.0)
                .build();

        PaymentResponse response =
                paymentService.createPayment(request, null);

        Assertions.assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus()
        );

        org.mockito.Mockito.verify(paymentRepository)
                .save(org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.verify(producer)
                .publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldCreateFailedPaymentWhenAmountIs1500() {

        PaymentRequest request = PaymentRequest.builder()
                .orderId(UUID.randomUUID())
                .amount(1500.0)
                .build();

        PaymentResponse response =
                paymentService.createPayment(request, null);

        Assertions.assertEquals(
                PaymentStatus.FAILED,
                response.getStatus()
        );

        org.mockito.Mockito.verify(paymentRepository)
                .save(org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.verify(producer)
                .publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowExceptionWhenAmountIs9999() {

        PaymentRequest request = PaymentRequest.builder()
                .orderId(UUID.randomUUID())
                .amount(9999.0)
                .build();

        Assertions.assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(request, null)
        );

        org.mockito.Mockito.verify(
                paymentRepository,
                org.mockito.Mockito.never()
        ).save(org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.verify(
                producer,
                org.mockito.Mockito.never()
        ).publish(org.mockito.ArgumentMatchers.any());
    }
    @Test
void shouldReturnCachedPaymentForExistingIdempotencyKey()
        throws Exception {

    String idempotencyKey = "payment-key-123";
    UUID orderId = UUID.randomUUID();
    UUID paymentId = UUID.randomUUID();

    PaymentResponse cachedResponse = PaymentResponse.builder()
            .id(paymentId)
            .orderId(orderId)
            .amount(900.0)
            .status(PaymentStatus.SUCCESS)
            .createdAt(Instant.now())
            .build();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    IdempotencyKey existingKey = IdempotencyKey.builder()
            .id(idempotencyKey)
            .response(objectMapper.writeValueAsString(cachedResponse))
            .createdAt(Instant.now())
            .build();

    org.mockito.Mockito.when(
            idempotencyKeyRepository.existsById(idempotencyKey)
    ).thenReturn(true);

    org.mockito.Mockito.when(
            idempotencyKeyRepository.findById(idempotencyKey)
    ).thenReturn(Optional.of(existingKey));

    PaymentRequest request = PaymentRequest.builder()
            .orderId(orderId)
            .amount(900.0)
            .build();

    PaymentResponse response =
            paymentService.createPayment(request, idempotencyKey);

    Assertions.assertEquals(paymentId, response.getId());
    Assertions.assertEquals(orderId, response.getOrderId());
    Assertions.assertEquals(PaymentStatus.SUCCESS, response.getStatus());

    org.mockito.Mockito.verify(
            paymentRepository,
            org.mockito.Mockito.never()
    ).save(org.mockito.ArgumentMatchers.any());

    org.mockito.Mockito.verify(
            producer,
            org.mockito.Mockito.never()
    ).publish(org.mockito.ArgumentMatchers.any());
}
}