import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface OrderResponse {
  id: string;
  amount: number;
  status: string;
  createdAt: string;
}

export interface PaymentResponse {
  id: string;
  orderId: string;
  amount: number;
  status: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  createOrder(amount: number): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/orders`, { amount });
  }

  getOrder(id: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/orders/${id}`);
  }

  createPayment(orderId: string, amount: number, idemKey: string): Observable<PaymentResponse> {
    const headers = new HttpHeaders({ 'Idempotency-Key': idemKey });

    return this.http.post<PaymentResponse>(
      `${this.baseUrl}/payments`,
      { orderId, amount },
      { headers }
    );
  }

  getPayment(id: string): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.baseUrl}/payments/${id}`);
  }
}