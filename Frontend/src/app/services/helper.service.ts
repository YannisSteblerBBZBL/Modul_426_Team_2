import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Helper } from '../models/helper.interface';

@Injectable({
  providedIn: 'root'
})
export class HelperService {
  private apiUrl = `${environment.apiUrl}/api/helpers`;

  constructor(private http: HttpClient) { }

  getAllHelpers(): Observable<Helper[]> {
    return this.http.get<Helper[]>(this.apiUrl);
  }

  getHelperById(id: string): Observable<Helper> {
    return this.http.get<Helper>(`${this.apiUrl}/${id}`);
  }

  updateHelperPreferences(id: string, preferences: string[]): Observable<Helper> {
    return this.http.put<Helper>(`${this.apiUrl}/${id}/preferences`, { preferences });
  }

  updateHelperPresence(id: string, presence: number[]): Observable<Helper> {
    return this.http.put<Helper>(`${this.apiUrl}/${id}/presence`, { presence });
  }
} 