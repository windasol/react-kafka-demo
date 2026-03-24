import { useState } from 'react';
import ProductList from './components/ProductList';
import OrderForm from './components/OrderForm';
import OrderList from './components/OrderList';
import NotificationList from './components/NotificationList';
import './App.css';

function App() {
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [productTrigger, setProductTrigger] = useState(0);

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
        <h1>주문 / 알림 시스템</h1>
        <p>React + Spring Boot + Kafka + Docker</p>
      </header>
      <main className="container">
        <section className="panel">
          <ProductList onProductChanged={handleProductChanged} />
          <OrderForm onOrderCreated={handleOrderCreated} refreshProductTrigger={productTrigger} />
          <OrderList refreshTrigger={refreshTrigger} />
        </section>
        <section className="panel">
          <NotificationList />
        </section>
      </main>
    </div>
  );
}

export default App;
