import { useCallback, useEffect, useState } from 'react';

export interface AsyncState<T> {
  data: T | null;
  loading: boolean;
  error: unknown;
  /** Re-run the fetcher. */
  retry: () => void;
}

/**
 * The single seam for data fetching with loading/error state. Replaces the
 * hand-rolled `useState(loading/error/data) + useEffect + try/catch` blocks that
 * were copy-pasted (inconsistently) across components. The fetcher's identity is
 * intentionally NOT a dependency — pass the real inputs in `deps` so inline
 * arrow fetchers don't re-run every render.
 */
export function useAsyncData<T>(fetcher: () => Promise<T>, deps: any[] = []): AsyncState<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [attempt, setAttempt] = useState(0);

  const retry = useCallback(() => setAttempt((a) => a + 1), []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    fetcher()
      .then((result) => {
        if (!cancelled) {
          setData(result);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err);
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, attempt]);

  return { data, loading, error, retry };
}
