import { useEffect, useState, useCallback } from 'react';
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  BarChart, Bar,
} from 'recharts';
import { fetchOrderStats } from '../api/orderApi';
import type { OrderStatsSummary } from '../types';
import './Dashboard.css';

const STATUS_COLORS = ['#f59e0b', '#10b981', '#ef4444'];

function formatYAxisRevenue(v: number): string {
  if (v === 0) return '0';
  if (v >= 10_000) return `${Math.round(v / 10_000)}만`;
  return `${(v / 1_000).toFixed(0)}천`;
}

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

  const statusPieData = stats
    ? [
        { name: '대기 중', value: Number(stats.pendingOrders) },
        { name: '완료', value: Number(stats.completedOrders) },
        { name: '취소', value: Number(stats.cancelledOrders) },
      ]
    : [];

  const hasChartData = stats != null && stats.dailyStats.length > 0;

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
          {/* 요약 카드 */}
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

          {/* 총 매출액 배너 */}
          <div className="dashboard-revenue">
            <span className="dashboard-revenue-label">총 매출액</span>
            <span className="dashboard-revenue-value">{stats.totalRevenue.toLocaleString()}원</span>
          </div>

          {/* 차트 영역 */}
          {hasChartData ? (
            <>
              {/* 2열: 상태 분포 파이 + 일별 주문 수 바 */}
              <div className="dashboard-charts-row">
                <div className="dashboard-chart-card">
                  <h3>주문 상태 분포</h3>
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie
                        data={statusPieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={55}
                        outerRadius={85}
                        paddingAngle={3}
                        dataKey="value"
                      >
                        {statusPieData.map((_, index) => (
                          <Cell key={index} fill={STATUS_COLORS[index]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value: number) => value.toLocaleString()} />
                      <Legend iconType="circle" iconSize={10} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="dashboard-chart-card">
                  <h3>일별 주문 수</h3>
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart
                      data={stats.dailyStats}
                      margin={{ top: 4, right: 8, left: -16, bottom: 0 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                      <XAxis
                        dataKey="date"
                        tick={{ fontSize: 11 }}
                        tickFormatter={(v: string) => v.slice(5)}
                      />
                      <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
                      <Tooltip
                        formatter={(value: number) => [value.toLocaleString(), '주문 수']}
                      />
                      <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>

              {/* 전체 너비: 일별 매출 추이 에어리어 차트 */}
              <div className="dashboard-chart-card dashboard-chart-full">
                <h3>일별 매출 추이</h3>
                <ResponsiveContainer width="100%" height={200}>
                  <AreaChart
                    data={stats.dailyStats}
                    margin={{ top: 4, right: 8, left: 8, bottom: 0 }}
                  >
                    <defs>
                      <linearGradient id="revenueGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#1e1e2e" stopOpacity={0.15} />
                        <stop offset="95%" stopColor="#1e1e2e" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis
                      dataKey="date"
                      tick={{ fontSize: 11 }}
                      tickFormatter={(v: string) => v.slice(5)}
                    />
                    <YAxis tick={{ fontSize: 11 }} tickFormatter={formatYAxisRevenue} />
                    <Tooltip
                      formatter={(value: number) => [`${value.toLocaleString()}원`, '매출액']}
                    />
                    <Area
                      type="monotone"
                      dataKey="revenue"
                      stroke="#1e1e2e"
                      strokeWidth={2}
                      fill="url(#revenueGrad)"
                      dot={{ r: 3, fill: '#1e1e2e' }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </>
          ) : (
            <p className="dashboard-empty">일별 통계 데이터가 없습니다.</p>
          )}

          {/* 일별 통계 테이블 */}
          {hasChartData && (
            <div className="dashboard-daily">
              <h3>일별 통계</h3>
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
            </div>
          )}
        </>
      )}
    </div>
  );
}
