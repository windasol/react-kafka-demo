import axios from 'axios';
import type { Notification } from '../types';

const API_BASE = import.meta.env.VITE_NOTIFICATION_API_URL || '';

export const fetchNotifications = async (): Promise<Notification[]> => {
  const response = await axios.get<Notification[]>(`${API_BASE}/api/notifications`);
  return response.data;
};

export const markAsRead = async (id: number): Promise<Notification> => {
  const response = await axios.patch<Notification>(`${API_BASE}/api/notifications/${id}/read`);
  return response.data;
};

export const markAllAsRead = async (): Promise<void> => {
  await axios.patch(`${API_BASE}/api/notifications/read-all`);
};

export const getNotificationStreamUrl = (): string => {
  return `${API_BASE}/api/notifications/stream`;
};
