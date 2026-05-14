import { useState, useEffect, useRef, useCallback } from 'react';

interface PollingOptions<T> {
  fetchFn: () => Promise<T>;
  interval: number;
  maxAttempts?: number;
  condition: (data: T) => boolean;
  onSuccess?: (data: T) => void;
  onTimeout?: () => void;
}

interface PollingResult<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  attemptCount: number;
  startPolling: () => void;
  stopPolling: () => void;
}

export function usePolling<T>(options: PollingOptions<T>): PollingResult<T> {
  const { fetchFn, interval, maxAttempts = 30, condition, onSuccess, onTimeout } = options;
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [attemptCount, setAttemptCount] = useState(0);
  const timerRef = useRef<number | null>(null);
  const attemptRef = useRef(0);

  const stopPolling = useCallback(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  const startPolling = useCallback(() => {
    setLoading(true);
    setError(null);
    attemptRef.current = 0;
    setAttemptCount(0);

    const poll = async () => {
      attemptRef.current += 1;
      setAttemptCount(attemptRef.current);

      try {
        const result = await fetchFn();
        setData(result);

        if (condition(result)) {
          stopPolling();
          setLoading(false);
          onSuccess?.(result);
        } else if (attemptRef.current >= maxAttempts) {
          stopPolling();
          setLoading(false);
          onTimeout?.();
        }
      } catch (err) {
        stopPolling();
        setLoading(false);
        setError((err as Error).message);
      }
    };

    poll();
    timerRef.current = window.setInterval(poll, interval);
  }, [fetchFn, interval, maxAttempts, condition, onSuccess, onTimeout, stopPolling]);

  useEffect(() => {
    return () => {
      stopPolling();
    };
  }, [stopPolling]);

  return { data, loading, error, attemptCount, startPolling, stopPolling };
}
