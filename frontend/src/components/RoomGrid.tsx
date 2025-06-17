import { useState } from 'react';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Star, Wifi, Car, Coffee, Waves } from 'lucide-react';
import BookingModal from './BookingModal';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { Room } from '@/types/admin';
import { toast } from 'sonner';

const RoomGrid = () => {
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState<Room | null>(null);

  const { data: rooms = [], isLoading, error } = useQuery({
    queryKey: ['rooms'],
    queryFn: () => api.get<Room[]>('/rooms').then(res => res.data),
    retry: 1,
    staleTime: 5 * 60 * 1000,
  });

  const handleBookNow = (room: Room) => {
    const token = localStorage.getItem('token');
    if (!token) {
      toast.error('Vui lòng đăng nhập để đặt phòng');
      window.location.href = '/login';
      return;
    }
    setSelectedRoom(room);
    setIsBookingModalOpen(true);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="text-center">
          <p className="text-red-500 mb-4">Không thể tải danh sách phòng</p>
          <Button onClick={() => window.location.reload()}>Thử lại</Button>
        </div>
      </div>
    );
  }

  return (
    <section id="rooms" className="py-16 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Our Premium Rooms
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Choose from our collection of carefully designed rooms and suites
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {rooms.map((room) => (
            <Card key={room.id} className="overflow-hidden hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
              <div className="relative">
                {room.images && room.images.length > 0 ? (
                  <img
                    src={`${import.meta.env.VITE_API_URL || "http://localhost:8081"}${room.images[0]}`}
                    alt={room.roomType}
                    className="w-full h-48 object-cover"
                  />
                ) : (
                  <div className="w-full h-48 bg-gray-200 flex items-center justify-center">
                    <span className="text-gray-400">No image available</span>
                  </div>
                )}
                <Badge className="absolute top-3 right-3 bg-blue-500 text-white">
                  {room.available ? 'Available' : 'Booked'}
                </Badge>
              </div>

              <CardContent className="p-4">
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{room.roomType}</h3>
                <p className="text-sm text-gray-600 mb-3">{room.description}</p>

                <div className="space-y-1 mb-4">
                  {room.amenities?.slice(0, 3).map((amenity, index) => (
                    <span key={index} className="text-sm text-gray-600 block">
                      • {amenity}
                    </span>
                  ))}
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-2xl font-bold text-blue-600">${room.price}</span>
                    <span className="text-sm text-gray-500">/night</span>
                  </div>
                </div>
              </CardContent>

              <CardFooter className="p-4 pt-0">
                <Button
                  className="w-full"
                  onClick={() => handleBookNow(room)}
                  disabled={!room.available}
                >
                  {room.available ? 'Book Now' : 'Not Available'}
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      </div>

      <BookingModal
        isOpen={isBookingModalOpen}
        onClose={() => {
          setIsBookingModalOpen(false);
          setSelectedRoom(null);
        }}
        room={selectedRoom}
      />
    </section>
  );
};

export default RoomGrid;
