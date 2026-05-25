interface StockStatusProps {
  soldOut: boolean;
}

export default function StockStatus({ soldOut }: StockStatusProps) {
  if (!soldOut) {
    return null;
  }
  return (
    <span style={{ color: 'rgba(239,68,68,0.85)', fontSize: '12px', fontWeight: 600 }}>
      已售罄
    </span>
  );
}