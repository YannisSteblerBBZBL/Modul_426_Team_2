import { NgStyle } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../services/auth/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-register',
    imports: [
        NgStyle,
        RouterLink,
        FormsModule
    ],
    templateUrl: './register.component.html',
    styleUrl: './register.component.scss'
})
export class RegisterComponent {
  username: string = '';
  firstname: string = '';
  lastname: string = '';
  email: string = '';
  birthdate: string = '';
  password: string = '';
  errorMessage: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Überprüfen, ob der Benutzer bereits angemeldet ist
    const token = this.authService.getToken();
    if (token) {
      const tokenExpiry = localStorage.getItem('tokenExpiry');
      if (tokenExpiry && Date.now() < parseInt(tokenExpiry)) {
        // Token ist gültig, Weiterleitung zur Startseite
        this.router.navigate(['/']).then(() => {
          window.location.reload();
        });
      }
    }
  }

  onRegister() {
    this.errorMessage = '';

    if (
      !this.username ||
      !this.firstname ||
      !this.lastname ||
      !this.email ||
      !this.birthdate ||
      !this.password
    ) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    this.authService.register(
      this.username,
      this.firstname,
      this.lastname,
      this.email,
      this.birthdate,
      this.password
    ).subscribe({
      next: (response) => {
        const accessToken = response.token;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('tokenExpiry', (Date.now() + 12 * 60 * 60 * 1000).toString());

        this.router.navigate(['/']).then(() => {
          window.location.reload();
        });
      },
      error: async (err) => {
        const message =
          err?.error?.message || 'Registration failed. Please try again.';
        this.errorMessage = message;
      }
    });
  }
}
