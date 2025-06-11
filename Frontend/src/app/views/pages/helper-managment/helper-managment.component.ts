import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Helper } from '../../../models/helper.interface';
import { HelperService } from '../../../services/helper.service';
import { StationService } from '../../../services/station.service';
import { Station } from '../../../models/station.interface';

@Component({
  selector: 'app-helper-managment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './helper-managment.component.html',
  styleUrl: './helper-managment.component.scss'
})
export class HelperManagmentComponent implements OnInit {
  helpers: Helper[] = [];
  stations: Station[] = [];
  loading = false;
  editingHelper: Helper | null = null;
  newHelper: Helper = this.getEmptyHelper();
  showNewHelperForm = false;
  availableDays = [1, 2, 3, 4, 5]; // Tage des Events

  constructor(
    private helperService: HelperService,
    private stationService: StationService
  ) {}

  ngOnInit(): void {
    this.loadData();
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

  loadData(): void {
    this.loading = true;
    Promise.all([
      this.helperService.getAllHelpers().toPromise(),
      this.stationService.getAllStations().toPromise()
    ]).then(([helpers, stations]) => {
      this.helpers = helpers || [];
      this.stations = stations || [];
      this.loading = false;
    }).catch(error => {
      console.error('Error loading data:', error);
      this.loading = false;
    });
  }

  startEditing(helper: Helper): void {
    this.editingHelper = { 
      ...helper,
      presence: [...(helper.presence || [])],
      preferences: [...(helper.preferences || [])],
      preferencedHelpers: [...(helper.preferencedHelpers || [])],
      preferencedStations: [...(helper.preferencedStations || [])]
    };
  }

  cancelEditing(): void {
    this.editingHelper = null;
  }

  saveHelper(helper: Helper): void {
    if (helper.id) {
      this.helperService.updateHelper(helper.id, helper).subscribe({
        next: (updatedHelper) => {
          const index = this.helpers.findIndex(h => h.id === helper.id);
          if (index !== -1) {
            this.helpers[index] = updatedHelper;
          }
          this.editingHelper = null;
        },
        error: (error) => console.error('Error updating helper:', error)
      });
    }
  }

  deleteHelper(helper: Helper): void {
    if (!helper.id) return;
    
    if (confirm(`Sind Sie sicher, dass Sie ${helper.firstname} ${helper.lastname} löschen möchten?`)) {
      this.helperService.deleteHelper(helper.id).subscribe({
        next: () => {
          this.helpers = this.helpers.filter(h => h.id !== helper.id);
        },
        error: (error) => console.error('Error deleting helper:', error)
      });
    }
  }

  createHelper(): void {
    this.helperService.createHelper(this.newHelper).subscribe({
      next: (createdHelper) => {
        this.helpers.push(createdHelper);
        this.newHelper = this.getEmptyHelper();
        this.showNewHelperForm = false;
      },
      error: (error) => console.error('Error creating helper:', error)
    });
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

  onBirthdateChange(helper: Helper): void {
    helper.age = this.calculateAge(helper.birthdate);
  }

  togglePresence(helper: Helper, day: number): void {
    const index = helper.presence.indexOf(day);
    if (index === -1) {
      helper.presence.push(day);
    } else {
      helper.presence.splice(index, 1);
    }
  }

  isPresent(helper: Helper, day: number): boolean {
    return helper.presence.includes(day);
  }

  togglePreferencedStation(helper: Helper, stationId: string): void {
    const index = helper.preferencedStations?.indexOf(stationId) ?? -1;
    if (index === -1) {
      helper.preferencedStations = [...(helper.preferencedStations || []), stationId];
    } else {
      helper.preferencedStations = helper.preferencedStations?.filter(id => id !== stationId) || [];
    }
  }

  togglePreferencedHelper(helper: Helper, helperId: string): void {
    const index = helper.preferencedHelpers?.indexOf(helperId) ?? -1;
    if (index === -1) {
      helper.preferencedHelpers = [...(helper.preferencedHelpers || []), helperId];
    } else {
      helper.preferencedHelpers = helper.preferencedHelpers?.filter(id => id !== helperId) || [];
    }
  }

  isStationPreferenced(helper: Helper, stationId: string): boolean {
    return helper.preferencedStations?.includes(stationId) || false;
  }

  isHelperPreferenced(helper: Helper, helperId: string): boolean {
    return helper.preferencedHelpers?.includes(helperId) || false;
  }

  getHelperName(helperId: string): string {
    const helper = this.helpers.find(h => h.id === helperId);
    return helper ? `${helper.firstname} ${helper.lastname}` : 'Unbekannt';
  }

  getStationName(stationId: string): string {
    const station = this.stations.find(s => s.id === stationId);
    return station ? station.name : 'Unbekannt';
  }
}
