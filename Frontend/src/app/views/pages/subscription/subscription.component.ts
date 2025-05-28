import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { environment } from '../../../../environments/environment';

@Component({
    selector: 'app-subscription',
    imports: [],
    templateUrl: './subscription.component.html',
    styleUrl: './subscription.component.scss'
})
export class SubscriptionComponent {
  private readonly baseUrl = environment.apiUrl || 'http://localhost:1000/api';

  constructor(private http: HttpClient) {}

  startCheckout() {
    this.http
      .post(
        `${this.baseUrl}/subscription/start-checkout`,
        {},
        {
          responseType: 'text',
          withCredentials: true,
        }
      )
      .subscribe({
        next: (url: string) => {
          console.log('Redirecting to Stripe Checkout:', url);
          window.location.href = url;
        },
        error: (err) => {
          console.error('Fehler beim Starten des Checkouts:', err);
        },
      });
  }

  cancelSubscription() {
    this.http
      .post(
        `${this.baseUrl}/subscription/cancel`,
        {},
        {
          responseType: 'text',
          withCredentials: true,
        }
      )
      .subscribe({
        next: (msg: string) => {
          alert(msg);
        },
        error: (err) => {
          console.error('Fehler beim Kündigen des Abos:', err);
        },
      });
  }
}
