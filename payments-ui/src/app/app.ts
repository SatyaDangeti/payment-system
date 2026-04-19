import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Payment } from './payment/payment';

@Component({
  selector: 'app-root',
    standalone: true,
  imports: [Payment],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('payments-ui');
}
