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
  private autoApiUrl = `${environment.apiUrl}/auto-assignments`;

  constructor(private http: HttpClient) { }

  getAllAssignments(): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(this.apiUrl);
  }

  getAssignmentById(id: string): Observable<Assignment> {
    return this.http.get<Assignment>(`${this.apiUrl}/${id}`);
  }

  createAssignment(assignment: Assignment): Observable<Assignment> {
    return this.http.post<Assignment>(this.apiUrl, assignment);
  }

  updateAssignment(id: string, assignment: Assignment): Observable<Assignment> {
    return this.http.put<Assignment>(`${this.apiUrl}/${id}`, assignment);
  }

  deleteAssignment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAssignmentsByEvent(eventId: string): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(`${this.apiUrl}/event/${eventId}`);
  }

  generateAutomaticAssignments(eventId: string): Observable<Assignment[]> {
    return this.http.post<Assignment[]>(`${this.autoApiUrl}/${eventId}`, {});
  }
}
