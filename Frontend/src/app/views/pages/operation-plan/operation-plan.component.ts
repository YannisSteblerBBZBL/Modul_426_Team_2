import { Component, OnInit } from '@angular/core';
import {
  CdkDragDrop,
  DragDropModule,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import { forkJoin } from 'rxjs';
import { Assignment } from '../../../models/assignment.interface';
import { AppEvent, EventDayDisplay } from '../../../models/appEvent.interface';
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
  events: AppEvent[] = [];
  selectedEvent?: AppEvent;
  selectedDay?: EventDayDisplay;
  displayDays: EventDayDisplay[] = [];

  helpers: Helper[] = [];
  stations: Station[] = [];
  assignments: Assignment[] = [];

  loading = false;

  constructor(
    private eventService: EventService,
    private assignmentService: AssignmentService,
    private helperService: HelperService,
    private stationService: StationService
  ) {}

  ngOnInit(): void {
    this.loadData();
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

  loadData(): void {
    this.loading = true;
    forkJoin({
      events: this.eventService.getAllEvents(),
      helpers: this.helperService.getAllHelpers(),
      stations: this.stationService.getAllStations(),
    }).subscribe({
      next: ({ events, helpers, stations }) => {
        this.events = events;
        this.helpers = helpers;
        this.stations = stations;

        if (events.length > 0) {
          // Set initial event and its days
          this.updateSelectedEvent(events[0].id!);
        }

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading data:', error);
        this.loading = false;
      },
    });
  }

  updateSelectedEvent(eventId: string): void {
    const event = this.events.find((e) => e.id === eventId);
    if (event) {
      this.selectedEvent = event;
      this.displayDays = this.getEventDaysForDisplay(event.eventDays);

      if (this.displayDays.length > 0) {
        this.updateSelectedDay(this.displayDays[0]);
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
          // Finde die Tagnummer für das ausgewählte Datum
          const eventDay = this.selectedEvent!.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
          if (!eventDay) {
            this.assignments = [];
            this.loading = false;
            return;
          }

          const dayNumber = String(eventDay[this.selectedDay!.date]);
          
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

  onEventChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const selectedId = select.value;
    this.updateSelectedEvent(selectedId);
  }

  onDayChange(event: Event): void {
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
          this.assignments = assignments.filter(
            (a) => a.eventDay === this.selectedDay?.date
          );
          this.loading = false;
        },
        error: (error) => {
          console.error('Error generating assignments:', error);
          this.loading = false;
        },
      });
  }

  getHelpersForStation(stationId: string): Helper[] {
    if (!this.selectedDay) return [];

    // Finde die Tagnummer für das ausgewählte Datum
    const eventDay = this.selectedEvent?.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay) return [];

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
    if (!this.selectedDay) return [];

    // Finde die Tagnummer für das ausgewählte Datum
    const eventDay = this.selectedEvent?.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay) return [];

    const dayNumber = String(eventDay[this.selectedDay.date]);

    const assignedIds = this.assignments
      .filter((a) => a.eventDay === dayNumber)
      .map((a) => a.helperId);

    return this.helpers.filter((h) => !assignedIds.includes(h.id!));
  }

  drop(event: CdkDragDrop<Helper[]>, stationId?: string): void {
    if (!this.selectedEvent || !this.selectedDay) return;

    const helper = event.item.data as Helper;
    if (!helper) return;

    // Find the event day number for the selected date
    const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay) return;

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
    // Logging für Debugging
    console.log('Event days:', this.selectedEvent?.eventDays);
    console.log('Selected day:', this.selectedDay);

    if (!this.selectedEvent || !this.selectedDay) {
      console.error('No event or day selected');
      return;
    }

    // Finde die Tagnummer für das ausgewählte Datum
    const eventDay = this.selectedEvent.eventDays.find(day => Object.keys(day)[0] === this.selectedDay?.date);
    if (!eventDay) {
      console.error('Selected date not found in event days');
      return;
    }

    const dayNumber = eventDay[this.selectedDay.date];
    console.log('Day number:', dayNumber);

    const newAssignment: Assignment = {
      eventId: this.selectedEvent.id!,
      eventDay: String(dayNumber), // Sende die Tagnummer anstelle des Datums
      helperId: helperId,
      stationId: stationId,
    };

    console.log('Creating new assignment:', newAssignment);

    this.loading = true;
    this.assignmentService.createAssignment(newAssignment).subscribe({
      next: (createdAssignment) => {
        console.log('Assignment created successfully:', createdAssignment);
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
}
