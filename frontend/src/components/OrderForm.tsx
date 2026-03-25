import { useEffect, useState } from 'react';
import { createOrder } from '../api/orderApi';
import { fetchProducts } from '../api/productApi';
import type { Product } from '../types';
import './OrderForm.css';

interface OrderFormProps {
  onOrderCreated: () => void;
  refreshProductTrigger: number;
}

export default function OrderForm({ onOrderCreated, refreshProductTrigger }: OrderFormProps) {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<number | ''>('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProducts()
      .then(setProducts)
      .catch((err) => console.error('상품 목록 조회 실패:', err));
  }, [refreshProductTrigger]);

  const selectedProduct = products.find((p) => p.id === selectedProductId);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProductId) return;

    setLoading(true);
    setError(null);
    try {
      await createOrder({ productId: Number(selectedProductId), quantity });
      setSelectedProductId('');
      setQuantity(1);
      onOrderCreated();
    } catch (err: unknown) {
      const errorData = (err as { response?: { data?: { error?: string } } })?.response?.data;
      setError(errorData?.error || '주문 생성 실패');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="order-form" onSubmit={handleSubmit}>
      <h2>주문 생성</h2>
      <div className="form-group">
        <label htmlFor="product">상품 선택</label>
        <select
          id="product"
          value={selectedProductId}
          onChange={(e) => setSelectedProductId(e.target.value ? Number(e.target.value) : '')}
          required
        >
          <option value="">상품을 선택하세요</option>
          {products.map((p) => (
            <option key={p.id} value={p.id} disabled={p.stock === 0}>
              {p.name} - {p.price.toLocaleString()}원 (재고: {p.stock})
            </option>
          ))}
        </select>
      </div>
      <div className="form-group">
        <label htmlFor="quantity">수량</label>
        <input
          id="quantity"
          type="number"
          min={1}
          max={selectedProduct?.stock || 999}
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
          placeholder={selectedProduct ? `최대 ${selectedProduct.stock}개` : '상품을 먼저 선택하세요'}
          required
        />
      </div>
      {selectedProduct && (
        <div className="order-summary">
          <span className="summary-label">단가</span>
          <span className="summary-value">{selectedProduct.price.toLocaleString()}원</span>
          <span className="summary-label">총 금액</span>
          <span className="summary-value total">
            {(selectedProduct.price * quantity).toLocaleString()}원
          </span>
        </div>
      )}
      {error && <p className="form-error">{error}</p>}
      <button type="submit" disabled={loading || !selectedProductId}>
        {loading ? '처리 중...' : '주문하기'}
      </button>
    </form>
  );
}
