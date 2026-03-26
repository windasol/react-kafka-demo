import { useEffect, useState, useCallback, useRef } from 'react';
import { fetchOrdersPaged, changeOrderStatus, cancelOrder, searchOrders } from '../api/orderApi';
import type { Order, OrderStatus } from '../types';
import { NEXT_STATUS, STATUS_LABEL } from '../types';
import OrderDetail from './OrderDetail';
import OrderFilter, { type FilterParams } from './OrderFilter';
import Pagination from './Pagination';
import './OrderList.css';

interface OrderListProps {
  refreshTrigger: number;
  onStockChanged?: () => void;
}

/** 다음 상태 전이 버튼의 라벨 */
const ACTION_LABEL: Partial<Record<OrderStatus, string>> = {
  CREATED: '주문 확인',
  CONFIRMED: '배송 시작',
  SHIPPED: '배송 완료',
};

export default function OrderList({ refreshTrigger, onStockChanged }: OrderListProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingOrderId, setLoadingOrderId] = useState<number | null>(null);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [filter, setFilter] = useState<FilterParams | null>(null);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(async (page: number, filterParams: FilterParams | null) => {
    const currentRequestId = ++requestIdRef.current;
    setIsLoading(true);
    try {
      const hasFilter = filterParams && (filterParams.keyword || filterParams.status || filterParams.dateFrom || filterParams.dateTo);

      const result = hasFilter
        ? await searchOrders({
            page,
            keyword: filterParams.keyword || undefined,
            status: (filterParams.status as OrderStatus) || undefined,
            dateFrom: filterParams.dateFrom || undefined,
            dateTo: filterParams.dateTo || undefined,
          })
        : await fetchOrdersPaged(page);

      // 이전 요청의 응답은 무시 (stale response 방지)
      if (currentRequestId !== requestIdRef.current) return;

      setOrders(result.content);
      setCurrentPage(result.page);
      setTotalPages(result.totalPages);
    } catch (err) {
      console.error('주문 목록 조회 실패:', err);
    } finally {
      if (currentRequestId === requestIdRef.current) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    setCurrentPage(0);
    loadPage(0, filter);
  }, [refreshTrigger, loadPage, filter]);

  const handlePageChange = useCallback((page: number) => {
    loadPage(page, filter);
  }, [filter, loadPage]);

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

  const handleCancel = async (orderId: number) => {
    setLoadingOrderId(orderId);
    try {
      const updated = await cancelOrder(orderId);
      setOrders((prev) =>
        prev.map((order) => (order.id === updated.id ? updated : order))
      );
      onStockChanged?.();
    } catch (err) {
      console.error('주문 취소 실패:', err);
    } finally {
      setLoadingOrderId(null);
    }
  };

  const handleFilter = (params: FilterParams) => {
    setFilter(params);
  };

  const handleReset = () => {
    setFilter(null);
  };

  return (
    <div className="order-list">
      <h2>주문 목록</h2>
      <OrderFilter onFilter={handleFilter} onReset={handleReset} />
      <div className="order-list-body">
      {orders.length === 0 && !isLoading ? (
        <p className="empty-message">{filter ? '검색 결과가 없습니다.' : '아직 주문이 없습니다.'}</p>
      ) : (
        <ul>
          {orders.map((order) => {
            const status = order.status as OrderStatus;
            const nextStatus = NEXT_STATUS[status];
            const isStatusLoading = loadingOrderId === order.id;
            const totalPrice = order.unitPrice ? order.unitPrice * order.quantity : null;

            return (
              <li key={order.id} className="order-item" onClick={() => order.id && setSelectedOrderId(order.id)}>
                <div className="order-top-row">
                  <span className="order-product-name">{order.productName}</span>
                  <span className={`order-status order-status-${status.toLowerCase()}`}>
                    {STATUS_LABEL[status]}
                  </span>
                </div>
                <div className="order-detail-row">
                  <span>
                    <span className="order-detail-label">수량</span>
                    <span className="order-detail-value">{order.quantity}개</span>
                  </span>
                  {order.unitPrice != null && (
                    <span>
                      <span className="order-detail-label">단가</span>
                      <span className="order-detail-value">{order.unitPrice.toLocaleString()}원</span>
                    </span>
                  )}
                  {totalPrice != null && (
                    <span>
                      <span className="order-detail-label">합계</span>
                      <span className="order-detail-value">{totalPrice.toLocaleString()}원</span>
                    </span>
                  )}
                </div>
                <div className="order-bottom-row">
                  <span className="order-date">
                    {order.createdAt && new Date(order.createdAt).toLocaleString('ko-KR')}
                  </span>
                  <div className="order-actions">
                    {nextStatus && (
                      <button
                        className={`action-btn action-btn-${nextStatus.toLowerCase()}`}
                        onClick={(e) => { e.stopPropagation(); handleStatusChange(order.id!, nextStatus); }}
                        disabled={isStatusLoading}
                      >
                        {isStatusLoading ? '...' : ACTION_LABEL[status]}
                      </button>
                    )}
                    {(status === 'CREATED' || status === 'CONFIRMED') && (
                      <button
                        className="action-btn action-btn-cancel"
                        onClick={(e) => { e.stopPropagation(); handleCancel(order.id!); }}
                        disabled={isStatusLoading}
                      >
                        {isStatusLoading ? '...' : '취소'}
                      </button>
                    )}
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      )}
      {isLoading && <p className="loading-message">불러오는 중...</p>}
      </div>
      <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
      {selectedOrderId !== null && (
        <OrderDetail
          orderId={selectedOrderId}
          onClose={() => setSelectedOrderId(null)}
          onStatusChanged={() => {}}
        />
      )}
    </div>
  );
}
