import ApiService from './index';

export interface Beverage {
  id: number;
  name: string;
  price: number;
}

export interface EventBar {
  barName: string;
  location: string;
  totalCapacity: number;
  beverageStock: { [key: number]: number };
}

export interface EventDropPoint {
  location: string;
  capacity: number;
}

export interface Event {
  eventId?: number;
  name: string;
  date: string;
  location: string;
  duration: number;
  status: 'PLANNED' | 'ONGOING' | 'COMPLETED';
  beverages: Beverage[];
  bars: EventBar[];
  dropPoints: EventDropPoint[];
}

export interface CreateEventDto {
  name: string;
  date: string;
  location: string;
  duration: number;
  status: 'PLANNED' | 'ONGOING' | 'COMPLETED';
  beverages: Beverage[];
  bars: EventBar[];
  dropPoints: EventDropPoint[];
}

export interface UpdateEventDto extends CreateEventDto {
  eventId?: number;
}

export const eventApi = {
  // Get all events
  getEvents: () => ApiService.get<Event[]>('/api/events'),

  // Get single event
  getEvent: (id: number) => ApiService.get<Event>(`/api/events/${id}`),

  // Get available beverages
  getBeverages: () => ApiService.get<Beverage[]>('/api/events/beverages'),

  // Create new event
  createEvent: (data: CreateEventDto) => ApiService.post<Event>('/api/events', data),

  // Update event
  updateEvent: (id: number, data: UpdateEventDto) =>
    ApiService.put<Event>(`/api/events/${id}`, data),

  // Delete event
  deleteEvent: (id: number) => ApiService.delete(`/api/events/${id}`),
};
