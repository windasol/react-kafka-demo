import { useEffect, useState } from 'react';
import { fetchOrder } from '../api/orderApi';
import type { Order, OrderStatus } from '../types';
import { STATUS_LABEL } from '../types';
import './OrderDetail.css';

interface OrderDetailProps {
  orderId: number;
  onClose: () => void;
  onStatusChanged: (updated: Order) => void;
}

export default function OrderDetail({ orderId, onClose }: OrderDetailProps) {
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchOrder(orderId)
      .then(setOrder)
      .catch(() => setError('주문 정보를 불러올 수 없습니다.'))
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) {
    return (
      <div className="order-detail-overlay" onClick={onClose}>
        <div className="order-detail" onClick={(e) => e.stopPropagation()}>
          <p className="detail-loading">불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="order-detail-overlay" onClick={onClose}>
        <div className="order-detail" onClick={(e) => e.stopPropagation()}>
          <p className="detail-error">{error || '주문을 찾을 수 없습니다.'}</p>
          <button className="detail-close-btn" onClick={onClose}>닫기</button>
        </div>
      </div>
    );
  }

  const status = order.status as OrderStatus;
  const totalPrice = order.unitPrice ? order.unitPrice * order.quantity : null;

  return (
    <div className="order-detail-overlay" onClick={onClose}>
      <div className="order-detail" onClick={(e) => e.stopPropagation()}>
        <div className="detail-header">
          <h3>주문 상세</h3>
          <button className="detail-x-btn" onClick={onClose} aria-label="닫기">&times;</button>
        </div>

        <div className="detail-body">
          <div className="detail-field">
            <span className="detail-label">주문 번호</span>
            <span className="detail-value">#{order.id}</span>
          </div>
          <div className="detail-field">
            <span className="detail-label">상품명</span>
            <span className="detail-value">{order.productName}</span>
          </div>
          <div className="detail-field">
            <span className="detail-label">수량</span>
            <span className="detail-value">{order.quantity}개</span>
          </div>
          {order.unitPrice != null && (
            <div className="detail-field">
              <span className="detail-label">단가</span>
              <span className="detail-value">{order.unitPrice.toLocaleString()}원</span>
            </div>
          )}
          {totalPrice != null && (
            <div className="detail-field detail-field-total">
              <span className="detail-label">총 금액</span>
              <span className="detail-value detail-value-total">{totalPrice.toLocaleString()}원</span>
            </div>
          )}
          <div className="detail-field">
            <span className="detail-label">상태</span>
            <span className={`order-status order-status-${status.toLowerCase()}`}>
              {STATUS_LABEL[status]}
            </span>
          </div>
          <div className="detail-field">
            <span className="detail-label">주문 일시</span>
            <span className="detail-value">
              {order.createdAt && new Date(order.createdAt).toLocaleString('ko-KR')}
            </span>
          </div>
        </div>

        <button className="detail-close-btn" onClick={onClose}>닫기</button>
      </div>
    </div>
  );
}
