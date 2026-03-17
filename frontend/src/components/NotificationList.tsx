import { useEffect, useState } from 'react';
import { fetchNotifications } from '../api/notificationApi';
import type { Notification } from '../types';
import './NotificationList.css';

export default function NotificationList() {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    const loadNotifications = () => {
      fetchNotifications()
        .then(setNotifications)
        .catch((err) => console.error('알림 조회 실패:', err));
    };

    loadNotifications();
    const interval = setInterval(loadNotifications, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="notification-list">
      <h2>
        알림
        {notifications.length > 0 && (
          <span className="badge">{notifications.length}</span>
        )}
      </h2>
      {notifications.length === 0 ? (
        <p className="empty-message">알림이 없습니다.</p>
      ) : (
        <ul>
          {notifications.map((noti) => (
            <li key={noti.id} className="notification-item">
              <div className="notification-icon">&#128276;</div>
              <div className="notification-content">
                <p className="notification-message">{noti.message}</p>
                <span className="notification-date">
                  {new Date(noti.createdAt).toLocaleString('ko-KR')}
                </span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
