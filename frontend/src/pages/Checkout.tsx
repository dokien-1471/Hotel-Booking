import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  CreditCard,
  Calendar,
  Users,
  MapPin,
  Phone,
  Mail,
  ArrowLeft,
  Shield
} from 'lucide-react';
import { api } from '@/lib/api';
import { useToast } from '@/hooks/use-toast';
import Navbar from '@/components/Navbar';

const Checkout = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [isLoading, setIsLoading] = useState(false);

  // Get booking details from URL params
  const bookingData = {
    roomId: searchParams.get('roomId'),
    roomName: searchParams.get('roomName'),
    roomPrice: Number(searchParams.get('roomPrice')),
    checkIn: searchParams.get('checkIn'),
    checkOut: searchParams.get('checkOut'),
    guests: Number(searchParams.get('guests')),
    nights: Number(searchParams.get('nights')),
    total: Number(searchParams.get('total')),
  };

  const [customerInfo, setCustomerInfo] = useState({
    fullName: searchParams.get('fullName') || '',
    email: searchParams.get('email') || '',
    phone: searchParams.get('phone') || '',
    address: '',
    city: '',
    zipCode: ''
  });

  const [paymentMethod, setPaymentMethod] = useState('vnpay');

  useEffect(() => {
    // Redirect if missing required booking data
    if (!bookingData.roomId || !bookingData.checkIn || !bookingData.checkOut) {
      navigate('/');
    }
  }, []);

  const handlePayment = async () => {
    if (!customerInfo.fullName || !customerInfo.email || !customerInfo.phone) {
      toast({
        title: "Lỗi",
        description: "Vui lòng điền đầy đủ thông tin",
        variant: "destructive"
      });
      return;
    }

    setIsLoading(true);

    try {
      // Create booking
      const booking = {
        roomId: Number(bookingData.roomId),
        checkInDate: bookingData.checkIn,
        checkOutDate: bookingData.checkOut,
        guestFullName: customerInfo.fullName,
        guestEmail: customerInfo.email,
        numOfAdults: Math.floor(bookingData.guests / 2), // Tạm tính số người lớn
        numOfChildren: bookingData.guests % 2, // Tạm tính số trẻ em
        totalPrice: bookingData.total
      };

      const response = await api.post('/bookings', booking);

      if (response.data.paymentUrl) {
        window.location.href = response.data.paymentUrl;
      } else {
        throw new Error('Không nhận được URL thanh toán');
      }

    } catch (error: any) {
      console.error('Lỗi thanh toán:', error);
      toast({
        title: "Lỗi",
        description: error.response?.data?.message || "Không thể xử lý thanh toán",
        variant: "destructive"
      });
    } finally {
      setIsLoading(false);
    }
  };

  if (!bookingData.roomId) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar onAuthClick={() => { }} />

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <Button
          variant="ghost"
          onClick={() => navigate(-1)}
          className="mb-6 hover:bg-gray-100"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Quay lại
        </Button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Booking Summary */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Calendar className="h-5 w-5" />
                  <span>Thông tin đặt phòng</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <h3 className="font-semibold text-lg">{bookingData.roomName}</h3>
                  <p className="text-gray-600">
                    {new Intl.NumberFormat('vi-VN', {
                      style: 'currency',
                      currency: 'VND'
                    }).format(bookingData.roomPrice)}/đêm
                  </p>
                </div>

                <Separator />

                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>Nhận phòng:</span>
                    <span>{bookingData.checkIn}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Trả phòng:</span>
                    <span>{bookingData.checkOut}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Số đêm:</span>
                    <span>{bookingData.nights}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Số khách:</span>
                    <span>{bookingData.guests}</span>
                  </div>
                </div>

                <Separator />

                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>Giá phòng ({bookingData.nights} đêm):</span>
                    <span>
                      {new Intl.NumberFormat('vi-VN', {
                        style: 'currency',
                        currency: 'VND'
                      }).format(bookingData.roomPrice * bookingData.nights)}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span>Thuế & phí:</span>
                    <span>0 ₫</span>
                  </div>
                  <div className="flex justify-between font-bold text-lg border-t pt-2">
                    <span>Tổng cộng:</span>
                    <span>
                      {new Intl.NumberFormat('vi-VN', {
                        style: 'currency',
                        currency: 'VND'
                      }).format(bookingData.total)}
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Payment Security */}
            <Alert>
              <Shield className="h-4 w-4" />
              <AlertDescription>
                Thông tin thanh toán của bạn được bảo mật và mã hóa. Chúng tôi hợp tác với VNPay để đảm bảo giao dịch an toàn.
              </AlertDescription>
            </Alert>
          </div>

          {/* Customer Information */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Users className="h-5 w-5" />
                  <span>Thông tin khách hàng</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="fullName">Họ và tên *</Label>
                  <Input
                    id="fullName"
                    value={customerInfo.fullName}
                    onChange={(e) => setCustomerInfo({ ...customerInfo, fullName: e.target.value })}
                    placeholder="Nguyễn Văn A"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="email">Email *</Label>
                  <Input
                    id="email"
                    type="email"
                    value={customerInfo.email}
                    onChange={(e) => setCustomerInfo({ ...customerInfo, email: e.target.value })}
                    placeholder="example@email.com"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="phone">Số điện thoại *</Label>
                  <Input
                    id="phone"
                    value={customerInfo.phone}
                    onChange={(e) => setCustomerInfo({ ...customerInfo, phone: e.target.value })}
                    placeholder="0123456789"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="address">Địa chỉ</Label>
                  <Input
                    id="address"
                    value={customerInfo.address}
                    onChange={(e) => setCustomerInfo({ ...customerInfo, address: e.target.value })}
                    placeholder="123 Đường ABC"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="city">Thành phố</Label>
                    <Input
                      id="city"
                      value={customerInfo.city}
                      onChange={(e) => setCustomerInfo({ ...customerInfo, city: e.target.value })}
                      placeholder="Hà Nội"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="zipCode">Mã bưu điện</Label>
                    <Input
                      id="zipCode"
                      value={customerInfo.zipCode}
                      onChange={(e) => setCustomerInfo({ ...customerInfo, zipCode: e.target.value })}
                      placeholder="100000"
                    />
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Payment Button */}
            <Button
              className="w-full"
              size="lg"
              onClick={handlePayment}
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <span className="animate-spin mr-2">⏳</span>
                  Đang xử lý...
                </>
              ) : (
                <>
                  <CreditCard className="mr-2 h-4 w-4" />
                  Thanh toán qua VNPay
                </>
              )}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;