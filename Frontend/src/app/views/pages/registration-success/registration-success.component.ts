import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registration-success',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="card border-success">
            <div class="card-body text-center">
              <div class="mb-4">
                <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
              </div>
              <h2 class="card-title text-success mb-4">Registrierung erfolgreich!</h2>
              <p class="card-text mb-4">
                Vielen Dank für Ihre Registrierung als Helfer. Ihre Anmeldung wurde erfolgreich gespeichert.
              </p>
              <p class="text-muted">
                Sie können diese Seite nun schließen.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class RegistrationSuccessComponent {} 