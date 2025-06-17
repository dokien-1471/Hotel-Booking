import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { api } from '@/lib/api';
import { useToast } from '@/hooks/use-toast';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';

const PaymentSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    const verifyPayment = async () => {
      try {
        // Convert URLSearchParams to plain object
        const params = Object.fromEntries(searchParams.entries());

        // Call API to verify payment
        const response = await api.post('/api/vnpay/verify', params);

        if (response.data.success) {
          toast({
            title: 'Payment Successful',
            description: 'Your booking has been confirmed',
          });
        } else {
          toast({
            title: 'Payment Failed',
            description: response.data.message || 'Please try again',
            variant: 'destructive',
          });
        }
      } catch (error) {
        toast({
          title: 'Error',
          description: 'Failed to verify payment',
          variant: 'destructive',
        });
      }
    };

    verifyPayment();
  }, [searchParams, toast]);

  return (
    <div className="container mx-auto py-10">
      <Card>
        <CardHeader>
          <CardTitle>Processing Payment</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col items-center space-y-4">
          <Loader2 className="h-8 w-8 animate-spin" />
          <p>Please wait while we verify your payment...</p>
          <Button onClick={() => navigate('/bookings')}>
            View My Bookings
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentSuccess; 