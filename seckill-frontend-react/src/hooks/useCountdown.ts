import { useState, useEffect, useRef } from 'react';

interface CountdownResult {
  hours: number;
  minutes: number;
  seconds: number;
  isFinished: boolean;
}

export function useCountdown(targetTime: string | Date): CountdownResult {
  const [result, setResult] = useState<CountdownResult>({
    hours: 0,
    minutes: 0,
    seconds: 0,
    isFinished: false,
  });
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    const target = new Date(targetTime).getTime();

    const updateCountdown = () => {
      const now = Date.now();
      const diff = target - now;

      if (diff <= 0) {
        setResult({ hours: 0, minutes: 0, seconds: 0, isFinished: true });
        if (timerRef.current) {
          clearInterval(timerRef.current);
        }
        return;
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      setResult({ hours, minutes, seconds, isFinished: false });
    };

    updateCountdown();
    timerRef.current = window.setInterval(updateCountdown, 1000);

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, [targetTime]);

  return result;
}
