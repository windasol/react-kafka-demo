import { useState } from 'react';
import { createOrder } from '../api/orderApi';
import './OrderForm.css';

interface OrderFormProps {
  onOrderCreated: () => void;
}

export default function OrderForm({ onOrderCreated }: OrderFormProps) {
  const [productName, setProductName] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productName.trim()) return;

    setLoading(true);
    try {
      await createOrder({ productName: productName.trim(), quantity });
      setProductName('');
      setQuantity(1);
      onOrderCreated();
    } catch (error) {
      console.error('주문 생성 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="order-form" onSubmit={handleSubmit}>
      <h2>주문 생성</h2>
      <div className="form-group">
        <label htmlFor="productName">상품명</label>
        <input
          id="productName"
          type="text"
          value={productName}
          onChange={(e) => setProductName(e.target.value)}
          placeholder="상품명을 입력하세요"
          required
        />
      </div>
      <div className="form-group">
        <label htmlFor="quantity">수량</label>
        <input
          id="quantity"
          type="number"
          min={1}
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
          required
        />
      </div>
      <button type="submit" disabled={loading}>
        {loading ? '처리 중...' : '주문하기'}
      </button>
    </form>
  );
}
