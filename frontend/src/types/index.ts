/** 커서 기반 페이지 응답 */
export interface CursorPage<T> {
  content: T[];
  nextCursor: number | null;
  hasNext: boolean;
}

/** 오프셋 기반 페이지 응답 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
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
  unitPrice?: number;
  status?: OrderStatus;
  createdAt?: string;
}

export type NotificationType =
  | 'ORDER_CREATED'
  | 'ORDER_CONFIRMED'
  | 'ORDER_SHIPPED'
  | 'ORDER_DELIVERED'
  | 'ORDER_CANCELLED';

export interface Notification {
  id: number;
  message: string;
  orderId: number;
  type: NotificationType;
  createdAt: string;
  isRead: boolean;
}

/** 알림 타입별 아이콘 */
export const NOTIFICATION_ICON: Record<NotificationType, string> = {
  ORDER_CREATED: '\u{1F4E6}',    // 📦
  ORDER_CONFIRMED: '\u{2705}',   // ✅
  ORDER_SHIPPED: '\u{1F69A}',    // 🚚
  ORDER_DELIVERED: '\u{1F389}',  // 🎉
  ORDER_CANCELLED: '\u{274C}',   // ❌
};

/** 알림 타입별 CSS 클래스 */
export const NOTIFICATION_COLOR_CLASS: Record<NotificationType, string> = {
  ORDER_CREATED: 'noti-created',
  ORDER_CONFIRMED: 'noti-confirmed',
  ORDER_SHIPPED: 'noti-shipped',
  ORDER_DELIVERED: 'noti-delivered',
  ORDER_CANCELLED: 'noti-cancelled',
};

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
