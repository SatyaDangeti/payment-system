import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
    private baseUrl = 'http://localhost:8080/api';
    constructor(private http: HttpClient) {}

    createOrder(amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/orders`, { amount });
  }
   createPayment(orderId: string, amount: number, idemKey: string): Observable<any> {
    const headers = new HttpHeaders({ 'Idempotency-Key': idemKey });
    return this.http.post(`${this.baseUrl}/payments`, { orderId, amount }, { headers });
  }

  getPayment(id: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/payments/${id}`);
  }


}
