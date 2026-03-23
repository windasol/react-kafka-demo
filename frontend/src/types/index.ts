export type OrderStatus = 'CREATED' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED';

export interface Order {
  id?: number;
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
};

/** 상태별 한국어 라벨 */
export const STATUS_LABEL: Record<OrderStatus, string> = {
  CREATED: '생성',
  CONFIRMED: '확인',
  SHIPPED: '배송중',
  DELIVERED: '배송완료',
};
