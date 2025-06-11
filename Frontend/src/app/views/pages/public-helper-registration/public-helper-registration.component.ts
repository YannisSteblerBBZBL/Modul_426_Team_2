import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Helper } from '../../../models/helper.interface';
import { HelperService } from '../../../services/helper.service';
import { EventService } from '../../../services/event.service';
import { AppEvent } from '../../../models/appEvent.interface';
import { Station } from '../../../models/station.interface';
import { StationService } from '../../../services/station.service';
import { Event } from '../../../models/event.interface';

@Component({
  selector: 'app-public-helper-registration',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './public-helper-registration.component.html',
  styleUrl: './public-helper-registration.component.scss'
})
export class PublicHelperRegistrationComponent implements OnInit {
  event?: Event;
  stations: Station[] = [];
  newHelper: Helper = this.getEmptyHelper();
  loading = false;
  error = '';
  availableDays: number[] = [];
  selectedDays: number[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private helperService: HelperService,
    private eventService: EventService,
    private stationService: StationService
  ) {}

  ngOnInit(): void {
    const eventId = this.route.snapshot.paramMap.get('id');
    if (!eventId) {
      this.error = 'No event ID provided';
      return;
    }

    this.loading = true;
    this.eventService.getEventById(eventId).subscribe({
      next: (event) => {
        if (!event.helperRegistrationOpen) {
          this.error = 'Helper registration is not open for this event';
          this.loading = false;
          return;
        }

        this.event = event;
        if (event.eventDays) {
          this.availableDays = event.eventDays.map((day, index) => index + 1);
        }
        this.loadStations();
      },
      error: (error) => {
        console.error('Error loading event:', error);
        this.error = 'Error loading event details';
        this.loading = false;
      }
    });
  }

  private loadStations(): void {
    if (!this.event?.id) return;
    
    this.stationService.getStationsByEventId(this.event.id).subscribe({
      next: (stations) => {
        this.stations = stations;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Fehler beim Laden der Stationen';
        this.loading = false;
      }
    });
  }

  private getEmptyHelper(): Helper {
    return {
      firstname: '',
      lastname: '',
      email: '',
      birthdate: '',
      age: '',
      presence: [],
      preferences: [],
      preferencedHelpers: [],
      preferencedStations: []
    };
  }

  onBirthdateChange(): void {
    this.newHelper.age = this.calculateAge(this.newHelper.birthdate);
  }

  calculateAge(birthdate: string): string {
    const birth = new Date(birthdate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    
    return age.toString();
  }

  togglePresence(day: number): void {
    const index = this.newHelper.presence.indexOf(day);
    if (index === -1) {
      this.newHelper.presence.push(day);
    } else {
      this.newHelper.presence.splice(index, 1);
    }
  }

  isPresent(day: number): boolean {
    return this.newHelper.presence.includes(day);
  }

  togglePreferencedStation(stationId: string): void {
    if (!this.newHelper.preferencedStations) {
      this.newHelper.preferencedStations = [];
    }
    const index = this.newHelper.preferencedStations.indexOf(stationId);
    if (index === -1) {
      this.newHelper.preferencedStations.push(stationId);
    } else {
      this.newHelper.preferencedStations.splice(index, 1);
    }
  }

  isStationPreferenced(stationId: string): boolean {
    return this.newHelper.preferencedStations?.includes(stationId) || false;
  }

  onSubmit(): void {
    if (!this.event?.id) return;

    this.loading = true;
    this.helperService.createPublicHelper(this.event.id, this.newHelper).subscribe({
      next: () => {
        // Redirect to success page or show success message
        this.router.navigate(['/registration-success']);
      },
      error: (err) => {
        this.error = 'Fehler beim Registrieren. Bitte versuchen Sie es später erneut.';
        this.loading = false;
      }
    });
  }

  onDaySelect(day: number): void {
    const index = this.selectedDays.indexOf(day);
    if (index === -1) {
      this.selectedDays.push(day);
    } else {
      this.selectedDays.splice(index, 1);
    }
  }

  isDaySelected(day: number): boolean {
    return this.selectedDays.includes(day);
  }
} 