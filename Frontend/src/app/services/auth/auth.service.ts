import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl || 'http://localhost:8080/api'}/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  login(identifier: string, password: string): Observable<any> {
    // Allow admin/admin123 to login without backend call
    if (identifier === 'admin' && password === 'admin123') {
      // Simulate a successful login response with a fake token
      return new Observable(observer => {
        observer.next({
          accessToken: 'fake-jwt-token-for-admin',
          user: { username: 'admin', role: 'admin' }
        });
        observer.complete();
      });
    }

    return this.http.post(
      `${this.baseUrl}/login`,
      { identifier, password },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  register(
    username: string,
    firstname: string,
    lastname: string,
    email: string,
    birthdate: string,
    password: string
  ): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/register`,
      { username, firstname, lastname, email, birthdate, password },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  getUserIdFromToken(): string | null {
    const token = this.getToken();
    if (!token) {
      console.error('No access token found in local storage');
      this.router.navigate(['/auth/login']);
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId;
    } catch (e) {
      console.error('Error decoding token:', e);
      return null;
    }
  }

  getExpDateFromToken(): string | null {
    const token = this.getToken();
    if (!token) {
      console.error('No access token found in local storage');
      this.router.navigate(['/auth/login']);
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp;
    } catch (e) {
      console.error('Error decoding token:', e);
      return null;
    }
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }
}
