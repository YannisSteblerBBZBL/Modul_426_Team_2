import { Component } from '@angular/core';

@Component({
  selector: 'app-cancel',
  standalone: true,
  imports: [],
  templateUrl: './cancel.component.html',
  styleUrl: './cancel.component.scss',
})
export class CancelComponent {
  message: string = 'Der Checkout wurde abgebrochen. Bitte versuche es erneut.';
}
