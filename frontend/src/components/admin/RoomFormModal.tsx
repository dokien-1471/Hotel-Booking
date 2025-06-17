import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { adminRoomAPI } from '@/services/adminAPI';
import { Room, CreateRoomData } from '@/types/admin';
import { useToast } from '@/hooks/use-toast';
import { X } from 'lucide-react';

interface RoomFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  room?: Room | null;
}

const RoomFormModal = ({ isOpen, onClose, room }: RoomFormModalProps) => {
  const [formData, setFormData] = useState<CreateRoomData>({
    roomNumber: '',
    roomType: '',
    price: 0,
    description: '',
    amenities: [],
    images: [],
    available: true,
  });

  const [amenityInput, setAmenityInput] = useState('');
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const queryClient = useQueryClient();
  const { toast } = useToast();

  useEffect(() => {
    if (room) {
      setFormData({
        roomNumber: room.roomNumber,
        roomType: room.roomType,
        price: room.price,
        description: room.description,
        amenities: room.amenities,
        images: room.images,
        available: room.available,
      });
    } else {
      setFormData({
        roomNumber: '',
        roomType: '',
        price: 0,
        description: '',
        amenities: [],
        images: [],
        available: true,
      });
    }
  }, [room]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.roomNumber.trim()) {
      newErrors.roomNumber = 'Vui lòng nhập số phòng';
    }

    if (!formData.roomType.trim()) {
      newErrors.roomType = 'Vui lòng chọn loại phòng';
    }

    if (formData.price <= 0) {
      newErrors.price = 'Giá phòng phải lớn hơn 0';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const createRoomMutation = useMutation({
    mutationFn: (data: CreateRoomData) => adminRoomAPI.createRoom(data),
    onSuccess: (newRoom) => {
      queryClient.invalidateQueries({ queryKey: ['admin-rooms'] });
      toast({
        title: 'Success',
        description: 'Room created successfully',
      });
      if (selectedFiles.length > 0 && newRoom.data.id) {
        selectedFiles.forEach(file => {
          addRoomImageMutation.mutate({ roomId: newRoom.data.id, file });
        });
      }
      onClose();
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create room',
        variant: 'destructive',
      });
    },
  });

  const updateRoomMutation = useMutation({
    mutationFn: (data: CreateRoomData) => adminRoomAPI.updateRoom(room!.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-rooms'] });
      toast({
        title: 'Success',
        description: 'Room updated successfully',
      });
      if (selectedFiles.length > 0 && room?.id) {
        selectedFiles.forEach(file => {
          addRoomImageMutation.mutate({ roomId: room.id, file });
        });
      }
      onClose();
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to update room',
        variant: 'destructive',
      });
    },
  });

  const addRoomImageMutation = useMutation({
    mutationFn: ({ roomId, file }: { roomId: number, file: File }) =>
      adminRoomAPI.addRoomImage(roomId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-rooms'] });
      toast({
        title: 'Success',
        description: 'Image uploaded successfully',
      });
      setSelectedFiles([]);
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to upload image',
        variant: 'destructive',
      });
    },
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {

      let response;
      if (room) {
        response = await updateRoomMutation.mutateAsync(formData);
      } else {
        response = await createRoomMutation.mutateAsync(formData);
      }

      if (selectedFiles.length > 0 && response.data?.id) {
        const uploadPromises = selectedFiles.map(file =>
          addRoomImageMutation.mutateAsync({
            roomId: response.data.id,
            file
          })
        );
        await Promise.all(uploadPromises);
      }

      onClose();
      toast({
        title: 'Success',
        description: `Room ${room ? 'updated' : 'created'} successfully`,
      });
    } catch (error) {
      console.error('Error:', error);
      toast({
        title: 'Error',
        description: error.message || `Failed to ${room ? 'update' : 'create'} room`,
        variant: 'destructive',
      });
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const filesArray = Array.from(e.target.files);
      setSelectedFiles(prevFiles => [...prevFiles, ...filesArray]);
    }
  };

  const addAmenity = () => {
    if (amenityInput.trim() && !formData.amenities.includes(amenityInput.trim())) {
      setFormData(prev => ({
        ...prev,
        amenities: [...prev.amenities, amenityInput.trim()]
      }));
      setAmenityInput('');
    }
  };

  const removeAmenity = (amenity: string) => {
    setFormData(prev => ({
      ...prev,
      amenities: prev.amenities.filter(a => a !== amenity)
    }));
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{room ? 'Edit Room' : 'Add New Room'}</DialogTitle>
          <DialogDescription>
            {room ? 'Edit the room information below.' : 'Fill in the information below to create a new room.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="roomNumber">Room Number</Label>
              <Input
                id="roomNumber"
                value={formData.roomNumber}
                onChange={(e) => setFormData(prev => ({ ...prev, roomNumber: e.target.value }))}
                className={errors.roomNumber ? 'border-red-500' : ''}
              />
              {errors.roomNumber && (
                <p className="text-red-500 text-sm mt-1">{errors.roomNumber}</p>
              )}
            </div>
            <div>
              <Label htmlFor="roomType">Room Type</Label>
              <Input
                id="roomType"
                value={formData.roomType}
                onChange={(e) => setFormData(prev => ({ ...prev, roomType: e.target.value }))}
                className={errors.roomType ? 'border-red-500' : ''}
              />
              {errors.roomType && (
                <p className="text-red-500 text-sm mt-1">{errors.roomType}</p>
              )}
            </div>
          </div>

          <div>
            <Label htmlFor="price">Price per Night (VNĐ)</Label>
            <Input
              id="price"
              type="number"
              step="1"
              value={formData.price}
              onChange={(e) => setFormData(prev => ({ ...prev, price: parseFloat(e.target.value) || 0 }))}
              className={errors.price ? 'border-red-500' : ''}
            />
            {errors.price && (
              <p className="text-red-500 text-sm mt-1">{errors.price}</p>
            )}
          </div>

          <div>
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              rows={3}
            />
          </div>

          <div>
            <Label>Amenities</Label>
            <div className="flex gap-2 mb-2">
              <Input
                value={amenityInput}
                onChange={(e) => setAmenityInput(e.target.value)}
                placeholder="Add amenity"
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addAmenity())}
              />
              <Button type="button" onClick={addAmenity}>Add</Button>
            </div>
            <div className="flex flex-wrap gap-2">
              {formData.amenities.map((amenity, index) => (
                <span
                  key={index}
                  className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm flex items-center gap-1"
                >
                  {amenity}
                  <button
                    type="button"
                    onClick={() => removeAmenity(amenity)}
                    className="text-blue-600 hover:text-blue-800"
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          </div>

          <div>
            <Label>Room Images</Label>
            <div className="flex gap-2 mb-2">
              <Input
                type="file"
                onChange={handleFileChange}
                multiple
                accept="image/*"
              />
            </div>
            {selectedFiles.length > 0 && (
              <div className="grid grid-cols-3 gap-2 mt-2">
                {selectedFiles.map((file, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={URL.createObjectURL(file)}
                      alt={`Selected Image ${index + 1}`}
                      className="w-full h-24 object-cover rounded-md"
                    />
                    <button
                      type="button"
                      onClick={() => setSelectedFiles(files => files.filter((_, i) => i !== index))}
                      className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}
            {formData.images.length > 0 && (
              <div className="grid grid-cols-3 gap-2 mt-2">
                {formData.images.map((imageUrl, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={`${import.meta.env.VITE_API_URL || "http://localhost:8081"}${imageUrl}`}
                      alt={`Room Image ${index + 1}`}
                      className="w-full h-24 object-cover rounded-md"
                    />
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="flex items-center space-x-2">
            <Switch
              id="available"
              checked={formData.available}
              onCheckedChange={(checked) => setFormData(prev => ({ ...prev, available: checked }))}
            />
            <Label htmlFor="available">Room Available</Label>
          </div>

          <div className="flex justify-end gap-2 pt-4">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={createRoomMutation.isPending || updateRoomMutation.isPending}
            >
              {createRoomMutation.isPending || updateRoomMutation.isPending
                ? 'Saving...'
                : room ? 'Update Room' : 'Create Room'
              }
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default RoomFormModal;
