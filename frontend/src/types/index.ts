/** 커서 기반 페이지 응답 */
export interface CursorPage<T> {
  content: T[];
  nextCursor: number | null;
  hasNext: boolean;
}

export type OrderStatus = 'CREATED' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  createdAt: string;
}

export interface Order {
  id?: number;
  productId?: number;
  productName: string;
  quantity: number;
  status?: OrderStatus;
  createdAt?: string;
}

export interface Notification {
  id: number;
  message: string;
  orderId: number;
  createdAt: string;
  isRead: boolean;
}

/** 각 상태에서 전이 가능한 다음 상태 */
export const NEXT_STATUS: Record<OrderStatus, OrderStatus | null> = {
  CREATED: 'CONFIRMED',
  CONFIRMED: 'SHIPPED',
  SHIPPED: 'DELIVERED',
  DELIVERED: null,
  CANCELLED: null,
};

/** 상태별 한국어 라벨 */
export const STATUS_LABEL: Record<OrderStatus, string> = {
  CREATED: '생성',
  CONFIRMED: '확인',
  SHIPPED: '배송중',
  DELIVERED: '배송완료',
  CANCELLED: '취소',
};
