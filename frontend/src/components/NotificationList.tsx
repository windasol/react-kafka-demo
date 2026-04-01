import { useEffect, useState, useCallback, useRef, type RefObject } from 'react';
import { fetchNotificationsPaged, markAsRead, markAllAsRead, deleteNotification, deleteAllNotifications } from '../api/notificationApi';
import type { Notification, NotificationType } from '../types';
import { NOTIFICATION_ICON, NOTIFICATION_COLOR_CLASS } from '../types';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import './NotificationList.css';

const FILTER_TABS: { label: string; value: string | null }[] = [
  { label: '전체', value: null },
  { label: 'ORDER_CREATED', value: 'ORDER_CREATED' },
  { label: 'ORDER_STATUS_CHANGED', value: 'ORDER_STATUS_CHANGED' },
  { label: 'ORDER_CANCELLED', value: 'ORDER_CANCELLED' },
  { label: 'LOW_STOCK', value: 'LOW_STOCK' },
];

interface NotificationListProps {
  latestNotification?: Notification | null;
}

export default function NotificationList({ latestNotification }: NotificationListProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [cursor, setCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [activeType, setActiveType] = useState<string | null>(null);
  const loadingRef = useRef(false);
  const requestIdRef = useRef(0);
  const scrollContainerRef = useRef<HTMLDivElement>(null) as RefObject<HTMLDivElement>;

  const loadPage = useCallback(async (nextCursor: number | null, type: string | null) => {
    if (loadingRef.current && nextCursor !== null) return;
    loadingRef.current = true;
    const currentRequestId = ++requestIdRef.current;
    setIsLoading(true);
    try {
      const page = await fetchNotificationsPaged(nextCursor ?? undefined, 7, type ?? undefined);

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
    loadPage(null, activeType);
  }, [loadPage, activeType]);

  useEffect(() => {
    if (!latestNotification) return;
    setNotifications((prev) => {
      if (prev.some((n) => n.id === latestNotification.id)) return prev;
      return [latestNotification, ...prev];
    });
  }, [latestNotification]);

  const handleTabChange = useCallback((type: string | null) => {
    setActiveType(type);
    setNotifications([]);
    setCursor(null);
    setHasNext(false);
  }, []);

  const handleLoadMore = useCallback(() => {
    loadPage(cursor, activeType);
  }, [cursor, activeType, loadPage]);

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
      <div className="notification-filter-tabs">
        {FILTER_TABS.map((tab) => (
          <button
            key={tab.value ?? 'all'}
            className={`filter-tab${activeType === tab.value ? ' active' : ''}`}
            onClick={() => handleTabChange(tab.value)}
          >
            {tab.label}
          </button>
        ))}
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
