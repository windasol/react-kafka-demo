import { useEffect, useState } from 'react';
import { fetchProducts, createProduct, updateProduct, deleteProduct } from '../api/productApi';
import type { Product } from '../types';
import './ProductList.css';

interface ProductListProps {
  onProductChanged: () => void;
}

export default function ProductList({ onProductChanged }: ProductListProps) {
  const [products, setProducts] = useState<Product[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [name, setName] = useState('');
  const [price, setPrice] = useState(0);
  const [stock, setStock] = useState(0);
  const [loading, setLoading] = useState(false);

  const loadProducts = async () => {
    try {
      const data = await fetchProducts();
      setProducts(data);
    } catch (err) {
      console.error('상품 목록 조회 실패:', err);
    }
  };

  useEffect(() => { loadProducts(); }, []);

  const resetForm = () => {
    setName('');
    setPrice(0);
    setStock(0);
    setEditingId(null);
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    try {
      if (editingId) {
        await updateProduct(editingId, { name: name.trim(), price, stock });
      } else {
        await createProduct({ name: name.trim(), price, stock });
      }
      resetForm();
      await loadProducts();
      onProductChanged();
    } catch (err) {
      console.error('상품 저장 실패:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (product: Product) => {
    setEditingId(product.id);
    setName(product.name);
    setPrice(product.price);
    setStock(product.stock);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteProduct(id);
      await loadProducts();
      onProductChanged();
    } catch (err) {
      console.error('상품 삭제 실패:', err);
    }
  };

  return (
    <div className="product-list">
      <div className="product-header">
        <h2>상품 관리</h2>
        <button className="add-btn" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? '취소' : '+ 상품 추가'}
        </button>
      </div>

      {showForm && (
        <form className="product-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="상품명"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <input
            type="number"
            placeholder="가격"
            min={0}
            value={price}
            onChange={(e) => setPrice(Number(e.target.value))}
            required
          />
          <input
            type="number"
            placeholder="재고"
            min={0}
            value={stock}
            onChange={(e) => setStock(Number(e.target.value))}
            required
          />
          <button type="submit" disabled={loading}>
            {loading ? '저장 중...' : editingId ? '수정' : '등록'}
          </button>
        </form>
      )}

      {products.length === 0 ? (
        <p className="empty-message">등록된 상품이 없습니다.</p>
      ) : (
        <ul>
          {products.map((product) => (
            <li key={product.id} className="product-item">
              <div className="product-info">
                <span className="product-name">{product.name}</span>
                <span className="product-price">{product.price.toLocaleString()}원</span>
              </div>
              <div className="product-meta">
                <span className={`stock ${product.stock === 0 ? 'out-of-stock' : ''}`}>
                  재고: {product.stock}
                </span>
                <div className="product-actions">
                  <button className="edit-btn" onClick={() => handleEdit(product)}>수정</button>
                  <button className="del-btn" onClick={() => handleDelete(product.id)}>삭제</button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
