import { useState, useEffect, useCallback } from 'react';
import { Modal, Input, Button, Toast } from '@douyinfe/semi-ui';
import { IconRefresh } from '@douyinfe/semi-icons';
import { orderApi } from '../api/order';

interface CaptchaDialogProps {
  visible: boolean;
  onConfirm: (captchaToken: string, captchaCode: string) => void;
  onCancel: () => void;
}

export default function CaptchaDialog({ visible, onConfirm, onCancel }: CaptchaDialogProps) {
  const [captchaToken, setCaptchaToken] = useState('');
  const [captchaImage, setCaptchaImage] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchCaptcha = useCallback(async () => {
    setLoading(true);
    try {
      const res = await orderApi.getCaptcha();
      setCaptchaToken(res.data.captchaToken);
      setCaptchaImage(res.data.captchaImage);
      setCode('');
    } catch {
      Toast.error('获取验证码失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (visible) fetchCaptcha();
  }, [visible, fetchCaptcha]);

  const handleSubmit = () => {
    if (!code.trim()) {
      Toast.warning('请输入验证码');
      return;
    }
    onConfirm(captchaToken, code.trim());
  };

  return (
    <Modal
      title="请输入验证码"
      visible={visible}
      onCancel={onCancel}
      footer={null}
      centered
      width={360}
    >
      <div className="flex flex-col gap-4 py-2">
        <div className="flex items-center gap-2">
          {captchaImage && (
            <img
              src={captchaImage}
              alt="验证码"
              className="h-12 rounded cursor-pointer"
              onClick={fetchCaptcha}
              title="点击刷新验证码"
            />
          )}
          <Button
            icon={<IconRefresh />}
            onClick={fetchCaptcha}
            loading={loading}
            type="tertiary"
          />
        </div>
        <Input
          value={code}
          onChange={setCode}
          placeholder="请输入验证码"
          onEnterPress={handleSubmit}
          autoFocus
          size="large"
        />
        <Button
          type="primary"
          theme="solid"
          block
          onClick={handleSubmit}
          size="large"
        >
          确认抢购
        </Button>
      </div>
    </Modal>
  );
}