import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { format } from 'date-fns';
import { Badge } from '@/components/ui/badge';
import { formatCurrency } from '@/lib/utils';

interface Room {
  id: number;
  roomNumber: string;
  roomType: string;
}

interface Booking {
  id: number;
  guestFullName: string;
  room: Room | null;
  roomId: number;
  checkInDate: string;
  checkOutDate: string;
  status: string;
  totalPrice: number;
}

const BookingManagement = () => {
  const { data: bookings = [], isLoading, error } = useQuery({
    queryKey: ['bookings'],
    queryFn: () => api.get<Booking[]>('/bookings').then(res => res.data),
  });

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'confirmed':
        return 'bg-green-100 text-green-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status.toLowerCase()) {
      case 'confirmed':
        return 'Đã xác nhận';
      case 'pending':
        return 'Chờ xác nhận';
      case 'cancelled':
        return 'Đã hủy';
      default:
        return status;
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-red-500">Có lỗi xảy ra khi tải danh sách đặt phòng</p>
        <Button onClick={() => window.location.reload()} className="mt-4">
          Thử lại
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Quản lý đặt phòng</h2>
        <p className="text-gray-600 mt-2">Tổng số đặt phòng: {bookings.length}</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Danh sách đặt phòng</CardTitle>
          <CardDescription>
            Cập nhật mới nhất: {new Date().toLocaleDateString('vi-VN')}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Tên khách</TableHead>
                <TableHead>Phòng</TableHead>
                <TableHead>Ngày nhận</TableHead>
                <TableHead>Ngày trả</TableHead>
                <TableHead>Tổng tiền</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead>Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {bookings.map((booking) => (
                <TableRow key={booking.id}>
                  <TableCell>{booking.guestFullName}</TableCell>
                  <TableCell>
                    {booking.room ? (
                      `${booking.room.roomNumber} - ${booking.room.roomType}`
                    ) : (
                      `Phòng #${booking.roomId}`
                    )}
                  </TableCell>
                  <TableCell>{format(new Date(booking.checkInDate), 'dd/MM/yyyy')}</TableCell>
                  <TableCell>{format(new Date(booking.checkOutDate), 'dd/MM/yyyy')}</TableCell>
                  <TableCell>{formatCurrency(booking.totalPrice)}</TableCell>
                  <TableCell>
                    <Badge className={getStatusColor(booking.status)}>
                      {getStatusText(booking.status)}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Button variant="outline" size="sm">
                      Chi tiết
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default BookingManagement;
