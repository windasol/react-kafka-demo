import { useEffect, useRef, useCallback } from 'react';

/** 스크롤이 하단에 도달하면 콜백을 실행하는 훅 */
export function useInfiniteScroll(onLoadMore: () => void, hasNext: boolean, isLoading: boolean) {
  const observerRef = useRef<IntersectionObserver | null>(null);

  const setSentinelRef = useCallback((node: HTMLDivElement | null) => {
    if (observerRef.current) {
      observerRef.current.disconnect();
    }

    if (!node || !hasNext || isLoading) return;

    observerRef.current = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onLoadMore();
        }
      },
      { threshold: 0.1 }
    );

    observerRef.current.observe(node);
  }, [onLoadMore, hasNext, isLoading]);

  useEffect(() => {
    return () => observerRef.current?.disconnect();
  }, []);

  return setSentinelRef;
}
