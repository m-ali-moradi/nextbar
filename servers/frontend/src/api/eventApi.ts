import ApiService from './index';

// ============ Type Definitions ============

export type EventStatus = 'SCHEDULED' | 'RUNNING' | 'COMPLETED' | 'CANCELLED';

export interface EventBar {
  id: number;
  name: string;
  location?: string;
  assignedStaff?: string;
  active: boolean;
  eventId: number;
  eventName?: string;
  stockCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface BarStock {
  id: number;
  barId: number;
  barName?: string;
  productId: number;
  productName: string;
  allocatedQuantity: number;
  usedQuantity: number;
  remainingQuantity: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface EventDropPoint {
  id: number;
  name: string;
  location?: string;
  eventId: number;
  eventName?: string;
  eventOccupancy?: number;
  assignedStaff?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EventSummary {
  id: number;
  name: string;
  date: string;
  location?: string;
  status: EventStatus;
  organizerName?: string;
  barCount: number;
  dropPointCount: number;
  isPublic: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface EventDetail extends EventSummary {
  description?: string;
  organizerEmail?: string;
  organizerPhone?: string;
  expectedAttendees?: number;
  maxCapacity?: number;
  bars: EventBar[];
  dropPoints: EventDropPoint[];
}

// ============ Request DTOs ============

export interface CreateEventRequest {
  name: string;
  date: string;
  location?: string;
  description?: string;
  organizerName?: string;
  organizerEmail?: string;
  organizerPhone?: string;
  expectedAttendees?: number;
  maxCapacity?: number;
  isPublic?: boolean;
}

export interface UpdateEventRequest extends Partial<CreateEventRequest> {
  status?: EventStatus;
}

export interface CreateBarRequest {
  name: string;
  location?: string;
  eventId: number;
  assignedStaff?: string;
}

export interface UpdateBarRequest {
  name?: string;
  location?: string;
  assignedStaff?: string;
  active?: boolean;
}

export interface CreateDropPointRequest {
  name: string;
  location?: string;
  eventId: number;
  assignedStaff?: string;
}

export interface UpdateDropPointRequest {
  name?: string;
  location?: string;
  assignedStaff?: string;
}

export interface CreateBarStockRequest {
  productId: number;
  productName: string;
  allocatedQuantity: number;
}

export interface UserDto {
  id: string;
  username: string;
  email?: string;
  fullName?: string;
}

export interface WarehouseStockDto {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unit?: string;
}

// ============ API Functions ============

export const eventApi = {
  // ===== Events =====

  /** Get all events (summary list) */
  getEvents: () => ApiService.get<EventSummary[]>('/api/events'),

  /** Get single event with full details */
  getEvent: (id: number) => ApiService.get<EventDetail>(`/api/events/${id}`),

  /** Create new event */
  createEvent: (data: CreateEventRequest) =>
    ApiService.post<EventSummary>('/api/events', data),

  /** Update event */
  updateEvent: (id: number, data: UpdateEventRequest) =>
    ApiService.put<EventDetail>(`/api/events/${id}`, data),

  /** Delete event */
  deleteEvent: (id: number) => ApiService.delete(`/api/events/${id}`),

  /** Start event (changes status to RUNNING) */
  startEvent: (id: number) =>
    ApiService.post<EventDetail>(`/api/events/${id}/start`),

  /** Complete event (changes status to COMPLETED) */
  completeEvent: (id: number) =>
    ApiService.post<EventDetail>(`/api/events/${id}/complete`),

  /** Cancel event (changes status to CANCELLED) */
  cancelEvent: (id: number) =>
    ApiService.post<EventDetail>(`/api/events/${id}/cancel`),

  // ===== Bars =====

  /** Get all bars for an event */
  getEventBars: (eventId: number) =>
    ApiService.get<EventBar[]>(`/api/events/bars/event/${eventId}`),

  /** Get all bars */
  getAllBars: () => ApiService.get<EventBar[]>('/api/events/bars'),

  /** Get single bar */
  getBar: (id: number) => ApiService.get<EventBar>(`/api/events/bars/${id}`),

  /** Create bar */
  createBar: (data: CreateBarRequest) =>
    ApiService.post<EventBar>('/api/events/bars', data),

  /** Update bar */
  updateBar: (id: number, data: UpdateBarRequest) =>
    ApiService.put<EventBar>(`/api/events/bars/${id}`, data),

  /** Delete bar */
  deleteBar: (id: number) => ApiService.delete(`/api/events/bars/${id}`),

  /** Assign staff to bar */
  assignBarStaff: (barId: number, staffId: string) =>
    ApiService.put<EventBar>(`/api/events/bars/${barId}/staff?staffIds=${staffId}`),

  /** Get available staff for bar assignment */
  getAvailableBarStaff: () =>
    ApiService.get<UserDto[]>('/api/events/bars/staff/available'),

  // ===== Bar Stocks =====

  /** Get stocks for a bar */
  getBarStocks: (barId: number) =>
    ApiService.get<BarStock[]>(`/api/events/bars/${barId}/stocks`),

  /** Add stock to bar */
  addBarStock: (barId: number, data: CreateBarStockRequest) =>
    ApiService.post<BarStock>(`/api/events/bars/${barId}/stocks`, data),

  /** Update bar stock */
  updateBarStock: (barId: number, stockId: number, data: Partial<CreateBarStockRequest>) =>
    ApiService.put<BarStock>(`/api/events/bars/${barId}/stocks/${stockId}`, data),

  /** Delete bar stock */
  deleteBarStock: (barId: number, stockId: number) =>
    ApiService.delete(`/api/events/bars/${barId}/stocks/${stockId}`),

  /** Batch update bar stocks */
  batchUpdateBarStocks: (barId: number, stocks: CreateBarStockRequest[]) =>
    ApiService.put<BarStock[]>(`/api/events/bars/${barId}/stocks/batch`, stocks),

  /** Get available warehouse inventory */
  getWarehouseInventory: () =>
    ApiService.get<WarehouseStockDto[]>('/api/events/warehouse/inventory'),

  // ===== Drop Points =====

  /** Get all drop points for an event */
  getEventDropPoints: (eventId: number) =>
    ApiService.get<EventDropPoint[]>(`/api/events/drop-points/event/${eventId}`),

  /** Get all drop points */
  getAllDropPoints: () =>
    ApiService.get<EventDropPoint[]>('/api/events/drop-points'),

  /** Get single drop point */
  getDropPoint: (id: number) =>
    ApiService.get<EventDropPoint>(`/api/events/drop-points/${id}`),

  /** Create drop point */
  createDropPoint: (data: CreateDropPointRequest) =>
    ApiService.post<EventDropPoint>('/api/events/drop-points', data),

  /** Update drop point */
  updateDropPoint: (id: number, data: UpdateDropPointRequest) =>
    ApiService.put<EventDropPoint>(`/api/events/drop-points/${id}`, data),

  /** Delete drop point */
  deleteDropPoint: (id: number) =>
    ApiService.delete(`/api/events/drop-points/${id}`),

  /** Assign staff to drop point */
  assignDropPointStaff: (dropPointId: number, staffId: string) =>
    ApiService.put<EventDropPoint>(`/api/events/drop-points/${dropPointId}/staff?staffIds=${staffId}`),
};
