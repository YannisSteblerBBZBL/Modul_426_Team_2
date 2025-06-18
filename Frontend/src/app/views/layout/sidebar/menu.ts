import { MenuItem } from './menu.model';

export const MENU: MenuItem[] = [
  {
    label: 'Main',
    isTitle: true
  },
  {
    label: 'Dashboard',
    icon: 'home',
    link: '/home'
  },
  {
    label: 'Functions',
    icon: 'aperture',
    link: '/apps'
  },

  {
    label: 'Operations',
    isTitle: true
  },
  {
    label: 'Operations Planning',
    icon: 'activity',
    link: '/operations'
  },

  {
    label: 'Manage Your Event',
    isTitle: true
  },
  {
    label: 'Manage Events',
    icon: 'activity',
    link: '/manage-events'
  },
  {
    label: 'Manage Helpers',
    icon: 'book-open',
    link: '/manage-helpers'
  },
  {
    label: 'Manage Stations',
    icon: 'activity',
    link: '/manage-stations'
  },
];
