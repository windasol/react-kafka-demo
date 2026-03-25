import { useEffect, useLayoutEffect, useRef, useCallback } from 'react';

/** 스크롤이 하단에 도달하면 콜백을 실행하는 훅 */
export function useInfiniteScroll(onLoadMore: () => void, hasNext: boolean, isLoading: boolean) {
  const observerRef = useRef<IntersectionObserver | null>(null);
  const onLoadMoreRef = useRef(onLoadMore);

  // useLayoutEffect로 ref를 paint 전에 갱신하여 observer 콜백과의 race condition 방지
  useLayoutEffect(() => {
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  const setSentinelRef = useCallback((node: HTMLDivElement | null) => {
    if (observerRef.current) {
      observerRef.current.disconnect();
      observerRef.current = null;
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
