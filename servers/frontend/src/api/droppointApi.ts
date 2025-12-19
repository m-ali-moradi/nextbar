import ApiService from './index';

export interface DropPoint {
  id?: number;
  location: string;
  capacity: number;
  current_empties: number;
  status: 'EMPTY' | 'FULL' | 'FULL_AND_NOTIFIED_TO_WAREHOUSE';
}

export interface CreateDropPointDto {
  location: string;
  capacity: number;
  current_empties?: number;
  status?: 'EMPTY';
}

export interface UpdateDropPointDto {
  location?: string;
  capacity?: number;
  current_empties?: number;
  status?: 'EMPTY' | 'FULL' | 'FULL_AND_NOTIFIED_TO_WAREHOUSE';
}

export const droppointApi = {
  // Get all drop points
  getDropPoints: () => ApiService.get<DropPoint[]>('/api/droppoints'),

  // Get single drop point
  getDropPoint: (id: number) => ApiService.get<DropPoint>(`/api/droppoints/${id}`),

  // Create new drop point
  createDropPoint: (data: CreateDropPointDto) =>
    ApiService.post<DropPoint>('/api/droppoints/create', data),

  // Update drop point
  updateDropPoint: (id: number, data: UpdateDropPointDto) =>
    ApiService.put<DropPoint>(`/api/droppoints/${id}`, data),

  // Delete drop point
  deleteDropPoint: (id: number) =>
    ApiService.delete(`/api/droppoints/droppoints/${id}`),

  // Add empties to a drop point
  addEmpties: (id: number) =>
    ApiService.put<DropPoint>(`/api/droppoints/add_empties/${id}`),

  // Notify warehouse that drop point is full
  notifyWarehouse: (id: number) =>
    ApiService.put<DropPoint>(`/api/droppoints/notify_warehouse/${id}`),

  // Verify that empties were transferred to warehouse
  verifyTransfer: (id: number) =>
    ApiService.get<DropPoint>(`/api/droppoints/verify_transferred_empties/${id}`),
};
