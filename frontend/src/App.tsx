import { useState } from 'react';
import { useAuth } from './contexts/AuthContext';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import FindAccountPage from './components/FindAccountPage';
import ProductList from './components/ProductList';
import OrderForm from './components/OrderForm';
import OrderList from './components/OrderList';
import NotificationList from './components/NotificationList';
import './App.css';

function App() {
  const { isLoggedIn, username, logout, authPage } = useAuth();
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [productTrigger, setProductTrigger] = useState(0);

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
            <button className="logout-btn" onClick={logout}>로그아웃</button>
          </div>
        </div>
      </header>
      <main className="container">
        <section className="panel">
          <ProductList onProductChanged={handleProductChanged} refreshTrigger={productTrigger} />
          <OrderForm onOrderCreated={handleOrderCreated} refreshProductTrigger={productTrigger} />
          <OrderList refreshTrigger={refreshTrigger} onStockChanged={handleProductChanged} />
        </section>
        <section className="panel">
          <NotificationList />
        </section>
      </main>
    </div>
  );
}

export default App;
