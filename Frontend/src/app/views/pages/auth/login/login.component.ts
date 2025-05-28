import { NgIf, NgStyle } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../services/auth/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-login',
    imports: [
        NgStyle,
        RouterLink,
        FormsModule,
        NgIf
    ],
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {

  returnUrl: any;
  errorMessage: string;
  username: string = '';
  password: string = '';

  constructor(private router: Router, private route: ActivatedRoute, private authService: AuthService) {}

  ngOnInit(): void {
    // Get the return URL from the route parameters, or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    const token = this.authService.getToken();
    if (token) {
      this.router.navigate([this.returnUrl]);
    }
  }

  onLoggedin(e: Event) {
    this.errorMessage = ''; 

    if (!this.username || !this.password) {
      this.errorMessage = 'Please enter both username and password.';
      return;
    }

    // Authentifizierung über den AuthService
    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        // Token und Ablaufzeit speichern
        const accessToken = response.token;
        localStorage.setItem('accessToken', accessToken);

        // Weiterleitung zur Startseite
        this.router.navigate([this.returnUrl])
      },
      error: async (err) => {
        // Fehlerbehandlung für ungültige Login-Daten
        this.errorMessage = 'Invalid username or password. Please try again.';
      },
    });
  }

}
