import { Modal } from '@douyinfe/semi-ui';
import { IconTickCircle, IconCrossCircleStroked, IconHourglass } from '@douyinfe/semi-icons';
import { formatPrice } from '../utils/format';

interface SeckillResultProps {
  visible: boolean;
  onClose: () => void;
  result: {
    status: 'waiting' | 'success' | 'fail';
    title: string;
    message: string;
    orderNo?: string;
    goodsName?: string;
    seckillPrice?: number;
  };
}

export default function SeckillResult({ visible, onClose, result }: SeckillResultProps) {
  const getIcon = () => {
    switch (result.status) {
      case 'waiting':
        return <IconHourglass size="extra-large" className="text-yellow-500" />;
      case 'success':
        return <IconTickCircle size="extra-large" className="text-green-500" />;
      case 'fail':
        return <IconCrossCircleStroked size="extra-large" className="text-red-500" />;
    }
  };

  return (
    <Modal
      title={result.title}
      visible={visible}
      onCancel={onClose}
      footer={null}
      centered
    >
      <div className="text-center py-6 space-y-4">
        <div className="text-5xl">{getIcon()}</div>
        <div className="text-lg font-bold">{result.message}</div>
        {result.orderNo && (
          <div className="text-sm text-gray-500">订单号: {result.orderNo}</div>
        )}
        {result.goodsName && (
          <div className="text-sm text-gray-500">商品: {result.goodsName}</div>
        )}
        {result.seckillPrice != null && (
          <div className="text-sm text-gray-500">秒杀价: {formatPrice(result.seckillPrice)}</div>
        )}
      </div>
    </Modal>
  );
}
