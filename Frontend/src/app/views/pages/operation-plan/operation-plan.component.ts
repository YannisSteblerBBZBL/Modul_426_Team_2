import { Component, OnInit } from '@angular/core';
import {
  CdkDragDrop,
  DragDropModule,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import { forkJoin } from 'rxjs';
import { Assignment } from '../../../models/assignment.interface';
import { Event, EventDayDisplay } from '../../../models/event.interface';
import { Helper } from '../../../models/helper.interface';
import { Station } from '../../../models/station.interface';
import { AssignmentService } from '../../../services/assignment.service';
import { EventService } from '../../../services/event.service';
import { HelperService } from '../../../services/helper.service';
import { StationService } from '../../../services/station.service';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-operation-plan',
  templateUrl: './operation-plan.component.html',
  styleUrls: ['./operation-plan.component.scss'],
  standalone: true,
  imports: [CommonModule, DragDropModule, FormsModule],
})
export class OperationPlanComponent implements OnInit {
  events: Event[] = [];
  selectedEvent?: Event;
  selectedDay: EventDayDisplay | undefined;
  displayDays: EventDayDisplay[] = [];
  assignments: Assignment[] = [];
  helpers: Helper[] = [];
  stations: Station[] = [];
  loading = false;

  constructor(
    private eventService: EventService,
    private helperService: HelperService,
    private stationService: StationService,
    private assignmentService: AssignmentService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  private getEventDaysForDisplay(
    eventDays: { [key: string]: number }[]
  ): EventDayDisplay[] {
    return eventDays.map((dayObj) => {
      const date = Object.keys(dayObj)[0];
      return {
        date,
        dayNumber: dayObj[date],
      };
    });
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading events:', error);
        this.loading = false;
      }
    });
  }

  updateSelectedEvent(eventId: string): void {
    const event = this.events.find((e) => e.id === eventId);
    if (event) {
      this.selectedEvent = event;
      if (event.eventDays) {
        this.displayDays = this.getEventDaysForDisplay(event.eventDays);
        if (this.displayDays.length > 0) {
          this.updateSelectedDay(this.displayDays[0]);
        }
      } else {
        this.selectedDay = undefined;
        this.assignments = [];
      }
    }
  }

  updateSelectedDay(day: EventDayDisplay): void {
    this.selectedDay = day;
    this.loadAssignments();
  }

  loadAssignments(): void {
    if (!this.selectedEvent || !this.selectedDay) {
      this.assignments = [];
      return;
    }

    this.loading = true;
    this.assignmentService
      .getAssignmentsByEvent(this.selectedEvent.id!)
      .subscribe({
        next: (assignments: Assignment[]) => {
          if (!this.selectedEvent?.eventDays || !this.selectedDay) {
            this.assignments = [];
            this.loading = false;
            return;
          }

          const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
          if (!eventDay) {
            this.assignments = [];
            this.loading = false;
            return;
          }

          const dayNumber = String(eventDay[this.selectedDay.date]);
          
          this.assignments = assignments.filter(
            (a) => a.eventDay === dayNumber
          );
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading assignments:', error);
          this.assignments = [];
          this.loading = false;
        },
      });
  }

  onEventChange(event: any): void {
    const select = event.target as HTMLSelectElement;
    const selectedId = select.value;
    this.updateSelectedEvent(selectedId);
  }

  onDayChange(event: any): void {
    const select = event.target as HTMLSelectElement;
    const selectedIndex = select.selectedIndex;
    if (selectedIndex >= 0 && selectedIndex < this.displayDays.length) {
      this.updateSelectedDay(this.displayDays[selectedIndex]);
    }
  }

  generateAutomaticAssignments(): void {
    if (!this.selectedEvent) return;

    this.loading = true;
    this.assignmentService
      .generateAutomaticAssignments(this.selectedEvent.id!)
      .subscribe({
        next: (assignments: Assignment[]) => {
          this.loadAssignments(); // Reload all assignments instead of filtering
        },
        error: (error) => {
          console.error('Error generating assignments:', error);
          this.loading = false;
        },
      });
  }

  getHelpersForStation(stationId: string): Helper[] {
    if (!this.selectedDay || !this.selectedEvent?.eventDays) return [];

    const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay || !this.selectedDay) return [];

    const dayNumber = String(eventDay[this.selectedDay.date]);

    return this.assignments
      .filter(
        (a) =>
          a.stationId === stationId && a.eventDay === dayNumber
      )
      .map((a) => this.helpers.find((h) => h.id === a.helperId)!)
      .filter(Boolean);
  }

  getUnassignedHelpers(): Helper[] {
    if (!this.selectedDay || !this.selectedEvent?.eventDays) return [];

    const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay || !this.selectedDay) return [];

    const dayNumber = String(eventDay[this.selectedDay.date]);

    const assignedIds = this.assignments
      .filter((a) => a.eventDay === dayNumber)
      .map((a) => a.helperId);

    return this.helpers.filter((h) => !assignedIds.includes(h.id!));
  }

  drop(event: CdkDragDrop<Helper[]>, stationId?: string): void {
    if (!this.selectedEvent || !this.selectedDay || !this.selectedEvent.eventDays) return;

    const helper = event.item.data as Helper;
    if (!helper) return;

    const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay || !this.selectedDay) return;

    const dayNumber = String(eventDay[this.selectedDay.date]);

    // Check for age restrictions if moving to a station
    if (stationId) {
      const isUnderage = parseInt(helper.age) < 18;
      const station = this.stations.find((s) => s.id === stationId);

      if (station?.is18Plus && isUnderage) {
        alert(`${helper.firstname} darf nicht auf eine 18+ Station zugewiesen werden.`);
        return;
      }
    }

    // Find existing assignment for this helper on this day
    const existingAssignment = this.assignments.find(
      (a) => a.helperId === helper.id && a.eventDay === dayNumber
    );

    this.loading = true;

    // If there's an existing assignment, delete it first
    if (existingAssignment) {
      this.assignmentService.deleteAssignment(existingAssignment.id!).subscribe({
        next: () => {
          // Remove the assignment from local array
          this.assignments = this.assignments.filter(a => a.id !== existingAssignment.id);
          
          // If dropping to a station (not to unassigned), create new assignment
          if (stationId) {
            this.createNewAssignment(helper.id!, stationId);
          } else {
            this.loading = false;
          }
        },
        error: (error) => {
          console.error('Error deleting assignment:', error);
          this.loading = false;
          this.loadAssignments(); // Reload to ensure UI sync
        }
      });
    } 
    // If no existing assignment and dropping to a station, create new assignment
    else if (stationId) {
      this.createNewAssignment(helper.id!, stationId);
    }
  }

  private createNewAssignment(helperId: string, stationId: string): void {
    if (!this.selectedEvent || !this.selectedDay || !this.selectedEvent.eventDays) {
      console.error('No event or day selected');
      return;
    }

    const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay || !this.selectedDay) {
      console.error('Selected date not found in event days');
      return;
    }

    const dayNumber = eventDay[this.selectedDay.date];

    const newAssignment: Assignment = {
      eventId: this.selectedEvent.id!,
      eventDay: String(dayNumber), // Sende die Tagnummer anstelle des Datums
      helperId: helperId,
      stationId: stationId,
    };

    this.loading = true;
    this.assignmentService.createAssignment(newAssignment).subscribe({
      next: (createdAssignment) => {
        this.assignments.push(createdAssignment);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating assignment:', error);
        this.loading = false;
        this.loadAssignments(); // Reload assignments to ensure UI is in sync
      }
    });
  }

  stationStatus(stationId: string): 'success' | 'warning' | 'danger' {
    const station = this.stations.find((s) => s.id === stationId);
    const assigned = this.getHelpersForStation(stationId).length;
    if (!station) return 'danger';
    if (assigned === station.neededHelpers) return 'success';
    if (assigned < station.neededHelpers)
      return assigned === 0 ? 'danger' : 'warning';
    return 'success';
  }

  hasAssignments(): boolean {
    return this.assignments.some((a) => a.eventDay === this.selectedDay?.date);
  }

  shouldShowDaySelector(): boolean {
    return !!this.selectedEvent && this.displayDays.length > 1;
  }

  toggleHelperRegistration(): void {
    if (!this.selectedEvent?.id) return;

    const newStatus = !this.selectedEvent.helperRegistrationOpen;
    this.loading = true;
    
    console.log('Toggling registration status:');
    console.log('- Current status:', this.selectedEvent.helperRegistrationOpen);
    console.log('- Setting new status to:', newStatus);

    this.eventService.setHelperRegistrationStatus(this.selectedEvent.id, newStatus).subscribe({
      next: (updatedEvent: Event) => {
        console.log('- Received updated event:', updatedEvent);
        console.log('- Updated registration status:', updatedEvent.helperRegistrationOpen);

        if (this.selectedEvent) {
          // Aktualisiere den Status im lokalen Event-Objekt
          this.selectedEvent = {
            ...this.selectedEvent,
            helperRegistrationOpen: updatedEvent.helperRegistrationOpen
          };
          
          console.log('- Final event status:', this.selectedEvent.helperRegistrationOpen);
          
          // Zeige eine Erfolgsmeldung an basierend auf dem tatsächlichen neuen Status
          const message = this.selectedEvent.helperRegistrationOpen 
            ? 'Die Helfer-Registrierung wurde erfolgreich geöffnet. Sie können den Link jetzt kopieren und teilen.' 
            : 'Die Helfer-Registrierung wurde geschlossen.';
          alert(message);
        }
        this.loading = false;
      },
      error: (err: Error) => {
        console.error('Error updating registration status:', err);
        alert('Fehler beim Aktualisieren des Registrierungsstatus.');
        this.loading = false;
      }
    });
  }

  getRegistrationUrl(): string {
    if (!this.selectedEvent?.id) return '';
    const baseUrl = window.location.origin;
    return `${baseUrl}/register-helper/${this.selectedEvent.id}`;
  }

  copyRegistrationUrl(): void {
    const url = this.getRegistrationUrl();
    navigator.clipboard.writeText(url).then(
      () => {
        alert('Der Registrierungs-Link wurde in die Zwischenablage kopiert!');
      },
      (err) => {
        console.error('Could not copy URL:', err);
        alert('Fehler beim Kopieren des Links. Bitte versuchen Sie es erneut.');
      }
    );
  }
}
