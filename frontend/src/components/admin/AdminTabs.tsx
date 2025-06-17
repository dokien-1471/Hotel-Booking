
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Users, Hotel, Calendar, BarChart3 } from 'lucide-react';
import AdminDashboard from './AdminDashboard';
import RoomManagement from './RoomManagement';
import UserManagement from './UserManagement';
import BookingManagement from './BookingManagement';

const AdminTabs = () => {
  return (
    <Tabs defaultValue="dashboard" className="w-full">
      <TabsList className="grid w-full grid-cols-4">
        <TabsTrigger value="dashboard" className="flex items-center gap-2">
          <BarChart3 className="h-4 w-4" />
          Dashboard
        </TabsTrigger>
        <TabsTrigger value="rooms" className="flex items-center gap-2">
          <Hotel className="h-4 w-4" />
          Rooms
        </TabsTrigger>
        <TabsTrigger value="users" className="flex items-center gap-2">
          <Users className="h-4 w-4" />
          Users
        </TabsTrigger>
        <TabsTrigger value="bookings" className="flex items-center gap-2">
          <Calendar className="h-4 w-4" />
          Bookings
        </TabsTrigger>
      </TabsList>

      <TabsContent value="dashboard" className="mt-6">
        <AdminDashboard />
      </TabsContent>

      <TabsContent value="rooms" className="mt-6">
        <RoomManagement />
      </TabsContent>

      <TabsContent value="users" className="mt-6">
        <UserManagement />
      </TabsContent>

      <TabsContent value="bookings" className="mt-6">
        <BookingManagement />
      </TabsContent>
    </Tabs>
  );
};

export default AdminTabs;
