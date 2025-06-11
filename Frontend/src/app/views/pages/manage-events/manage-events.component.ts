import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Event } from '../../../models/event.interface';
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
    events: Event[] = [];
    editingEvent: { [key: string]: boolean } = {};
    tempEventData: { [key: string]: Partial<Event> } = {};

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

    getEventId(event: Event): string {
        return event.id ?? '';
    }

    startEditing(event: Event): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        this.editingEvent[eventId] = true;
        this.tempEventData[eventId] = {
            name: event.name,
            description: event.description,
            startDate: event.startDate,
            endDate: event.endDate,
            status: event.status,
            hasActiveData: event.hasActiveData
        };
    }

    cancelEditing(event: Event): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        this.editingEvent[eventId] = false;
        delete this.tempEventData[eventId];
    }

    saveEvent(event: Event): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        const updatedEvent = {
            id: eventId,
            name: this.tempEventData[eventId].name || event.name,
            description: this.tempEventData[eventId].description || event.description,
            startDate: this.tempEventData[eventId].startDate || event.startDate,
            endDate: this.tempEventData[eventId].endDate || event.endDate,
            status: this.tempEventData[eventId].status || event.status,
            hasActiveData: this.tempEventData[eventId].hasActiveData ?? event.hasActiveData
        };

        this.eventService.updateEvent(updatedEvent).subscribe({
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

    deleteEvent(event: Event): void {
        const eventId = this.getEventId(event);
        if (!eventId) return;

        if (event.hasActiveData) {
            this.showErrorAlert('Cannot delete event with active data');
            return;
        }

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
