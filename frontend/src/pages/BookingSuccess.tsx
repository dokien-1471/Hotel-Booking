
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle, XCircle, Calendar, CreditCard } from 'lucide-react';
import { vnpayAPI } from '@/services/api';
import { useToast } from '@/hooks/use-toast';

const BookingSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [paymentStatus, setPaymentStatus] = useState<'loading' | 'success' | 'failed'>('loading');
  const [bookingDetails, setBookingDetails] = useState<any>(null);
  const { toast } = useToast();

  useEffect(() => {
    const handlePaymentCallback = async () => {
      try {
        const params = Object.fromEntries(searchParams.entries());
        const response = await vnpayAPI.handleCallback(params);
        
        if (response.data.success) {
          setPaymentStatus('success');
          setBookingDetails(response.data.booking);
          toast({
            title: "Payment Successful",
            description: "Your booking has been confirmed!",
          });
        } else {
          setPaymentStatus('failed');
          toast({
            title: "Payment Failed",
            description: response.data.message || "Payment was not successful",
            variant: "destructive"
          });
        }
      } catch (error: any) {
        console.error('Payment callback error:', error);
        setPaymentStatus('failed');
        toast({
          title: "Error",
          description: "Failed to process payment callback",
          variant: "destructive"
        });
      }
    };

    if (searchParams.size > 0) {
      handlePaymentCallback();
    } else {
      setPaymentStatus('failed');
    }
  }, [searchParams, toast]);

  if (paymentStatus === 'loading') {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-lg text-gray-600">Processing your payment...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        <Card className="shadow-lg">
          <CardHeader className="text-center">
            <div className="mx-auto mb-4">
              {paymentStatus === 'success' ? (
                <CheckCircle className="h-16 w-16 text-green-500" />
              ) : (
                <XCircle className="h-16 w-16 text-red-500" />
              )}
            </div>
            <CardTitle className="text-2xl font-bold">
              {paymentStatus === 'success' ? 'Booking Confirmed!' : 'Booking Failed'}
            </CardTitle>
          </CardHeader>
          
          <CardContent className="space-y-6">
            {paymentStatus === 'success' && bookingDetails ? (
              <div className="space-y-4">
                <div className="bg-green-50 p-4 rounded-lg">
                  <p className="text-green-800 text-center">
                    Thank you for your booking! A confirmation email has been sent to your email address.
                  </p>
                </div>
                
                <div className="space-y-3">
                  <div className="flex items-center justify-between py-2 border-b">
                    <span className="font-medium">Booking ID:</span>
                    <span>#{bookingDetails.id}</span>
                  </div>
                  <div className="flex items-center justify-between py-2 border-b">
                    <span className="font-medium">Room:</span>
                    <span>{bookingDetails.roomName}</span>
                  </div>
                  <div className="flex items-center justify-between py-2 border-b">
                    <span className="font-medium">Check-in:</span>
                    <span>{bookingDetails.checkInDate}</span>
                  </div>
                  <div className="flex items-center justify-between py-2 border-b">
                    <span className="font-medium">Check-out:</span>
                    <span>{bookingDetails.checkOutDate}</span>
                  </div>
                  <div className="flex items-center justify-between py-2 border-b">
                    <span className="font-medium">Total Paid:</span>
                    <span className="font-bold text-green-600">${bookingDetails.totalAmount}</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="bg-red-50 p-4 rounded-lg">
                <p className="text-red-800 text-center">
                  Your payment was not successful. Please try booking again or contact our support team.
                </p>
              </div>
            )}
            
            <div className="flex flex-col sm:flex-row gap-3 pt-4">
              <Button 
                onClick={() => navigate('/')} 
                className="flex-1 bg-blue-600 hover:bg-blue-700"
              >
                <Calendar className="mr-2 h-4 w-4" />
                Back to Home
              </Button>
              {paymentStatus === 'failed' && (
                <Button 
                  onClick={() => navigate('/#rooms')} 
                  variant="outline"
                  className="flex-1"
                >
                  <CreditCard className="mr-2 h-4 w-4" />
                  Try Again
                </Button>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default BookingSuccess;
