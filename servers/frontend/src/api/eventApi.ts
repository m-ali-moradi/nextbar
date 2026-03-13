import ApiService from './index';
import type {
  EventStatus,
  ResourceMode,
  EventBar,
  BarStock,
  EventDropPoint,
  EventSummary,
  EventDetail,
  CreateEventRequest,
  UpdateEventRequest,
  CreateBarRequest,
  UpdateBarRequest,
  CreateDropPointRequest,
  UpdateDropPointRequest,
  CreateBarStockRequest,
  UserDto,
  WarehouseStockDto,
} from './types';

export type {
  EventStatus,
  ResourceMode,
  EventBar,
  BarStock,
  EventDropPoint,
  EventSummary,
  EventDetail,
  CreateEventRequest,
  UpdateEventRequest,
  CreateBarRequest,
  UpdateBarRequest,
  CreateDropPointRequest,
  UpdateDropPointRequest,
  CreateBarStockRequest,
  UserDto,
  WarehouseStockDto,
};

const EVENTS_BASE_PATH = '/api/v1/events';
const EVENT_BARS_BASE_PATH = '/api/v1/events/bars';
const EVENT_DROPPOINTS_BASE_PATH = '/api/v1/events/drop-points';

/**
 * Event planner API module.
 *
 * Groups endpoints by feature area:
 * - events lifecycle
 * - event bars + stock allocation
 * - event drop points
 */

export const eventApi = {
  // ===== Events =====

  /** Get all events (summary list) */
  getEvents: () => ApiService.get<EventSummary[]>(EVENTS_BASE_PATH),

  /** Get single event with full details */
  getEvent: (id: number) => ApiService.get<EventDetail>(`${EVENTS_BASE_PATH}/${id}/details`),

  /** Create new event */
  createEvent: (data: CreateEventRequest) =>
    ApiService.post<EventSummary>(EVENTS_BASE_PATH, data),

  /** Update event */
  updateEvent: (id: number, data: UpdateEventRequest) =>
    ApiService.put<EventDetail>(`${EVENTS_BASE_PATH}/${id}`, data),

  /** Delete event */
  deleteEvent: (id: number) => ApiService.delete(`${EVENTS_BASE_PATH}/${id}`),

  /** Start event (changes status to RUNNING) */
  startEvent: (id: number) =>
    ApiService.post<EventDetail>(`${EVENTS_BASE_PATH}/${id}/start`),

  /** Complete event (changes status to COMPLETED) */
  completeEvent: (id: number) =>
    ApiService.post<EventDetail>(`${EVENTS_BASE_PATH}/${id}/complete`),

  /** Cancel event (changes status to CANCELLED) */
  cancelEvent: (id: number) =>
    ApiService.post<EventDetail>(`${EVENTS_BASE_PATH}/${id}/cancel`),

  // ===== Bars =====

  /** Get all bars for an event */
  getEventBars: (eventId: number) =>
    ApiService.get<EventBar[]>(`${EVENT_BARS_BASE_PATH}/event/${eventId}`),

  /** Get all bars */
  getAllBars: () => ApiService.get<EventBar[]>(EVENT_BARS_BASE_PATH),

  /** Get single bar */
  getBar: (id: number) => ApiService.get<EventBar>(`${EVENT_BARS_BASE_PATH}/${id}`),

  /** Create bar */
  createBar: (data: CreateBarRequest) =>
    ApiService.post<EventBar>(EVENT_BARS_BASE_PATH, data),

  /** Update bar */
  updateBar: (id: number, data: UpdateBarRequest) =>
    ApiService.put<EventBar>(`${EVENT_BARS_BASE_PATH}/${id}`, data),

  /** Delete bar */
  deleteBar: (id: number) => ApiService.delete(`${EVENT_BARS_BASE_PATH}/${id}`),

  /** Assign staff to bar */
  assignBarStaff: (barId: number, staffId: string) =>
    ApiService.put<EventBar>(`${EVENT_BARS_BASE_PATH}/${barId}/staff?staffIds=${staffId}`),

  /** Get available staff for bar assignment */
  getAvailableBarStaff: () =>
    ApiService.get<UserDto[]>(`${EVENT_BARS_BASE_PATH}/staff/available`),

  // ===== Bar Stocks =====

  /** Get stocks for a bar */
  getBarStocks: (barId: number) =>
    ApiService.get<BarStock[]>(`${EVENT_BARS_BASE_PATH}/${barId}/stocks`),

  /** Add stock to bar */
  addBarStock: (barId: number, data: CreateBarStockRequest) =>
    ApiService.post<BarStock>(`${EVENT_BARS_BASE_PATH}/${barId}/stocks`, data),

  /** Update bar stock */
  updateBarStock: (barId: number, stockId: number, data: Partial<CreateBarStockRequest>) =>
    ApiService.put<BarStock>(`${EVENT_BARS_BASE_PATH}/${barId}/stocks/${stockId}`, data),

  /** Delete bar stock */
  deleteBarStock: (barId: number, stockId: number) =>
    ApiService.delete(`${EVENT_BARS_BASE_PATH}/${barId}/stocks/${stockId}`),

  /** Batch update bar stocks */
  batchUpdateBarStocks: (barId: number, stocks: CreateBarStockRequest[]) =>
    ApiService.put<BarStock[]>(`${EVENT_BARS_BASE_PATH}/${barId}/stocks/batch`, stocks),

  /** Get available warehouse inventory */
  getWarehouseInventory: () =>
    ApiService.get<WarehouseStockDto[]>(`${EVENTS_BASE_PATH}/warehouse/inventory`),

  // ===== Drop Points =====

  /** Get all drop points for an event */
  getEventDropPoints: (eventId: number) =>
    ApiService.get<EventDropPoint[]>(`${EVENT_DROPPOINTS_BASE_PATH}/event/${eventId}`),

  /** Get all drop points */
  getAllDropPoints: () =>
    ApiService.get<EventDropPoint[]>(EVENT_DROPPOINTS_BASE_PATH),

  /** Get single drop point */
  getDropPoint: (id: number) =>
    ApiService.get<EventDropPoint>(`${EVENT_DROPPOINTS_BASE_PATH}/${id}`),

  /** Create drop point */
  createDropPoint: (data: CreateDropPointRequest) =>
    ApiService.post<EventDropPoint>(EVENT_DROPPOINTS_BASE_PATH, data),

  /** Update drop point */
  updateDropPoint: (id: number, data: UpdateDropPointRequest) =>
    ApiService.put<EventDropPoint>(`${EVENT_DROPPOINTS_BASE_PATH}/${id}`, data),

  /** Delete drop point */
  deleteDropPoint: (id: number) =>
    ApiService.delete(`${EVENT_DROPPOINTS_BASE_PATH}/${id}`),

  /** Assign staff to drop point */
  assignDropPointStaff: (dropPointId: number, staffId: string) =>
    ApiService.put<EventDropPoint>(`${EVENT_DROPPOINTS_BASE_PATH}/${dropPointId}/staff?staffIds=${staffId}`),
};
