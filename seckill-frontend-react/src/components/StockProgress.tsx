import { Progress } from '@douyinfe/semi-ui';

interface StockProgressProps {
  totalStock: number;
  remainStock: number;
}

export default function StockProgress({ totalStock, remainStock }: StockProgressProps) {
  const soldStock = totalStock - remainStock;
  const percent = totalStock > 0 ? Math.round((soldStock / totalStock) * 100) : 0;

  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs text-gray-500">
        <span>已售 {soldStock}</span>
        <span>库存 {remainStock}</span>
      </div>
      <Progress
        percent={percent}
        showInfo={false}
        stroke="rgba(239,68,68,0.85)"
      />
    </div>
  );
}
