import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Room } from '@/types/admin';
import { useToast } from '@/hooks/use-toast';
import { api } from '@/lib/api';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';

// Hàm tính số đêm giữa 2 ngày
const calculateNights = (checkIn: string, checkOut: string): number => {
  const oneDay = 24 * 60 * 60 * 1000;
  const start = new Date(checkIn);
  const end = new Date(checkOut);
  return Math.round(Math.abs((end.getTime() - start.getTime()) / oneDay));
};

interface BookingModalProps {
  isOpen: boolean;
  onClose: () => void;
  room: Room | null;
}

interface BookingFormData {
  guestFullName: string;
  guestEmail: string;
  checkInDate: string;
  checkOutDate: string;
  numOfAdults: number;
  numOfChildren: number;
  specialRequests?: string;
  roomId?: number;
}

const BookingModal = ({ isOpen, onClose, room }: BookingModalProps) => {
  const [formData, setFormData] = useState<BookingFormData>({
    guestFullName: '',
    guestEmail: '',
    checkInDate: '',
    checkOutDate: '',
    numOfAdults: 1,
    numOfChildren: 0,
    specialRequests: '',
  });

  const { toast } = useToast();
  const navigate = useNavigate();

  const bookingMutation = useMutation({
    mutationFn: async (data: BookingFormData) => {
      if (!room?.id) {
        throw new Error('Room information is missing');
      }

      // Format dates to LocalDate format (YYYY-MM-DD)
      const checkInDate = new Date(data.checkInDate).toISOString().split('T')[0];
      const checkOutDate = new Date(data.checkOutDate).toISOString().split('T')[0];

      // Get userId from localStorage
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Vui lòng đăng nhập để đặt phòng');
      }

      // Kiểm tra phòng có trống không
      const checkAvailability = await api.get(`/rooms/${room.id}/availability`, {
        params: {
          checkIn: checkInDate,
          checkOut: checkOutDate
        }
      });

      if (!checkAvailability.data.available) {
        throw new Error('Phòng đã được đặt trong khoảng thời gian này');
      }

      const bookingData = {
        roomId: room.id,
        checkInDate,
        checkOutDate,
        guestFullName: data.guestFullName,
        guestEmail: data.guestEmail,
        numOfAdults: data.numOfAdults,
        numOfChildren: data.numOfChildren,
        specialRequests: data.specialRequests,
        totalPrice: room.price
      };

      console.log('Sending booking data:', bookingData);
      const response = await api.post('/bookings', bookingData);
      return response;
    },
    onSuccess: (response) => {
      console.log('Booking successful:', response);
      if (response.data.paymentUrl) {
        // Điều hướng với query params thay vì state
        const params = new URLSearchParams({
          roomId: room?.id?.toString() || '',
          roomName: room?.roomType || '',
          roomPrice: room?.price?.toString() || '',
          checkIn: formData.checkInDate,
          checkOut: formData.checkOutDate,
          guests: (formData.numOfAdults + formData.numOfChildren).toString(),
          nights: calculateNights(formData.checkInDate, formData.checkOutDate).toString(),
          total: room?.price?.toString() || '',
          fullName: formData.guestFullName,
          email: formData.guestEmail,
          bookingId: response.data.bookingId?.toString() || '',
          paymentUrl: response.data.paymentUrl
        });
        navigate(`/checkout?${params.toString()}`);
        onClose();
      } else {
        toast({
          title: 'Lỗi',
          description: 'Không thể tạo đường dẫn thanh toán',
          variant: 'destructive',
        });
      }
    },
    onError: (error: any) => {
      console.error('Booking error:', error.response?.data || error);
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to create booking',
        variant: 'destructive',
      });
    },
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // Validate room
      if (!room?.id) {
        throw new Error('Room information is missing');
      }

      // Validate required fields
      if (!formData.guestFullName || !formData.guestEmail || !formData.checkInDate || !formData.checkOutDate) {
        throw new Error('Vui lòng điền đầy đủ thông tin bắt buộc');
      }

      // Validate email format
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.guestEmail)) {
        throw new Error('Email không hợp lệ');
      }

      // Validate dates
      const checkIn = new Date(formData.checkInDate);
      const checkOut = new Date(formData.checkOutDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (checkIn < today) {
        throw new Error('Ngày nhận phòng không thể là ngày trong quá khứ');
      }

      if (checkIn >= checkOut) {
        throw new Error('Ngày trả phòng phải sau ngày nhận phòng');
      }

      // Validate number of guests
      if (formData.numOfAdults < 1) {
        throw new Error('Số lượng người lớn phải lớn hơn 0');
      }

      if (formData.numOfChildren < 0) {
        throw new Error('Số lượng trẻ em không thể âm');
      }

      await bookingMutation.mutateAsync(formData);
    } catch (error: any) {
      toast({
        title: 'Lỗi',
        description: error.message,
        variant: 'destructive',
      });
    }
  };

  if (!room) return null;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Book Room {room.roomNumber}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="guestFullName">Full Name</Label>
            <Input
              id="guestFullName"
              value={formData.guestFullName}
              onChange={(e) => setFormData(prev => ({ ...prev, guestFullName: e.target.value }))}
              required
            />
          </div>

          <div>
            <Label htmlFor="guestEmail">Email</Label>
            <Input
              id="guestEmail"
              type="email"
              value={formData.guestEmail}
              onChange={(e) => setFormData(prev => ({ ...prev, guestEmail: e.target.value }))}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="checkInDate">Check-in Date</Label>
              <Input
                id="checkInDate"
                type="date"
                value={formData.checkInDate}
                onChange={(e) => setFormData(prev => ({ ...prev, checkInDate: e.target.value }))}
                min={new Date().toISOString().split('T')[0]}
                required
              />
            </div>
            <div>
              <Label htmlFor="checkOutDate">Check-out Date</Label>
              <Input
                id="checkOutDate"
                type="date"
                value={formData.checkOutDate}
                onChange={(e) => setFormData(prev => ({ ...prev, checkOutDate: e.target.value }))}
                min={formData.checkInDate || new Date().toISOString().split('T')[0]}
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="numOfAdults">Adults</Label>
              <Input
                id="numOfAdults"
                type="number"
                min="1"
                value={formData.numOfAdults}
                onChange={(e) => setFormData(prev => ({ ...prev, numOfAdults: parseInt(e.target.value) }))}
                required
              />
            </div>
            <div>
              <Label htmlFor="numOfChildren">Children</Label>
              <Input
                id="numOfChildren"
                type="number"
                min="0"
                value={formData.numOfChildren}
                onChange={(e) => setFormData(prev => ({ ...prev, numOfChildren: parseInt(e.target.value) }))}
              />
            </div>
          </div>

          <div>
            <Label htmlFor="specialRequests">Special Requests</Label>
            <Input
              id="specialRequests"
              value={formData.specialRequests}
              onChange={(e) => setFormData(prev => ({ ...prev, specialRequests: e.target.value }))}
            />
          </div>

          <div className="flex justify-end gap-2 pt-4">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" disabled={bookingMutation.isPending}>
              {bookingMutation.isPending ? 'Booking...' : 'Book Now'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default BookingModal;
