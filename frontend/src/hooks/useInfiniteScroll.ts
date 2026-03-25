import { useEffect, useRef, useCallback } from 'react';

/** 스크롤이 하단에 도달하면 콜백을 실행하는 훅 */
export function useInfiniteScroll(onLoadMore: () => void, hasNext: boolean, isLoading: boolean) {
  const observerRef = useRef<IntersectionObserver | null>(null);
  const onLoadMoreRef = useRef(onLoadMore);

  // ref로 최신 콜백 유지 (observer 재생성 없이 항상 최신 함수 호출)
  useEffect(() => {
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  const setSentinelRef = useCallback((node: HTMLDivElement | null) => {
    if (observerRef.current) {
      observerRef.current.disconnect();
    }

    if (!node || !hasNext || isLoading) return;

    observerRef.current = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onLoadMoreRef.current();
        }
      },
      { rootMargin: '100px', threshold: 0 }
    );

    observerRef.current.observe(node);
  }, [hasNext, isLoading]);

  useEffect(() => {
    return () => observerRef.current?.disconnect();
  }, []);

  return setSentinelRef;
}
