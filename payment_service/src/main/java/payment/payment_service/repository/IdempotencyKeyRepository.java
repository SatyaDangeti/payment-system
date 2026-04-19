package payment.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import payment.payment_service.model.IdempotencyKey;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    
}
