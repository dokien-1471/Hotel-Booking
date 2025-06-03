
import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { CalendarIcon, Users, CreditCard } from 'lucide-react';
import { format } from 'date-fns';
import { cn } from '@/lib/utils';
import { bookingAPI, vnpayAPI } from '@/services/api';
import { useToast } from '@/hooks/use-toast';

interface BookingModalProps {
  isOpen: boolean;
  onClose: () => void;
  room: {
    id: number;
    name: string;
    price: number;
    image: string;
  } | null;
}

const BookingModal = ({ isOpen, onClose, room }: BookingModalProps) => {
  const [checkInDate, setCheckInDate] = useState<Date>();
  const [checkOutDate, setCheckOutDate] = useState<Date>();
  const [guests, setGuests] = useState(1);
  const [customerInfo, setCustomerInfo] = useState({
    fullName: '',
    email: '',
    phone: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  const calculateNights = () => {
    if (!checkInDate || !checkOutDate) return 0;
    const diffTime = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  };

  const calculateTotal = () => {
    if (!room) return 0;
    return room.price * calculateNights();
  };

  const handleBooking = async () => {
    if (!room || !checkInDate || !checkOutDate) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive"
      });
      return;
    }

    if (!customerInfo.fullName || !customerInfo.email || !customerInfo.phone) {
      toast({
        title: "Error",
        description: "Please fill in your contact information",
        variant: "destructive"
      });
      return;
    }

    setIsLoading(true);

    try {
      // Create booking
      const bookingData = {
        roomId: room.id,
        checkInDate: format(checkInDate, 'yyyy-MM-dd'),
        checkOutDate: format(checkOutDate, 'yyyy-MM-dd'),
        guests: guests,
        customerName: customerInfo.fullName,
        customerEmail: customerInfo.email,
        customerPhone: customerInfo.phone,
        totalAmount: calculateTotal()
      };

      const bookingResponse = await bookingAPI.createBooking(bookingData);
      const bookingId = bookingResponse.data.id;

      // Create VNPay payment
      const paymentData = {
        bookingId: bookingId,
        amount: calculateTotal(),
        orderInfo: `Booking ${room.name} from ${format(checkInDate, 'dd/MM/yyyy')} to ${format(checkOutDate, 'dd/MM/yyyy')}`,
        returnUrl: `${window.location.origin}/booking-success`,
        ipAddress: '127.0.0.1'
      };

      const paymentResponse = await vnpayAPI.createPayment(paymentData);
      
      if (paymentResponse.data.paymentUrl) {
        // Redirect to VNPay payment page
        window.location.href = paymentResponse.data.paymentUrl;
      } else {
        throw new Error('Payment URL not received');
      }

    } catch (error: any) {
      console.error('Booking error:', error);
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to create booking",
        variant: "destructive"
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleModalClose = () => {
    onClose();
    setCheckInDate(undefined);
    setCheckOutDate(undefined);
    setGuests(1);
    setCustomerInfo({ fullName: '', email: '', phone: '' });
  };

  if (!room) return null;

  return (
    <Dialog open={isOpen} onOpenChange={handleModalClose}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold">Book {room.name}</DialogTitle>
        </DialogHeader>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-4">
          {/* Room Details */}
          <div className="space-y-4">
            <img
              src={room.image}
              alt={room.name}
              className="w-full h-48 object-cover rounded-lg"
            />
            <div>
              <h3 className="text-lg font-semibold">{room.name}</h3>
              <p className="text-2xl font-bold text-blue-600">${room.price}/night</p>
            </div>
          </div>

          {/* Booking Form */}
          <div className="space-y-4">
            {/* Check-in Date */}
            <div className="space-y-2">
              <Label>Check-in Date</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      "w-full justify-start text-left font-normal",
                      !checkInDate && "text-muted-foreground"
                    )}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {checkInDate ? format(checkInDate, "PPP") : "Select date"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0 bg-white border shadow-lg z-50">
                  <Calendar
                    mode="single"
                    selected={checkInDate}
                    onSelect={setCheckInDate}
                    disabled={(date) => date < new Date()}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>

            {/* Check-out Date */}
            <div className="space-y-2">
              <Label>Check-out Date</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      "w-full justify-start text-left font-normal",
                      !checkOutDate && "text-muted-foreground"
                    )}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {checkOutDate ? format(checkOutDate, "PPP") : "Select date"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0 bg-white border shadow-lg z-50">
                  <Calendar
                    mode="single"
                    selected={checkOutDate}
                    onSelect={setCheckOutDate}
                    disabled={(date) => date <= (checkInDate || new Date())}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>

            {/* Guests */}
            <div className="space-y-2">
              <Label htmlFor="guests">Guests</Label>
              <div className="relative">
                <Users className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                <Input
                  id="guests"
                  type="number"
                  min="1"
                  max="6"
                  value={guests}
                  onChange={(e) => setGuests(Number(e.target.value))}
                  className="pl-10"
                />
              </div>
            </div>

            {/* Customer Information */}
            <div className="space-y-2">
              <Label htmlFor="fullName">Full Name</Label>
              <Input
                id="fullName"
                value={customerInfo.fullName}
                onChange={(e) => setCustomerInfo(prev => ({ ...prev, fullName: e.target.value }))}
                placeholder="Enter your full name"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={customerInfo.email}
                onChange={(e) => setCustomerInfo(prev => ({ ...prev, email: e.target.value }))}
                placeholder="Enter your email"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">Phone Number</Label>
              <Input
                id="phone"
                type="tel"
                value={customerInfo.phone}
                onChange={(e) => setCustomerInfo(prev => ({ ...prev, phone: e.target.value }))}
                placeholder="Enter your phone number"
                required
              />
            </div>
          </div>
        </div>

        {/* Booking Summary */}
        {checkInDate && checkOutDate && (
          <div className="bg-gray-50 p-4 rounded-lg mt-6">
            <h4 className="font-semibold mb-2">Booking Summary</h4>
            <div className="space-y-1 text-sm">
              <div className="flex justify-between">
                <span>Check-in:</span>
                <span>{format(checkInDate, 'dd/MM/yyyy')}</span>
              </div>
              <div className="flex justify-between">
                <span>Check-out:</span>
                <span>{format(checkOutDate, 'dd/MM/yyyy')}</span>
              </div>
              <div className="flex justify-between">
                <span>Nights:</span>
                <span>{calculateNights()}</span>
              </div>
              <div className="flex justify-between">
                <span>Guests:</span>
                <span>{guests}</span>
              </div>
              <div className="flex justify-between font-semibold text-lg border-t pt-2">
                <span>Total:</span>
                <span>${calculateTotal()}</span>
              </div>
            </div>
          </div>
        )}

        {/* Book Button */}
        <Button
          onClick={handleBooking}
          disabled={isLoading || !checkInDate || !checkOutDate}
          className="w-full bg-blue-600 hover:bg-blue-700 text-white mt-4"
        >
          {isLoading ? (
            'Processing...'
          ) : (
            <>
              <CreditCard className="mr-2 h-4 w-4" />
              Book Now & Pay with VNPay
            </>
          )}
        </Button>
      </DialogContent>
    </Dialog>
  );
};

export default BookingModal;
