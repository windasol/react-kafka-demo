import { useEffect, useState, useCallback } from 'react';
import { fetchOrderStats } from '../api/orderApi';
import type { OrderStatsSummary } from '../types';
import './Dashboard.css';

export default function Dashboard() {
  const [stats, setStats] = useState<OrderStatsSummary | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadStats = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchOrderStats();
      setStats(data);
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } }).response?.data?.message ??
        '일시적 오류가 발생했습니다. 다시 시도해주세요.';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>주문 통계 대시보드</h2>
        <button
          className="dashboard-refresh-btn"
          onClick={loadStats}
          disabled={isLoading}
        >
          {isLoading ? '불러오는 중...' : '새로고침'}
        </button>
      </div>

      {error && <p className="dashboard-error">{error}</p>}

      {isLoading && !stats && <p className="dashboard-loading">통계를 불러오는 중...</p>}

      {stats && (
        <>
          <div className="dashboard-stat-cards">
            <div className="stat-card">
              <span className="stat-card-label">전체 주문</span>
              <span className="stat-card-value">{stats.totalOrders.toLocaleString()}</span>
            </div>
            <div className="stat-card">
              <span className="stat-card-label">대기 중</span>
              <span className="stat-card-value">{stats.pendingOrders.toLocaleString()}</span>
            </div>
            <div className="stat-card">
              <span className="stat-card-label">완료</span>
              <span className="stat-card-value">{stats.completedOrders.toLocaleString()}</span>
            </div>
            <div className="stat-card">
              <span className="stat-card-label">취소</span>
              <span className="stat-card-value">{stats.cancelledOrders.toLocaleString()}</span>
            </div>
          </div>

          <div className="dashboard-revenue">
            <span className="dashboard-revenue-label">총 매출액</span>
            <span className="dashboard-revenue-value">{stats.totalRevenue.toLocaleString()}원</span>
          </div>

          <div className="dashboard-daily">
            <h3>일별 통계</h3>
            {stats.dailyStats.length === 0 ? (
              <p className="dashboard-empty">일별 통계 데이터가 없습니다.</p>
            ) : (
              <table className="dashboard-table">
                <thead>
                  <tr>
                    <th>날짜</th>
                    <th>주문 수</th>
                    <th>매출액</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.dailyStats.map((stat) => (
                    <tr key={stat.date}>
                      <td>{stat.date}</td>
                      <td>{stat.count.toLocaleString()}</td>
                      <td>{stat.revenue.toLocaleString()}원</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
}
