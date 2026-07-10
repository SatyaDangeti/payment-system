import { ChangeDetectorRef, Component } from '@angular/core';
import { ApiService, OrderResponse } from '../api-service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment.html',
  styleUrl: './payment.css',
})
export class Payment {
  amount: number = 500;
  order?: OrderResponse;
  loading = false;
  error = '';
  intervalId: any;

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  createOrder() {
    this.loading = true;
    this.error = '';
    this.order = undefined;

    if (this.intervalId) {
      clearInterval(this.intervalId);
    }

    this.api.createOrder(this.amount).subscribe({
      next: (order) => {
        console.log('ORDER RESPONSE:', order);

        this.order = order;
        this.loading = false;
        this.cdr.detectChanges();

        this.startPolling(order.id);
      },
      error: (err) => {
        console.error('ORDER ERROR:', err);
        this.error = 'Failed to create order. Check API Gateway and backend services.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  startPolling(orderId: string) {
    this.intervalId = setInterval(() => {
      this.api.getOrder(orderId).subscribe({
        next: (updatedOrder) => {
          console.log('LIVE STATUS:', updatedOrder.status);

          this.order = updatedOrder;
          this.loading = false;

          if (
            updatedOrder.status !== 'CREATED' &&
            updatedOrder.status !== 'PROCESSING'
          ) {
            clearInterval(this.intervalId);
          }

          if (updatedOrder.status === 'CANCELLED') {
            this.error = 'Payment failed. Order cancelled.';
          }

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('POLLING ERROR:', err);
          clearInterval(this.intervalId);
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
    }, 1500);
  }
}