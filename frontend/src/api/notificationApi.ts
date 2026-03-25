import axios from 'axios';
import type { Notification, CursorPage } from '../types';

const API_BASE = import.meta.env.VITE_NOTIFICATION_API_URL || '';

export const fetchNotifications = async (): Promise<Notification[]> => {
  const response = await axios.get<Notification[]>(`${API_BASE}/api/notifications`);
  return response.data;
};

export const fetchNotificationsPaged = async (cursor?: number, size = 7): Promise<CursorPage<Notification>> => {
  const params = new URLSearchParams({ paged: 'true', size: String(size) });
  if (cursor != null) params.set('cursor', String(cursor));
  const response = await axios.get<CursorPage<Notification>>(`${API_BASE}/api/notifications?${params}`);
  return response.data;
};

export const markAsRead = async (id: number): Promise<Notification> => {
  const response = await axios.patch<Notification>(`${API_BASE}/api/notifications/${id}/read`);
  return response.data;
};

export const markAllAsRead = async (): Promise<void> => {
  await axios.patch(`${API_BASE}/api/notifications/read-all`);
};

export const deleteNotification = async (id: number): Promise<void> => {
  await axios.delete(`${API_BASE}/api/notifications/${id}`);
};

export const deleteAllNotifications = async (): Promise<void> => {
  await axios.delete(`${API_BASE}/api/notifications`);
};

export const getNotificationStreamUrl = (): string => {
  return `${API_BASE}/api/notifications/stream`;
};
