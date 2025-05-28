import { Component } from '@angular/core';

@Component({
    selector: 'app-cancel',
    imports: [],
    templateUrl: './cancel.component.html',
    styleUrl: './cancel.component.scss'
})
export class CancelComponent {
  message: string = 'Der Checkout wurde abgebrochen. Bitte versuche es erneut.';
}
