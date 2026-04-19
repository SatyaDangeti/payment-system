import { Component } from '@angular/core';
import { ApiService } from '../api-service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, JsonPipe } from '@angular/common';

@Component({
  selector: 'app-payment',
    standalone: true,
  imports: [ReactiveFormsModule,CommonModule,FormsModule,JsonPipe],
  templateUrl: './payment.html',
  styleUrl: './payment.css',
})
export class Payment {
   amount = 500;
  order: any;
  payment: any;

  constructor(private api: ApiService) {}

  createFlow() {
    this.api.createOrder(this.amount).subscribe(order => {
      this.order = order;
      const idemKey = crypto.randomUUID();
      this.api.createPayment(order.id, order.amount, idemKey).subscribe(payment => {
        this.payment = payment;
      });
    });
  }

}
