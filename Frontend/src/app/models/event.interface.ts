export interface Event {
    id?: string;
    name: string;
    description?: string;
    startDate: string;
    endDate: string;
    eventDays: EventDay[];
}

export interface EventDay {
    date: string;
    dayNumber: number;
} 