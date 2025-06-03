
import { Hotel, Mail, Phone, MapPin } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-white py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center space-x-2 mb-4">
              <Hotel className="h-8 w-8 text-blue-400" />
              <span className="text-2xl font-bold">Haven</span>
            </div>
            <p className="text-gray-300 mb-4 max-w-md">
              Experience luxury and comfort at Haven Hotel. We provide exceptional service 
              and unforgettable stays for our valued guests.
            </p>
          </div>

          <div>
            <h3 className="text-lg font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2 text-gray-300">
              <li><a href="#rooms" className="hover:text-blue-400 transition-colors">Rooms</a></li>
              <li><a href="#about" className="hover:text-blue-400 transition-colors">About Us</a></li>
              <li><a href="#contact" className="hover:text-blue-400 transition-colors">Contact</a></li>
              <li><a href="#booking" className="hover:text-blue-400 transition-colors">Booking</a></li>
            </ul>
          </div>

          <div>
            <h3 className="text-lg font-semibold mb-4">Contact Info</h3>
            <div className="space-y-2 text-gray-300">
              <div className="flex items-center space-x-2">
                <Phone className="h-4 w-4" />
                <span>+1 (555) 123-4567</span>
              </div>
              <div className="flex items-center space-x-2">
                <Mail className="h-4 w-4" />
                <span>info@havenhotel.com</span>
              </div>
              <div className="flex items-center space-x-2">
                <MapPin className="h-4 w-4" />
                <span>123 Paradise Ave, City</span>
              </div>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; 2024 Haven Hotel. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
