import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Calendar, MapPin, Users } from 'lucide-react';
import { Card } from '@/components/ui/card';

const Hero = () => {
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [guests, setGuests] = useState(2);

  return (
    <div className="relative text-white">
      {/* Background Image */}
      <div
        className="absolute inset-0 bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage: 'url("https://images.unsplash.com/photo-1571896349842-33c89424de2d?q=80&w=1760&auto=format&fit=crop")'
        }}
      ></div>
      {/* Overlay */}
      <div className="absolute inset-0 bg-black/40"></div>
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32">
        <div className="text-center mb-12">
          <h1 className="text-4xl md:text-6xl font-bold mb-6 animate-fade-in">
            Khám phá hành trình của bạn
            <span className="block text-yellow-400">Getaway</span>
          </h1>
          <p className="text-xl md:text-2xl text-blue-100 max-w-3xl mx-auto animate-fade-in">
            Trải nghiệm khách sạn đẳng cấp với chất lượng dịch vụ tuyệt vời. Đặt chỗ ngay hôm nay.
          </p>
        </div>

        {/* Search Card */}
        <Card className="max-w-4xl mx-auto bg-white text-gray-900 p-6 shadow-2xl animate-scale-in">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-700 flex items-center">
                <Calendar className="h-4 w-4 mr-2" />
                Check-in
              </label>
              <input
                type="date"
                value={checkIn}
                onChange={(e) => setCheckIn(e.target.value)}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-700 flex items-center">
                <Calendar className="h-4 w-4 mr-2" />
                Check-out
              </label>
              <input
                type="date"
                value={checkOut}
                onChange={(e) => setCheckOut(e.target.value)}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-700 flex items-center">
                <Users className="h-4 w-4 mr-2" />
                Guests
              </label>
              <select
                value={guests}
                onChange={(e) => setGuests(Number(e.target.value))}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                {[1, 2, 3, 4, 5, 6].map(num => (
                  <option key={num} value={num}>{num} Guest{num > 1 ? 's' : ''}</option>
                ))}
              </select>
            </div>

            <div className="flex items-end">
              <Button className="w-full bg-blue-600 hover:bg-blue-700 text-white p-3 h-12 text-lg font-semibold">
                Search Rooms
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default Hero;
