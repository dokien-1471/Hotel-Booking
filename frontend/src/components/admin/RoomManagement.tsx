import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Search, Edit, Trash2 } from 'lucide-react';
import { adminRoomAPI } from '@/services/adminAPI';
import RoomFormModal from './RoomFormModal';
import ConfirmDeleteDialog from './ConfirmDeleteDialog';
import { Room } from '@/types/admin';
import { useToast } from '@/hooks/use-toast';

const RoomManagement = () => {
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [deletingRoom, setDeletingRoom] = useState<Room | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const { data: rooms = [], isLoading } = useQuery({
    queryKey: ['admin-rooms'],
    queryFn: () => adminRoomAPI.getAllRooms().then(res => res.data),
  });

  const deleteRoomMutation = useMutation({
    mutationFn: (id: number) => adminRoomAPI.deleteRoom(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-rooms'] });
      toast({
        title: 'Success',
        description: 'Room deleted successfully',
      });
      setDeletingRoom(null);
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to delete room',
        variant: 'destructive',
      });
      setDeletingRoom(null);
    },
  });

  const handleEdit = (room: Room) => {
    setEditingRoom(room);
    setIsFormModalOpen(true);
  };

  const handleDelete = (room: Room) => {
    setDeletingRoom(room);
  };

  const confirmDelete = () => {
    if (deletingRoom) {
      deleteRoomMutation.mutate(deletingRoom.id);
    }
  };

  const filteredRooms = rooms.filter(room =>
    room.roomNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
    room.roomType.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Room Management</h2>
          <p className="text-gray-600 mt-2">Manage your hotel rooms and availability</p>
        </div>
        <Button onClick={() => setIsFormModalOpen(true)} className="flex items-center gap-2">
          <Plus className="h-4 w-4" />
          Add Room
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Rooms</CardTitle>
              <CardDescription>
                Total rooms: {rooms.length}
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <input
                  type="text"
                  placeholder="Search rooms..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Room Number</TableHead>
                  <TableHead>Room Type</TableHead>
                  <TableHead>Price</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Amenities</TableHead>
                  <TableHead>Images</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredRooms.map((room) => (
                  <TableRow key={room.id}>
                    <TableCell className="font-medium">{room.roomNumber}</TableCell>
                    <TableCell>{room.roomType}</TableCell>
                    <TableCell>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(room.price)}</TableCell>
                    <TableCell>
                      <span className={`px-2 py-1 rounded-full text-xs ${room.available ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                        {room.available ? 'Available' : 'Unavailable'}
                      </span>
                    </TableCell>
                    <TableCell className="max-w-xs truncate" title={room.description}>
                      {room.description || 'No description'}
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {room.amenities?.map((amenity, index) => (
                          <span key={index} className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">
                            {amenity}
                          </span>
                        )) || 'No amenities'}
                      </div>
                    </TableCell>
                    <TableCell>
                      {room.images?.length > 0 ? (
                        <div className="flex -space-x-2">
                          {room.images.slice(0, 3).map((image, index) => (
                            <img
                              key={index}
                              src={`${import.meta.env.VITE_API_URL || "http://localhost:8081"}${image}`}
                              alt={`Room ${room.roomNumber} image ${index + 1}`}
                              className="w-8 h-8 rounded-full border-2 border-white object-cover"
                            />
                          ))}
                          {room.images.length > 3 && (
                            <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-xs">
                              +{room.images.length - 3}
                            </div>
                          )}
                        </div>
                      ) : (
                        'No images'
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEdit(room)}
                          className="h-8 w-8 p-0"
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDelete(room)}
                          className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <RoomFormModal
        isOpen={isFormModalOpen}
        onClose={() => {
          setIsFormModalOpen(false);
          setEditingRoom(null);
        }}
        room={editingRoom}
      />

      <ConfirmDeleteDialog
        isOpen={!!deletingRoom}
        onClose={() => setDeletingRoom(null)}
        onConfirm={confirmDelete}
        title="Delete Room"
        description={`Are you sure you want to delete room "${deletingRoom?.roomNumber}"? This action cannot be undone.`}
        isLoading={deleteRoomMutation.isPending}
      />
    </div>
  );
};

export default RoomManagement;
