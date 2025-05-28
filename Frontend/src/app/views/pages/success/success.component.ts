import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-success',
    imports: [],
    templateUrl: './success.component.html',
    styleUrl: './success.component.scss'
})
export class SuccessComponent implements OnInit {
  sessionId: string | null = null;
  message: string = 'Danke für deinen Checkout! Dein Abo wurde erfolgreich abgeschlossen.';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    // session_id aus der URL abrufen
    this.sessionId = this.route.snapshot.queryParamMap.get('session_id');

    if (this.sessionId) {
      this.message = `Der Checkout war erfolgreich! Session ID: ${this.sessionId}`;
    }
  }
}
