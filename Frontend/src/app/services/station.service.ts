import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Station } from '../models/station.interface';

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

  createStation(station: Station): Observable<Station> {
    return this.http.post<Station>(this.apiUrl, station);
  }

  updateStation(id: string, station: Station): Observable<Station> {
    return this.http.put<Station>(`${this.apiUrl}/${id}`, station);
  }

  deleteStation(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
