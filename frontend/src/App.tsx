import { useState, useEffect, useCallback } from 'react';
import { useAuth } from './contexts/AuthContext';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import FindAccountPage from './components/FindAccountPage';
import ProductList from './components/ProductList';
import OrderForm from './components/OrderForm';
import OrderList from './components/OrderList';
import NotificationList from './components/NotificationList';
import ProfilePage from './components/ProfilePage';
import Dashboard from './components/Dashboard';
import ToastNotification from './components/ToastNotification';
import { getNotificationStreamUrl } from './api/notificationApi';
import type { Notification } from './types';
import './App.css';

type MainTab = 'orders' | 'dashboard';

function App() {
  const { isLoggedIn, username, logout, authPage } = useAuth();
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [productTrigger, setProductTrigger] = useState(0);
  const [showProfile, setShowProfile] = useState(false);
  const [activeTab, setActiveTab] = useState<MainTab>('orders');
  const [latestNotification, setLatestNotification] = useState<Notification | null>(null);
  const [toasts, setToasts] = useState<Notification[]>([]);

  useEffect(() => {
    if (!isLoggedIn) return;

    const es = new EventSource(getNotificationStreamUrl(), { withCredentials: true });

    es.addEventListener('notification', (event) => {
      const newNotification: Notification = JSON.parse(event.data);
      setLatestNotification(newNotification);
      setToasts((prev) => [...prev, newNotification]);
    });

    es.onerror = () => {
      es.close();
    };

    return () => {
      es.close();
    };
  }, [isLoggedIn]);

  const handleDismissToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  if (!isLoggedIn) {
    if (authPage === 'register') return <RegisterPage />;
    if (authPage === 'find-account') return <FindAccountPage />;
    return <LoginPage />;
  }

  const handleOrderCreated = () => {
    setRefreshTrigger((prev) => prev + 1);
    setProductTrigger((prev) => prev + 1);
  };

  const handleProductChanged = () => {
    setProductTrigger((prev) => prev + 1);
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <div>
            <h1>주문 / 알림 시스템</h1>
            <p>React + Spring Boot + Kafka + Docker</p>
          </div>
          <div className="header-user">
            <span className="username">{username}</span>
            <button className="profile-btn" onClick={() => setShowProfile(true)}>프로필</button>
            <button className="logout-btn" onClick={logout}>로그아웃</button>
          </div>
        </div>
        <nav className="app-nav">
          <button
            className={`nav-btn${activeTab === 'orders' ? ' nav-btn-active' : ''}`}
            onClick={() => setActiveTab('orders')}
          >
            주문 관리
          </button>
          <button
            className={`nav-btn${activeTab === 'dashboard' ? ' nav-btn-active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            대시보드
          </button>
        </nav>
      </header>
      <main className="container">
        {showProfile ? (
          <ProfilePage onClose={() => setShowProfile(false)} />
        ) : activeTab === 'dashboard' ? (
          <section className="panel panel-full">
            <Dashboard />
          </section>
        ) : (
          <>
            <section className="panel">
              <ProductList onProductChanged={handleProductChanged} refreshTrigger={productTrigger} />
              <OrderForm onOrderCreated={handleOrderCreated} refreshProductTrigger={productTrigger} />
              <OrderList refreshTrigger={refreshTrigger} onStockChanged={handleProductChanged} />
            </section>
            <section className="panel">
              <NotificationList latestNotification={latestNotification} />
            </section>
          </>
        )}
      </main>
      <ToastNotification toasts={toasts} onDismiss={handleDismissToast} />
    </div>
  );
}

export default App;
