export interface Event {
    id?: string;
    name: string;
    description: string;
    startDate: string;
    endDate: string;
    status: string;
    hasActiveData: boolean;
    eventDays?: { [key: string]: number }[];
    helperRegistrationOpen?: boolean;
    userId?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface EventDay {
    date: string;
    dayNumber: number;
}

export interface EventDayDisplay {
    date: string;
    dayNumber: number;
    assignments?: any[];
} 