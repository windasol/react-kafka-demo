export interface Order {
  id?: number;
  productName: string;
  quantity: number;
  status?: string;
  createdAt?: string;
}

export interface Notification {
  id: number;
  message: string;
  orderId: number;
  createdAt: string;
  isRead: boolean;
}
