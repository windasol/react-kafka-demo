# frontend 상세

포트: 5173 | 경로: `frontend/src/`

## 환경변수 (`.env`)
- `VITE_AUTH_API_URL` = http://localhost:8084
- `VITE_ORDER_API_URL` = http://localhost:8083
- `VITE_NOTIFICATION_API_URL` = http://localhost:8082

## API (`api/`)
| 파일 | 주요 함수 | BASE_URL |
|------|----------|----------|
| `authApi.ts` | `login`, `register`, `findUsername`, `resetPassword` | VITE_AUTH_API_URL |
| `orderApi.ts` | `createOrder`, `fetchOrdersPaged`, `searchOrders`, `changeOrderStatus`, `cancelOrder` | VITE_ORDER_API_URL |
| `productApi.ts` | `fetchProducts`, `createProduct`, `updateProduct`, `deleteProduct` | VITE_ORDER_API_URL |
| `notificationApi.ts` | `fetchNotificationsPaged`, `markAsRead`, `markAllAsRead`, `deleteNotification`, `deleteAllNotifications`, `getNotificationStreamUrl` | VITE_NOTIFICATION_API_URL |
| `axiosConfig.ts` | JWT Bearer 인터셉터, 401→토큰 제거+새로고침 | — |

## 타입 (`types/index.ts`)
- `Order`: id?, productId?, productName, quantity, unitPrice?, status?, createdAt?
- `Product`: id, name, price, stock, createdAt
- `Notification`: id, message, orderId, type, createdAt, isRead
- `PageResponse<T>`: content, page, size, totalElements, totalPages
- `CursorPage<T>`: content, nextCursor, hasNext
- 상수: `NEXT_STATUS`, `STATUS_LABEL`, `NOTIFICATION_ICON`, `NOTIFICATION_COLOR_CLASS`

## 컴포넌트 (`components/`)
| 파일 | Props | 역할 |
|------|-------|------|
| `App.tsx` | — | 루트. SSE 단일 인스턴스 관리, ToastNotification 렌더링 |
| `LoginPage.tsx` | — | 로그인 폼, 회원가입/계정찾기 링크 |
| `RegisterPage.tsx` | — | 회원가입 폼 |
| `FindAccountPage.tsx` | — | 아이디 찾기 / 비밀번호 재설정 탭 |
| `OrderList.tsx` | refreshTrigger, onStockChanged? | 오프셋 페이지네이션, 필터/검색, 상태 변경 |
| `OrderForm.tsx` | onOrderCreated, refreshProductTrigger | 상품 드롭다운, 주문 생성 |
| `OrderFilter.tsx` | onFilter, onReset | keyword/status/date 필터 |
| `OrderDetail.tsx` | orderId, onClose, onStatusChanged | 주문 상세 모달 |
| `Pagination.tsx` | currentPage, totalPages, onPageChange | 오프셋 페이지 버튼 (최대 5개) |
| `ProductList.tsx` | onProductChanged, refreshTrigger | 상품 CRUD, 인라인 편집 |
| `NotificationList.tsx` | latestNotification? | 커서 무한스크롤, 읽음/삭제. SSE는 App에서 주입 |
| `ToastNotification.tsx` | toasts, onDismiss | 우하단 고정 토스트 팝업. 4초 자동 소멸 |

## 컨텍스트 / 훅
- `AuthContext.tsx`: `useAuth()`. localStorage token/username. login/register/logout. authPage 상태
- `useInfiniteScroll.ts`: IntersectionObserver. `(onLoadMore, hasNext, isLoading, rootRef?) → sentinelRef`
