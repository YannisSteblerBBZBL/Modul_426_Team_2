import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Station } from '../models/station.interface';

@Injectable({
  providedIn: 'root'
})
export class StationService {
  private apiUrl = `${environment.apiUrl}/api/stations`;

  constructor(private http: HttpClient) { }

  getAllStations(): Observable<Station[]> {
    return this.http.get<Station[]>(this.apiUrl);
  }

  updateStationHelperRequirements(id: string, neededHelpers: number, is18Plus: boolean): Observable<Station> {
    return this.http.put<Station>(`${this.apiUrl}/${id}/requirements`, { neededHelpers, is18Plus });
  }
} 