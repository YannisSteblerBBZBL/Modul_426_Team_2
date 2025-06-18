export interface Helper {
    id?: string;
    firstname: string;
    lastname: string;
    email: string;
    birthdate: string;
    age: string;
    eventId?: string;
    presence: number[];
    preferences?: string[];
    preferencedHelpers?: string[];
    preferencedStations?: string[];
} 