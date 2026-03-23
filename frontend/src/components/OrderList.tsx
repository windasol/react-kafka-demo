import { useEffect, useState, useCallback } from 'react';
import { fetchOrdersPaged, changeOrderStatus } from '../api/orderApi';
import type { Order, OrderStatus } from '../types';
import { NEXT_STATUS, STATUS_LABEL } from '../types';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import OrderDetail from './OrderDetail';
import './OrderList.css';

interface OrderListProps {
  refreshTrigger: number;
}

/** 다음 상태 전이 버튼의 라벨 */
const ACTION_LABEL: Partial<Record<OrderStatus, string>> = {
  CREATED: '주문 확인',
  CONFIRMED: '배송 시작',
  SHIPPED: '배송 완료',
};

export default function OrderList({ refreshTrigger }: OrderListProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [cursor, setCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingOrderId, setLoadingOrderId] = useState<number | null>(null);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);

  const loadPage = useCallback(async (nextCursor: number | null) => {
    setIsLoading(true);
    try {
      const page = await fetchOrdersPaged(nextCursor ?? undefined);
      setOrders((prev) => nextCursor ? [...prev, ...page.content] : page.content);
      setCursor(page.nextCursor);
      setHasNext(page.hasNext);
    } catch (err) {
      console.error('주문 목록 조회 실패:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 첫 페이지 로드 + refreshTrigger 변경 시 초기화
  useEffect(() => {
    setOrders([]);
    setCursor(null);
    setHasNext(false);
    loadPage(null);
  }, [refreshTrigger, loadPage]);

  const handleLoadMore = useCallback(() => {
    loadPage(cursor);
  }, [cursor, loadPage]);

  const sentinelRef = useInfiniteScroll(handleLoadMore, hasNext, isLoading);

  const handleStatusChange = async (orderId: number, nextStatus: OrderStatus) => {
    setLoadingOrderId(orderId);
    try {
      const updated = await changeOrderStatus(orderId, nextStatus);
      setOrders((prev) =>
        prev.map((order) => (order.id === updated.id ? updated : order))
      );
    } catch (err) {
      console.error('주문 상태 변경 실패:', err);
    } finally {
      setLoadingOrderId(null);
    }
  };

  return (
    <div className="order-list">
      <h2>주문 목록</h2>
      {orders.length === 0 && !isLoading ? (
        <p className="empty-message">아직 주문이 없습니다.</p>
      ) : (
        <ul>
          {orders.map((order) => {
            const status = order.status as OrderStatus;
            const nextStatus = NEXT_STATUS[status];
            const isStatusLoading = loadingOrderId === order.id;

            return (
              <li key={order.id} className="order-item" onClick={() => order.id && setSelectedOrderId(order.id)}>
                <div className="order-info">
                  <span className="product-name">{order.productName}</span>
                  <span className="quantity">x{order.quantity}</span>
                </div>
                <div className="order-meta">
                  <span className={`status status-${status.toLowerCase()}`}>
                    {STATUS_LABEL[status]}
                  </span>
                  <span className="date">
                    {order.createdAt && new Date(order.createdAt).toLocaleString('ko-KR')}
                  </span>
                </div>
                {nextStatus && (
                  <button
                    className={`status-btn status-btn-${nextStatus.toLowerCase()}`}
                    onClick={(e) => { e.stopPropagation(); handleStatusChange(order.id!, nextStatus); }}
                    disabled={isStatusLoading}
                  >
                    {isStatusLoading ? '처리중...' : ACTION_LABEL[status]}
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      )}
      <div ref={sentinelRef} className="scroll-sentinel" />
      {isLoading && <p className="loading-message">불러오는 중...</p>}
      {selectedOrderId !== null && (
        <OrderDetail
          orderId={selectedOrderId}
          onClose={() => setSelectedOrderId(null)}
          onStatusChanged={(updated) => {
            setOrders((prev) =>
              prev.map((o) => (o.id === updated.id ? updated : o))
            );
          }}
        />
      )}
    </div>
  );
}
