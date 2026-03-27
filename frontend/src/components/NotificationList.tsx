import { useEffect, useState, useCallback, useRef, type RefObject } from 'react';
import { fetchNotificationsPaged, markAsRead, markAllAsRead, deleteNotification, deleteAllNotifications, getNotificationStreamUrl } from '../api/notificationApi';
import type { Notification, NotificationType } from '../types';
import { NOTIFICATION_ICON, NOTIFICATION_COLOR_CLASS } from '../types';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import './NotificationList.css';

export default function NotificationList() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [cursor, setCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const loadingRef = useRef(false);
  const requestIdRef = useRef(0);
  const scrollContainerRef = useRef<HTMLDivElement>(null) as RefObject<HTMLDivElement>;

  const loadPage = useCallback(async (nextCursor: number | null) => {
    if (loadingRef.current && nextCursor !== null) return;
    loadingRef.current = true;
    const currentRequestId = ++requestIdRef.current;
    setIsLoading(true);
    try {
      const page = await fetchNotificationsPaged(nextCursor ?? undefined);

      if (currentRequestId !== requestIdRef.current) return;

      setNotifications((prev) => nextCursor ? [...prev, ...page.content] : page.content);
      setCursor(page.nextCursor);
      setHasNext(page.hasNext);
    } catch (err) {
      console.error('알림 조회 실패:', err);
    } finally {
      if (currentRequestId === requestIdRef.current) {
        loadingRef.current = false;
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    loadPage(null);

    const eventSource = new EventSource(getNotificationStreamUrl(), { withCredentials: true });

    // SSE로 들어오는 새 알림은 목록 상단에 추가 (커서/hasNext와 독립)
    eventSource.addEventListener('notification', (event) => {
      const newNotification: Notification = JSON.parse(event.data);
      setNotifications((prev) => {
        // 중복 방지
        if (prev.some((n) => n.id === newNotification.id)) return prev;
        return [newNotification, ...prev];
      });
    });

    eventSource.onerror = () => {
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, [loadPage]);

  const handleLoadMore = useCallback(() => {
    loadPage(cursor);
  }, [cursor, loadPage]);

  const sentinelRef = useInfiniteScroll(handleLoadMore, hasNext, isLoading, scrollContainerRef);

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

  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    try {
      await deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch (err) {
      console.error('알림 삭제 실패:', err);
    }
  };

  const handleDeleteAll = async () => {
    try {
      await deleteAllNotifications();
      setNotifications([]);
      setCursor(null);
      setHasNext(false);
    } catch (err) {
      console.error('전체 알림 삭제 실패:', err);
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
        <div className="header-actions">
          {unreadCount > 0 && (
            <button className="mark-all-read-btn" onClick={handleMarkAllAsRead}>
              모두 읽음
            </button>
          )}
          {notifications.length > 0 && (
            <button className="delete-all-btn" onClick={handleDeleteAll}>
              전체 삭제
            </button>
          )}
        </div>
      </div>
      <div className="notification-list-scroll" ref={scrollContainerRef}>
      {notifications.length === 0 && !isLoading ? (
        <p className="empty-message">알림이 없습니다.</p>
      ) : (
        <ul>
          {notifications.map((noti) => (
            <li
              key={noti.id}
              className={`notification-item ${noti.isRead ? 'read' : 'unread'} ${NOTIFICATION_COLOR_CLASS[noti.type as NotificationType] || ''}`}
              onClick={() => !noti.isRead && handleMarkAsRead(noti.id)}
            >
              <div className="notification-icon">
                {NOTIFICATION_ICON[noti.type as NotificationType] || '\u{1F514}'}
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
              <button
                className="delete-btn"
                onClick={(e) => handleDelete(e, noti.id)}
                title="알림 삭제"
              >
                &times;
              </button>
            </li>
          ))}
        </ul>
      )}
      <div ref={sentinelRef} className="scroll-sentinel" />
      {isLoading && <p className="loading-message">불러오는 중...</p>}
      </div>
    </div>
  );
}
