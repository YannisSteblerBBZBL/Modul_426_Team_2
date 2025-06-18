import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Station } from '../../../models/station.interface';
import { StationService } from '../../../services/station.service';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SweetAlert2Module } from '@sweetalert2/ngx-sweetalert2';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-manage-stations',
  standalone: true,
  imports: [CommonModule, FormsModule, NgbModule, SweetAlert2Module],
  templateUrl: './manage-stations.component.html',
  styleUrl: './manage-stations.component.scss'
})
export class ManageStationsComponent implements OnInit {
  stations: Station[] = [];
  loading = false;
  editingStation: Station | null = null;
  newStation: Station = this.getEmptyStation();
  showNewStationForm = false;

  constructor(private stationService: StationService) {}

  ngOnInit(): void {
    this.loadStations();
  }

  private getEmptyStation(): Station {
    return {
      name: '',
      neededHelpers: 1,
      is18Plus: false
    };
  }

  loadStations(): void {
    this.loading = true;
    this.stationService.getAllStations().subscribe({
      next: (stations) => {
        this.stations = stations;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading stations:', error);
        this.showErrorAlert('Fehler beim Laden der Stationen');
        this.loading = false;
      }
    });
  }

  startEditing(station: Station): void {
    this.editingStation = { ...station };
  }

  cancelEditing(): void {
    this.editingStation = null;
  }

  saveStation(station: Station): void {
    if (!station.id) return;

    this.stationService.updateStation(station.id, station).subscribe({
      next: (updatedStation) => {
        const index = this.stations.findIndex(s => s.id === station.id);
        if (index !== -1) {
          this.stations[index] = updatedStation;
        }
        this.editingStation = null;
        this.showSuccessAlert('Station erfolgreich aktualisiert');
      },
      error: (error) => {
        console.error('Error updating station:', error);
        this.showErrorAlert('Fehler beim Aktualisieren der Station');
      }
    });
  }

  deleteStation(station: Station): void {
    if (!station.id) return;

    // First check if there are any assignments for this station
    this.stationService.getAssignmentsByStationId(station.id).subscribe({
      next: (assignments) => {
        if (assignments.length > 0) {
          // Station has assignments - show options dialog
          this.showDeleteOptionsDialog(station, assignments);
        } else {
          // No assignments - proceed with normal deletion
          this.confirmAndDeleteStation(station);
        }
      },
      error: (error) => {
        console.error('Error checking assignments:', error);
        this.showErrorAlert('Fehler beim Prüfen der Zuteilungen');
      }
    });
  }

  private showDeleteOptionsDialog(station: Station, assignments: any[]): void {
    Swal.fire({
      title: 'Station hat aktive Zuteilungen',
      html: `
        <p>Die Station <strong>"${station.name}"</strong> hat noch ${assignments.length} Zuteilung(en).</p>
        <p>Was möchten Sie tun?</p>
      `,
      icon: 'warning',
      showCancelButton: true,
      showDenyButton: true,
      confirmButtonText: 'Force Löschen',
      denyButtonText: 'Abbrechen',
      cancelButtonText: 'Manuell entfernen',
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      denyButtonColor: '#6c757d'
    }).then((result) => {
      if (result.isConfirmed) {
        // Force delete - delete station and all assignments
        this.forceDeleteStation(station, assignments.length);
      } else if (result.isDismissed && result.dismiss === Swal.DismissReason.cancel) {
        // Manual removal - show message to user
        this.showManualRemovalMessage(station, assignments.length);
      }
      // If result.isDenied, user clicked "Abbrechen" - do nothing
    });
  }

  private forceDeleteStation(station: Station, assignmentCount: number): void {
    if (!station.id) return;

    Swal.fire({
      title: 'Sind Sie sicher?',
      text: `Möchten Sie die Station "${station.name}" und alle ${assignmentCount} Zuteilungen wirklich löschen?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Ja, alles löschen!',
      cancelButtonText: 'Abbrechen',
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6'
    }).then((result) => {
      if (result.isConfirmed) {
        this.stationService.forceDeleteStation(station.id!).subscribe({
          next: () => {
            this.stations = this.stations.filter(s => s.id !== station.id);
            this.showSuccessAlert('Station und alle Zuteilungen erfolgreich gelöscht');
          },
          error: (error) => {
            console.error('Error force deleting station:', error);
            this.showErrorAlert('Fehler beim Löschen der Station');
          }
        });
      }
    });
  }

  private showManualRemovalMessage(station: Station, assignmentCount: number): void {
    Swal.fire({
      title: 'Manuelle Entfernung erforderlich',
      html: `
        <p>Bitte entfernen Sie zuerst alle ${assignmentCount} Zuteilungen für die Station <strong>"${station.name}"</strong> manuell.</p>
        <p>Sie können dies in der <strong>Operationsplanung</strong> tun.</p>
      `,
      icon: 'info',
      confirmButtonText: 'Verstanden',
      confirmButtonColor: '#3085d6'
    });
  }

  private confirmAndDeleteStation(station: Station): void {
    Swal.fire({
      title: 'Sind Sie sicher?',
      text: `Möchten Sie die Station "${station.name}" wirklich löschen?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Ja, löschen!',
      cancelButtonText: 'Abbrechen',
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6'
    }).then((result) => {
      if (result.isConfirmed) {
        this.stationService.deleteStation(station.id!).subscribe({
          next: () => {
            this.stations = this.stations.filter(s => s.id !== station.id);
            this.showSuccessAlert('Station erfolgreich gelöscht');
          },
          error: (error) => {
            console.error('Error deleting station:', error);
            this.showErrorAlert('Fehler beim Löschen der Station');
          }
        });
      }
    });
  }

  createStation(): void {
    this.stationService.createStation(this.newStation).subscribe({
      next: (createdStation) => {
        this.stations.push(createdStation);
        this.newStation = this.getEmptyStation();
        this.showNewStationForm = false;
        this.showSuccessAlert('Station erfolgreich erstellt');
      },
      error: (error) => {
        console.error('Error creating station:', error);
        this.showErrorAlert('Fehler beim Erstellen der Station');
      }
    });
  }

  private showSuccessAlert(message: string): void {
    Swal.fire({
      title: 'Erfolg',
      text: message,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });
  }

  private showErrorAlert(message: string): void {
    Swal.fire({
      title: 'Fehler',
      text: message,
      icon: 'error'
    });
  }
}
