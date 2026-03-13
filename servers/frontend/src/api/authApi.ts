import { AxiosResponse } from 'axios';
import ApiService from './index';

/**
 * Local payload aliases for endpoints that currently accept flexible structures.
 * These keep the API surface explicit while remaining backward compatible.
 */
type UserMutationPayload = Record<string, unknown>;
type AssignmentPayload = Record<string, unknown>;

/**
 * API path prefixes used by auth + user management endpoints.
 */
const USERS_BASE_PATH = '/api/v1/users';
const USER_MANAGEMENT_BASE_PATH = '/api/v1/users/management';

/**
 * Authentication and user-management API module.
 *
 * Note: Responses remain `AxiosResponse` to avoid changing existing consumers.
 */
export const authApi = {
  // ==============================
  // Authentication
  // ==============================

  /** Authenticate user and retrieve access/refresh tokens. */
  login(username: string, password: string): Promise<AxiosResponse> {
    return ApiService.post(`${USERS_BASE_PATH}/login`, { username, password });
  },

  /** Refresh access token using a refresh token. */
  refresh(): Promise<AxiosResponse> {
    return ApiService.post(`${USERS_BASE_PATH}/refresh`, {}, { withCredentials: true });
  },

  /** Invalidate refresh token and end server-side session state. */
  logout(): Promise<AxiosResponse> {
    return ApiService.post(`${USERS_BASE_PATH}/logout`, {}, { withCredentials: true });
  },

  // ==============================
  // User CRUD and profile
  // ==============================

  /** Get all users. */
  getUsers(): Promise<AxiosResponse> {
    return ApiService.get(USERS_BASE_PATH);
  },

  /** Create a user. */
  createUser(data: UserMutationPayload): Promise<AxiosResponse> {
    return ApiService.post(USERS_BASE_PATH, data);
  },

  /** Update user fields. */
  updateUser(userId: string, data: UserMutationPayload): Promise<AxiosResponse> {
    return ApiService.put(`${USERS_BASE_PATH}/${userId}`, data);
  },

  /** Replace assignment set for a user. */
  setUserAssignments(userId: string, assignments: AssignmentPayload[]): Promise<AxiosResponse> {
    return ApiService.put(`${USERS_BASE_PATH}/${userId}/assignments`, assignments);
  },

  /** Remove one assignment from a user. */
  removeUserAssignment(userId: string, assignmentId: string): Promise<AxiosResponse> {
    return ApiService.delete(`${USERS_BASE_PATH}/${userId}/assignments/${assignmentId}`);
  },

  /** Delete user account. */
  deleteUser(userId: string): Promise<AxiosResponse> {
    return ApiService.delete(`${USERS_BASE_PATH}/${userId}`);
  },

  /** Get currently authenticated user's profile. */
  getProfile(): Promise<AxiosResponse> {
    return ApiService.get(`${USERS_BASE_PATH}/profile`);
  },

  /** Update current user's profile. */
  updateProfile(data: UserMutationPayload): Promise<AxiosResponse> {
    return ApiService.put(`${USERS_BASE_PATH}/profile`, data);
  },

  /** Change current user's password. */
  changeMyPassword(data: UserMutationPayload): Promise<AxiosResponse> {
    return ApiService.post(`${USERS_BASE_PATH}/password/change`, data);
  },

  /** Admin operation: reset user password. */
  adminResetUserPassword(userId: string, newPassword: string): Promise<AxiosResponse> {
    return ApiService.post(`${USERS_BASE_PATH}/${userId}/password/reset`, { newPassword });
  },

  // ==============================
  // Role management
  // ==============================

  /** List available roles. */
  getRoles(): Promise<AxiosResponse> {
    return ApiService.get(`${USER_MANAGEMENT_BASE_PATH}/roles`);
  },

  /** List available services/scope areas. */
  getServices(): Promise<AxiosResponse> {
    return ApiService.get(`${USER_MANAGEMENT_BASE_PATH}/services`);
  },

  /** Assign role+service scope to a user. */
  assignRoleToUser(data: AssignmentPayload): Promise<AxiosResponse> {
    return ApiService.post(`${USER_MANAGEMENT_BASE_PATH}/assignments`, data);
  },

  /** Remove one role assignment by assignment id. */
  removeAssignment(assignmentId: string): Promise<AxiosResponse> {
    return ApiService.delete(`${USER_MANAGEMENT_BASE_PATH}/assignments/${assignmentId}`);
  },

  /** Get all assignments for a specific role. */
  getRoleAssignments(roleName: string): Promise<AxiosResponse> {
    return ApiService.get(`${USER_MANAGEMENT_BASE_PATH}/roles/${encodeURIComponent(roleName)}/assignments`);
  },
};