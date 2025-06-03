
import { useState, useEffect } from 'react';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Star, Wifi, Car, Coffee, Waves } from 'lucide-react';
import BookingModal from './BookingModal';

// Mock room data - replace with API call
const mockRooms = [
  {
    id: 1,
    name: "Deluxe Ocean View",
    price: 299,
    image: "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=500&h=300&fit=crop",
    rating: 4.8,
    amenities: ["Ocean View", "King Bed", "Free WiFi", "Mini Bar"],
    features: [Wifi, Car, Coffee, Waves]
  },
  {
    id: 2,
    name: "Premium Suite",
    price: 399,
    image: "https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=500&h=300&fit=crop",
    rating: 4.9,
    amenities: ["City View", "Living Area", "Kitchenette", "Balcony"],
    features: [Wifi, Car, Coffee]
  },
  {
    id: 3,
    name: "Standard Room",
    price: 199,
    image: "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=500&h=300&fit=crop",
    rating: 4.6,
    amenities: ["Queen Bed", "Free WiFi", "Air Conditioning"],
    features: [Wifi, Coffee]
  },
  {
    id: 4,
    name: "Executive Suite",
    price: 549,
    image: "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500&h=300&fit=crop",
    rating: 5.0,
    amenities: ["Corner Suite", "Business Center", "Concierge", "Spa Access"],
    features: [Wifi, Car, Coffee, Waves]
  }
];

const RoomGrid = () => {
  const [rooms, setRooms] = useState(mockRooms);
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState<typeof mockRooms[0] | null>(null);

  const handleBookNow = (room: typeof mockRooms[0]) => {
    setSelectedRoom(room);
    setIsBookingModalOpen(true);
  };

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
                <img
                  src={room.image}
                  alt={room.name}
                  className="w-full h-48 object-cover"
                />
                <Badge className="absolute top-3 right-3 bg-yellow-500 text-white">
                  <Star className="h-3 w-3 mr-1 fill-current" />
                  {room.rating}
                </Badge>
              </div>
              
              <CardContent className="p-4">
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{room.name}</h3>
                
                <div className="flex items-center space-x-4 mb-3">
                  {room.features.slice(0, 3).map((Icon, index) => (
                    <Icon key={index} className="h-4 w-4 text-gray-500" />
                  ))}
                </div>

                <div className="space-y-1 mb-4">
                  {room.amenities.slice(0, 2).map((amenity, index) => (
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
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white"
                  onClick={() => handleBookNow(room)}
                >
                  Book Now
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      </div>

      <BookingModal
        isOpen={isBookingModalOpen}
        onClose={() => setIsBookingModalOpen(false)}
        room={selectedRoom}
      />
    </section>
  );
};

export default RoomGrid;
