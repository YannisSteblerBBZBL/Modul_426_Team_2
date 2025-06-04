import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AppEvent, EventDayDisplay } from '../models/appEvent.interface';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) { }

  getAllEvents(): Observable<AppEvent[]> {
    return this.http.get<AppEvent[]>(this.apiUrl);
  }

  getEventById(id: string): Observable<AppEvent> {
    return this.http.get<AppEvent>(`${this.apiUrl}/${id}`);
  }

  createEvent(event: AppEvent): Observable<AppEvent> {
    return this.http.post<AppEvent>(this.apiUrl, event);
  }

  updateEvent(id: string, event: AppEvent): Observable<AppEvent> {
    return this.http.put<AppEvent>(`${this.apiUrl}/${id}`, event);
  }

  deleteEvent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateEventDays(id: string, eventDays: { [key: string]: number }[]): Observable<AppEvent> {
    return this.http.put<AppEvent>(`${this.apiUrl}/eventDays/${id}`, eventDays);
  }
}
