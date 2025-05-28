import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
    private readonly baseUrl = `${environment.apiUrl || 'http://localhost:8080/api'}/users`;

    constructor(private http: HttpClient) {}

    async getAllUsers(): Promise<User[]> {
        return lastValueFrom(this.http.get<User[]>(this.baseUrl));
    }

    async getUserById(userId: string): Promise<User> {
        return lastValueFrom(this.http.get<User>(`${this.baseUrl}/${userId}`));
    }
}
