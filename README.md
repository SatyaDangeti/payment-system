# Payment System (Microservices Architecture)

## 🚀 Tech Stack
- Java Spring Boot
- Apache Kafka (Event Streaming)
- Redis (Caching + Idempotency)
- PostgreSQL (Database)
- Docker

## 🧠 Architecture
Order Service → Kafka → Payment Service → Kafka → Notification Service

## 🔥 Features
- Payment Intents (Create / Confirm / Cancel)
- Idempotency Keys (Stripe-style)
- Event-driven architecture using Kafka
- Distributed flow (Saga pattern)
- Retry + failure handling (foundation ready)
- Redis caching

## ⚙️ How to Run

### 1. Start infrastructure
```bash
docker-compose up -d
