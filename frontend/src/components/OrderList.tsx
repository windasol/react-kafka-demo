import { useEffect, useState } from 'react';
import { fetchOrders } from '../api/orderApi';
import { Order } from '../types';
import './OrderList.css';

interface OrderListProps {
  refreshTrigger: number;
}

export default function OrderList({ refreshTrigger }: OrderListProps) {
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    fetchOrders()
      .then(setOrders)
      .catch((err) => console.error('주문 목록 조회 실패:', err));
  }, [refreshTrigger]);

  return (
    <div className="order-list">
      <h2>주문 목록</h2>
      {orders.length === 0 ? (
        <p className="empty-message">아직 주문이 없습니다.</p>
      ) : (
        <ul>
          {orders.map((order) => (
            <li key={order.id} className="order-item">
              <div className="order-info">
                <span className="product-name">{order.productName}</span>
                <span className="quantity">x{order.quantity}</span>
              </div>
              <div className="order-meta">
                <span className={`status status-${order.status?.toLowerCase()}`}>
                  {order.status}
                </span>
                <span className="date">
                  {order.createdAt && new Date(order.createdAt).toLocaleString('ko-KR')}
                </span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
