import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-apps',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './apps.component.html',
    styleUrl: './apps.component.scss'
})
export class AppsComponent {


} 