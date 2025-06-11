import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BaseComponent } from './views/layout/base/base.component';

const routes: Routes = [
  {
    path: '',
    component: BaseComponent,
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      },
      {
        path: 'home',
        loadComponent: () => import('./views/pages/home/home.component').then(c => c.HomeComponent)
      },
      {
        path: 'manage-events',
        loadComponent: () => import('./views/pages/manage-events/manage-events.component').then(c => c.ManageEventsComponent)
      },
      {
        path: 'manage-helpers',
        loadComponent: () => import('./views/pages/helper-managment/helper-managment.component').then(c => c.HelperManagmentComponent)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { } 