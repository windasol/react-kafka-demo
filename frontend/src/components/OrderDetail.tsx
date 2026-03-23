import { useEffect, useState } from 'react';
import { fetchOrder, changeOrderStatus } from '../api/orderApi';
import type { Order, OrderStatus } from '../types';
import { NEXT_STATUS, STATUS_LABEL } from '../types';
import './OrderDetail.css';

interface OrderDetailProps {
  orderId: number;
  onClose: () => void;
  onStatusChanged: (updated: Order) => void;
}

const ACTION_LABEL: Partial<Record<OrderStatus, string>> = {
  CREATED: '주문 확인',
  CONFIRMED: '배송 시작',
  SHIPPED: '배송 완료',
};

export default function OrderDetail({ orderId, onClose, onStatusChanged }: OrderDetailProps) {
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchOrder(orderId)
      .then(setOrder)
      .catch(() => setError('주문 정보를 불러올 수 없습니다.'))
      .finally(() => setLoading(false));
  }, [orderId]);

  const handleStatusChange = async (nextStatus: OrderStatus) => {
    setActionLoading(true);
    try {
      const updated = await changeOrderStatus(orderId, nextStatus);
      setOrder(updated);
      onStatusChanged(updated);
    } catch {
      setError('상태 변경에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="order-detail-overlay" onClick={onClose}>
        <div className="order-detail" onClick={(e) => e.stopPropagation()}>
          <p className="loading-message">로딩중...</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="order-detail-overlay" onClick={onClose}>
        <div className="order-detail" onClick={(e) => e.stopPropagation()}>
          <p className="error-message">{error || '주문을 찾을 수 없습니다.'}</p>
          <button className="close-btn" onClick={onClose}>닫기</button>
        </div>
      </div>
    );
  }

  const status = order.status as OrderStatus;
  const nextStatus = NEXT_STATUS[status];

  return (
    <div className="order-detail-overlay" onClick={onClose}>
      <div className="order-detail" onClick={(e) => e.stopPropagation()}>
        <div className="detail-header">
          <h3>주문 상세</h3>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>
        <dl className="detail-body">
          <dt>주문 번호</dt>
          <dd>#{order.id}</dd>
          <dt>상품명</dt>
          <dd>{order.productName}</dd>
          <dt>수량</dt>
          <dd>{order.quantity}개</dd>
          <dt>상태</dt>
          <dd>
            <span className={`status status-${status.toLowerCase()}`}>
              {STATUS_LABEL[status]}
            </span>
          </dd>
          <dt>주문 일시</dt>
          <dd>{order.createdAt && new Date(order.createdAt).toLocaleString('ko-KR')}</dd>
        </dl>
        {nextStatus && (
          <button
            className={`status-btn status-btn-${nextStatus.toLowerCase()}`}
            onClick={() => handleStatusChange(nextStatus)}
            disabled={actionLoading}
          >
            {actionLoading ? '처리중...' : ACTION_LABEL[status]}
          </button>
        )}
      </div>
    </div>
  );
}
