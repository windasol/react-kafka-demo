import { useState } from 'react';
import type { OrderStatus } from '../types';
import { STATUS_LABEL } from '../types';
import './OrderFilter.css';

export interface FilterParams {
  keyword: string;
  status: OrderStatus | '';
  dateFrom: string;
  dateTo: string;
}

interface OrderFilterProps {
  onFilter: (params: FilterParams) => void;
  onReset: () => void;
}

const ALL_STATUSES: OrderStatus[] = ['CREATED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function OrderFilter({ onFilter, onReset }: OrderFilterProps) {
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<OrderStatus | ''>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onFilter({ keyword, status, dateFrom, dateTo });
  };

  const handleReset = () => {
    setKeyword('');
    setStatus('');
    setDateFrom('');
    setDateTo('');
    onReset();
  };

  const hasFilter = keyword || status || dateFrom || dateTo;

  return (
    <div className="order-filter">
      <button
        className={`filter-toggle ${hasFilter ? 'active' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        {isOpen ? '필터 닫기' : '필터/검색'}
        {hasFilter && <span className="filter-badge">!</span>}
      </button>

      {isOpen && (
        <form className="filter-form" onSubmit={handleSubmit}>
          <div className="filter-row">
            <input
              type="text"
              placeholder="상품명 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as OrderStatus | '')}
            >
              <option value="">전체 상태</option>
              {ALL_STATUSES.map((s) => (
                <option key={s} value={s}>{STATUS_LABEL[s]}</option>
              ))}
            </select>
          </div>
          <div className="filter-row">
            <input
              type="date"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
            />
            <span className="date-separator">~</span>
            <input
              type="date"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
            />
          </div>
          <div className="filter-actions">
            <button type="submit" className="search-btn">검색</button>
            <button type="button" className="reset-btn" onClick={handleReset}>초기화</button>
          </div>
        </form>
      )}
    </div>
  );
}
