import { Tag } from '@douyinfe/semi-ui';

interface StockStatusProps {
  soldOut: boolean;
}

export default function StockStatus({ soldOut }: StockStatusProps) {
  if (soldOut) {
    return <Tag color="red" size="large">已售罄</Tag>;
  }
  return <Tag color="green" size="large">有货</Tag>;
}