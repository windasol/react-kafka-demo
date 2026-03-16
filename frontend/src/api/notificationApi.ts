import axios from 'axios';
import { Notification } from '../types';

const API_BASE = import.meta.env.VITE_NOTIFICATION_API_URL || '';

export const fetchNotifications = async (): Promise<Notification[]> => {
  const response = await axios.get<Notification[]>(`${API_BASE}/api/notifications`);
  return response.data;
};
