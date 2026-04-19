package payment.payment_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import payment.payment_service.model.Payment;

public interface PaymentRepository  extends JpaRepository<Payment, UUID>{
    Optional<Payment>  findByOrderId(UUID orderId);
}
