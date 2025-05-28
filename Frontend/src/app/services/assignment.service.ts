import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Assignment } from '../models/assignment.interface';

@Injectable({
  providedIn: 'root'
})
export class AssignmentService {
  private apiUrl = `${environment.apiUrl}/assignments`;

  constructor(private http: HttpClient) { }

  getAssignmentsByEvent(eventId: string): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(`${this.apiUrl}/event/${eventId}`);
  }

  generateAutomaticAssignments(eventId: string): Observable<Assignment[]> {
    return this.http.post<Assignment[]>(`${this.apiUrl}/generate/${eventId}`, {});
  }
}
