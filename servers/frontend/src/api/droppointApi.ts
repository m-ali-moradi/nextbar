import ApiService from './index';
import type { DropPoint } from './types';

export type { DropPoint };

const DROPPOINT_BASE_PATH = '/api/v1/droppoints';

/**
 * Drop point API module.
 *
 * Exposes operational actions used by the drop-point workflow:
 * empties intake, warehouse notification, and transfer verification.
 */

export const droppointApi = {
  /** Get all drop points. */
  getDropPoints: () => ApiService.get<DropPoint[]>(DROPPOINT_BASE_PATH),

  /** Get one drop point by id. */
  getDropPoint: (id: number) => ApiService.get<DropPoint>(`${DROPPOINT_BASE_PATH}/${id}`),

  /** Add empties to a drop point. */
  addEmpties: (id: number) =>
    ApiService.put<DropPoint>(`${DROPPOINT_BASE_PATH}/add_empties/${id}`),

  /** Notify warehouse that a drop point is ready for pickup. */
  notifyWarehouse: (id: number) =>
    ApiService.put<DropPoint>(`${DROPPOINT_BASE_PATH}/notify_warehouse/${id}`),

  /** Verify that empties were transferred from drop point to warehouse. */
  verifyTransfer: (id: number) =>
    ApiService.get<DropPoint>(`${DROPPOINT_BASE_PATH}/verify_transferred_empties/${id}`),
};
