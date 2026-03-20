import { useEffect, useState, useCallback } from 'react';
import { fetchNotifications, markAsRead, markAllAsRead, getNotificationStreamUrl } from '../api/notificationApi';
import type { Notification } from '../types';
import './NotificationList.css';

export default function NotificationList() {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  const loadNotifications = useCallback(() => {
    fetchNotifications()
      .then(setNotifications)
      .catch((err) => console.error('알림 조회 실패:', err));
  }, []);

  useEffect(() => {
    loadNotifications();

    const eventSource = new EventSource(getNotificationStreamUrl());

    eventSource.addEventListener('notification', (event) => {
      const newNotification: Notification = JSON.parse(event.data);
      setNotifications((prev) => [newNotification, ...prev]);
    });

    eventSource.onerror = () => {
      eventSource.close();
      // SSE 연결 끊기면 폴링으로 폴백
      const interval = setInterval(loadNotifications, 5000);
      return () => clearInterval(interval);
    };

    return () => eventSource.close();
  }, [loadNotifications]);

  const handleMarkAsRead = async (id: number) => {
    try {
      const updated = await markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === updated.id ? updated : n))
      );
    } catch (err) {
      console.error('읽음 처리 실패:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) =>
        prev.map((n) => ({ ...n, isRead: true }))
      );
    } catch (err) {
      console.error('일괄 읽음 처리 실패:', err);
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="notification-list">
      <div className="notification-header">
        <h2>
          알림
          {unreadCount > 0 && (
            <span className="badge">{unreadCount}</span>
          )}
        </h2>
        {unreadCount > 0 && (
          <button className="mark-all-read-btn" onClick={handleMarkAllAsRead}>
            모두 읽음
          </button>
        )}
      </div>
      {notifications.length === 0 ? (
        <p className="empty-message">알림이 없습니다.</p>
      ) : (
        <ul>
          {notifications.map((noti) => (
            <li
              key={noti.id}
              className={`notification-item ${noti.isRead ? 'read' : 'unread'}`}
              onClick={() => !noti.isRead && handleMarkAsRead(noti.id)}
            >
              <div className="notification-icon">
                {noti.isRead ? '\u{1F514}' : '\u{1F515}'}
              </div>
              <div className="notification-content">
                <p className="notification-message">{noti.message}</p>
                <div className="notification-meta">
                  <span className="notification-date">
                    {new Date(noti.createdAt).toLocaleString('ko-KR')}
                  </span>
                  {!noti.isRead && <span className="unread-dot" />}
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
