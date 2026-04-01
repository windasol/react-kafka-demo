import { useEffect } from 'react';
import type { Notification } from '../types';
import { NOTIFICATION_ICON, NOTIFICATION_COLOR_CLASS, type NotificationType } from '../types';
import './ToastNotification.css';

interface ToastNotificationProps {
  toasts: Notification[];
  onDismiss: (id: number) => void;
}

const AUTO_DISMISS_MS = 4000;

export default function ToastNotification({ toasts, onDismiss }: ToastNotificationProps) {
  useEffect(() => {
    if (toasts.length === 0) return;
    const latest = toasts[toasts.length - 1];
    const timer = setTimeout(() => onDismiss(latest.id), AUTO_DISMISS_MS);
    return () => clearTimeout(timer);
  }, [toasts, onDismiss]);

  if (toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`toast-item ${NOTIFICATION_COLOR_CLASS[toast.type as NotificationType] || ''}`}
        >
          <span className="toast-icon">{NOTIFICATION_ICON[toast.type as NotificationType] || '🔔'}</span>
          <span className="toast-message">{toast.message}</span>
          <button className="toast-close" onClick={() => onDismiss(toast.id)}>×</button>
        </div>
      ))}
    </div>
  );
}
