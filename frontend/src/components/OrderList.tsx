import { useEffect, useState } from 'react';
import { fetchOrders, changeOrderStatus } from '../api/orderApi';
import type { Order, OrderStatus } from '../types';
import { NEXT_STATUS, STATUS_LABEL } from '../types';
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
  const [loadingOrderId, setLoadingOrderId] = useState<number | null>(null);

  useEffect(() => {
    fetchOrders()
      .then(setOrders)
      .catch((err) => console.error('주문 목록 조회 실패:', err));
  }, [refreshTrigger]);

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
      {orders.length === 0 ? (
        <p className="empty-message">아직 주문이 없습니다.</p>
      ) : (
        <ul>
          {orders.map((order) => {
            const status = order.status as OrderStatus;
            const nextStatus = NEXT_STATUS[status];
            const isLoading = loadingOrderId === order.id;

            return (
              <li key={order.id} className="order-item">
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
                    onClick={() => handleStatusChange(order.id!, nextStatus)}
                    disabled={isLoading}
                  >
                    {isLoading ? '처리중...' : ACTION_LABEL[status]}
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
