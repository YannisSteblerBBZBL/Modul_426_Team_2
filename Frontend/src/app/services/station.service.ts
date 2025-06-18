import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Station } from '../models/station.interface';
import { Assignment } from '../models/assignment.interface';

@Injectable({
  providedIn: 'root'
})
export class StationService {
  private apiUrl = `${environment.apiUrl}/stations`;

  constructor(private http: HttpClient) { }

  getAllStations(): Observable<Station[]> {
    return this.http.get<Station[]>(this.apiUrl);
  }

  getStationById(id: string): Observable<Station> {
    return this.http.get<Station>(`${this.apiUrl}/${id}`);
  }

  getStationsByEventId(eventId: string): Observable<Station[]> {
    return this.http.get<Station[]>(`${this.apiUrl}/event/${eventId}`);
  }

  createStation(station: Station): Observable<Station> {
    return this.http.post<Station>(this.apiUrl, station);
  }

  updateStation(id: string, station: Station): Observable<Station> {
    return this.http.put<Station>(`${this.apiUrl}/${id}`, station);
  }

  deleteStation(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  forceDeleteStation(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/force`);
  }

  getAssignmentsByStationId(id: string): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(`${this.apiUrl}/${id}/assignments`);
  }
}
