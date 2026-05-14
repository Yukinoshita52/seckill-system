import { useEffect } from 'react';
import { useCountdown } from '../hooks/useCountdown';
import { formatCountdown } from '../utils/format';

interface CountdownProps {
  targetTime: string;
  onComplete?: () => void;
}

export default function Countdown({ targetTime, onComplete }: CountdownProps) {
  const { hours, minutes, seconds, isFinished } = useCountdown(targetTime);

  useEffect(() => {
    if (isFinished) {
      onComplete?.();
    }
  }, [isFinished, onComplete]);

  if (isFinished) {
    return <span className="text-green-500 font-bold">已开始</span>;
  }

  return (
    <span className="text-red-500 font-bold">
      距开始 {formatCountdown(hours, minutes, seconds)}
    </span>
  );
}
