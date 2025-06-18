import { Routes } from '@angular/router';
import { BaseComponent } from './views/layout/base/base.component';
import { authGuard } from './services/auth/auth.guard';
import { PublicHelperRegistrationComponent } from './views/pages/public-helper-registration/public-helper-registration.component';
import { RegistrationSuccessComponent } from './views/pages/registration-success/registration-success.component';

export const routes: Routes = [
  {
    path: 'impressum',
    loadComponent: () => import('./views/pages/legal/impressum/impressum.component').then(c => c.ImpressumComponent)
  },
  {
    path: 'agb',
    loadComponent: () => import('./views/pages/legal/agb/agb.component').then(c => c.AgbComponent)
  },
  {
    path: 'data-protection-law',
    loadComponent: () => import('./views/pages/legal/data-protection-law/data-protection-law.component').then(c => c.DataProtectionLawComponent)
  },
  { path: 'auth', loadChildren: () => import('./views/pages/auth/auth.routes')},
  {
    path: '',
    component: BaseComponent,
    canActivateChild: [authGuard],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      {
        path: 'home',
        loadComponent: () => import('./views/pages/home/home.component').then(c => c.HomeComponent),
      },
      {
        path: 'operations',
        loadComponent: () => import('./views/pages/operation-plan/operation-plan.component').then(c => c.OperationPlanComponent),
      },
      {
        path: 'manage-helpers',
        loadComponent: () => import('./views/pages/helper-managment/helper-managment.component').then(c => c.HelperManagmentComponent),
      },
      {
        path: 'manage-events',
        loadComponent: () => import('./views/pages/manage-events/manage-events.component').then(c => c.ManageEventsComponent),
        title: 'Manage Events'
      },
      {
        path: 'manage-stations',
        loadComponent: () => import('./views/pages/manage-stations/manage-stations.component').then(c => c.ManageStationsComponent),
        title: 'Manage Stations'
      },
    ]
  },
  {
    path: 'error',
    loadComponent: () => import('./views/pages/error/error.component').then(c => c.ErrorComponent),
  },
  {
    path: 'error/:type',
    loadComponent: () => import('./views/pages/error/error.component').then(c => c.ErrorComponent)
  },
  { 
    path: 'register-helper/:eventId', 
    component: PublicHelperRegistrationComponent 
  },
  {
    path: 'registration-success',
    component: RegistrationSuccessComponent
  }
];
