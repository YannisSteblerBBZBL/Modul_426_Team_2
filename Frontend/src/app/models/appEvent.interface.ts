export interface AppEvent {
    id?: string;
    name: string;
    description?: string;
    startDate: string;
    endDate: string;
    eventDays: { [key: string]: number }[];
    helperRegistrationOpen: boolean;
    status?: 'active' | 'cancelled' | 'completed';
    hasActiveData?: boolean;
}

// Interface for the transformed event day structure used in the frontend
export interface EventDayDisplay {
    date: string;
    dayNumber: number;
} 