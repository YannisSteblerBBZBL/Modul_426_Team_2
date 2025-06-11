import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Helper } from '../models/helper.interface';

@Injectable({
  providedIn: 'root'
})
export class HelperService {
  private apiUrl = `${environment.apiUrl}/helpers`;

  constructor(private http: HttpClient) { }

  getAllHelpers(): Observable<Helper[]> {
    return this.http.get<Helper[]>(this.apiUrl);
  }

  getHelperById(id: string): Observable<Helper> {
    return this.http.get<Helper>(`${this.apiUrl}/${id}`);
  }

  createHelper(helper: Helper): Observable<Helper> {
    return this.http.post<Helper>(this.apiUrl, helper);
  }

  createPublicHelper(eventId: string, helper: Helper): Observable<Helper> {
    return this.http.post<Helper>(`${this.apiUrl}/public/${eventId}`, helper);
  }

  updateHelper(id: string, helper: Helper): Observable<Helper> {
    return this.http.put<Helper>(`${this.apiUrl}/${id}`, helper);
  }

  deleteHelper(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
