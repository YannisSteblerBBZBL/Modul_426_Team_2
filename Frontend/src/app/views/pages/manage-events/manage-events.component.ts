import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppEvent } from '../../../models/appEvent.interface';
import { EventService } from '../../../services/event.service';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SweetAlert2Module } from '@sweetalert2/ngx-sweetalert2';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-manage-events',
    standalone: true,
    imports: [CommonModule, FormsModule, NgbModule, SweetAlert2Module],
    templateUrl: './manage-events.component.html',
    styleUrls: ['./manage-events.component.scss']
})
export class ManageEventsComponent implements OnInit {
    events: AppEvent[] = [];
    editingEvent: { [key: string]: boolean } = {};
    tempEventData: { [key: string]: Partial<AppEvent> } = {};

    constructor(private eventService: EventService) {}

    ngOnInit(): void {
        this.loadEvents();
    }

    loadEvents(): void {
        this.eventService.getAllEvents().subscribe({
            next: (events) => {
                this.events = events;
            },
            error: (error) => {
                console.error('Error loading events:', error);
                this.showErrorAlert('Error loading events');
            }
        });
    }

    getEventId(event: AppEvent): string {
        return event.id ?? '';
    }

    startEditing(event: AppEvent): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        this.editingEvent[eventId] = true;
        this.tempEventData[eventId] = {
            name: event.name,
            description: event.description,
            startDate: event.startDate,
            endDate: event.endDate,
            eventDays: event.eventDays,
            helperRegistrationOpen: event.helperRegistrationOpen,
            status: event.status
        };
    }

    cancelEditing(event: AppEvent): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        this.editingEvent[eventId] = false;
        delete this.tempEventData[eventId];
    }

    saveEvent(event: AppEvent): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        const updatedEvent: AppEvent = {
            id: eventId,
            name: this.tempEventData[eventId].name || event.name,
            description: this.tempEventData[eventId].description || event.description,
            startDate: this.tempEventData[eventId].startDate || event.startDate,
            endDate: this.tempEventData[eventId].endDate || event.endDate,
            eventDays: this.tempEventData[eventId].eventDays || event.eventDays,
            helperRegistrationOpen: this.tempEventData[eventId].helperRegistrationOpen ?? event.helperRegistrationOpen,
            status: this.tempEventData[eventId].status || event.status
        };

        this.eventService.updateEvent(eventId, updatedEvent).subscribe({
            next: (response) => {
                const index = this.events.findIndex(e => e.id === eventId);
                if (index !== -1) {
                    this.events[index] = response;
                }
                this.editingEvent[eventId] = false;
                delete this.tempEventData[eventId];
                this.showSuccessAlert('Event updated successfully');
            },
            error: (error) => {
                console.error('Error updating event:', error);
                this.showErrorAlert('Error updating event');
            }
        });
    }

    deleteEvent(event: AppEvent): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        Swal.fire({
            title: 'Are you sure?',
            text: 'This action cannot be undone!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete it!',
            cancelButtonText: 'Cancel'
        }).then((result) => {
            if (result.isConfirmed) {
                this.eventService.deleteEvent(eventId).subscribe({
                    next: () => {
                        this.events = this.events.filter(e => e.id !== eventId);
                        this.showSuccessAlert('Event deleted successfully');
                    },
                    error: (error) => {
                        console.error('Error deleting event:', error);
                        this.showErrorAlert('Error deleting event');
                    }
                });
            }
        });
    }

    private showSuccessAlert(message: string): void {
        Swal.fire({
            title: 'Success',
            text: message,
            icon: 'success',
            timer: 2000,
            showConfirmButton: false
        });
    }

    private showErrorAlert(message: string): void {
        Swal.fire({
            title: 'Error',
            text: message,
            icon: 'error'
        });
    }
}
