import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';
import { api } from '@/lib/api';

interface PaymentResult {
  success: boolean;
  message: string;
  bookingId: string;
  amount: number;
  transactionId: string;
}

const PaymentResult = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [result, setResult] = useState<PaymentResult | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const processPaymentResult = async () => {
      try {
        // Get query parameters from URL
        const params = new URLSearchParams(location.search);
        const vnp_ResponseCode = params.get('vnp_ResponseCode');
        const vnp_TxnRef = params.get('vnp_TxnRef');
        const vnp_TransactionNo = params.get('vnp_TransactionNo');

        if (!vnp_ResponseCode || !vnp_TxnRef || !vnp_TransactionNo) {
          throw new Error('Thiếu thông tin thanh toán');
        }

        // Call API to verify payment
        const response = await api.post('/payments/verify', {
          responseCode: vnp_ResponseCode,
          txnRef: vnp_TxnRef,
          transactionNo: vnp_TransactionNo
        });

        setResult({
          success: vnp_ResponseCode === '00',
          message: vnp_ResponseCode === '00' ? 'Thanh toán thành công' : 'Thanh toán thất bại',
          bookingId: vnp_TxnRef.split('-')[0],
          amount: parseInt(params.get('vnp_Amount') || '0') / 100,
          transactionId: vnp_TransactionNo
        });

        // Show toast message
        toast({
          title: vnp_ResponseCode === '00' ? 'Thành công' : 'Thất bại',
          description: vnp_ResponseCode === '00' ? 'Thanh toán thành công' : 'Thanh toán thất bại',
          variant: vnp_ResponseCode === '00' ? 'default' : 'destructive'
        });
      } catch (error: any) {
        console.error('Error processing payment result:', error);
        toast({
          title: 'Lỗi',
          description: error.message || 'Có lỗi xảy ra khi xử lý kết quả thanh toán',
          variant: 'destructive'
        });
      } finally {
        setLoading(false);
      }
    };

    processPaymentResult();
  }, [location, toast]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-lg mx-auto">
        <Card>
          <CardHeader>
            <CardTitle className={`text-2xl font-bold ${result?.success ? 'text-green-600' : 'text-red-600'
              }`}>
              {result?.success ? 'Thanh toán thành công' : 'Thanh toán thất bại'}
            </CardTitle>
          </CardHeader>

          <CardContent className="space-y-4">
            {result && (
              <>
                <div className="grid grid-cols-2 gap-4">
                  <div className="text-gray-600">Mã đơn hàng:</div>
                  <div>{result.bookingId}</div>

                  <div className="text-gray-600">Số tiền:</div>
                  <div>{new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND'
                  }).format(result.amount)}</div>

                  <div className="text-gray-600">Mã giao dịch:</div>
                  <div>{result.transactionId}</div>
                </div>

                <div className={`p-4 rounded-lg ${result.success ? 'bg-green-50' : 'bg-red-50'
                  }`}>
                  <p className={`text-sm ${result.success ? 'text-green-700' : 'text-red-700'
                    }`}>
                    {result.message}
                  </p>
                </div>
              </>
            )}
          </CardContent>

          <CardFooter className="flex justify-between">
            <Button variant="outline" onClick={() => navigate('/')}>
              Về trang chủ
            </Button>
            <Button onClick={() => navigate('/profile')}>
              Xem đơn hàng
            </Button>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
};

export default PaymentResult; 