import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import { toast } from "sonner";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { Loader2, CalendarDays, Wallet, Tag } from "lucide-react";
import { api } from "@/lib/api";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

const statusColors = {
  PENDING: "bg-yellow-500",
  CONFIRMED: "bg-green-500",
  CANCELLED: "bg-red-500",
  COMPLETED: "bg-blue-500",
} as const;

const statusLabels = {
  PENDING: "Chờ xác nhận",
  CONFIRMED: "Đã xác nhận",
  CANCELLED: "Đã hủy",
  COMPLETED: "Hoàn thành",
} as const;

interface Booking {
  id: number;
  bookingReference: string;
  checkIn: string;
  checkOut: string;
  status: keyof typeof statusColors;
  totalPrice: number;
  room: {
    id: number;
    name: string;
    type: string;
  };
}

interface BookingHistoryProps {
  bookings: Booking[];
}

export function BookingHistory({ bookings }: BookingHistoryProps) {
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);
  const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);
  const queryClient = useQueryClient();

  const cancelBookingMutation = useMutation({
    mutationFn: async (bookingId: number) => {
      try {
        const response = await api.post(`/api/bookings/${bookingId}/cancel`);
        return response.data;
      } catch (error: any) {
        if (error.response?.status === 403) {
          throw new Error("Bạn không có quyền hủy đặt phòng này");
        }
        throw error;
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      toast.success("Hủy đặt phòng thành công");
      setSelectedBooking(null);
      setIsCancelDialogOpen(false);
    },
    onError: (error: any) => {
      toast.error(error.message || "Có lỗi xảy ra khi hủy đặt phòng");
    },
  });

  const handleCancelBooking = (booking: Booking) => {
    setSelectedBooking(booking);
    setIsCancelDialogOpen(true);
  };

  const confirmCancelBooking = () => {
    if (selectedBooking) {
      cancelBookingMutation.mutate(selectedBooking.id);
    }
  };

  if (!bookings || bookings.length === 0) {
    return (
      <Card className="text-center py-8">
        <p className="text-muted-foreground text-lg mb-4">Bạn chưa có đơn đặt phòng nào</p>
        <p className="text-muted-foreground text-sm">Hãy bắt đầu hành trình của bạn bằng cách đặt một căn phòng!</p>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {bookings.map((booking) => (
        <Card key={booking.id} className="overflow-hidden border-2 border-transparent hover:border-blue-200 transition-colors duration-200">
          <CardHeader className="bg-gray-50/50 p-4 border-b">
            <div className="flex justify-between items-center">
              <div>
                <CardTitle className="text-xl font-semibold text-blue-700">
                  {booking.room?.name || 'Phòng không xác định'}
                </CardTitle>
                <CardDescription className="text-gray-600">
                  Loại phòng: {booking.room?.type || 'Không xác định'}
                </CardDescription>
              </div>
              <Badge
                className={`${statusColors[booking.status] || 'bg-gray-500'} text-white text-base py-1 px-3 rounded-full`}
              >
                {statusLabels[booking.status] || booking.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent className="p-4 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center space-x-2 text-sm text-gray-700">
              <Tag className="h-4 w-4 text-blue-500" />
              <p>Mã đặt phòng: <span className="font-medium text-gray-900">{booking.bookingReference}</span></p>
            </div>
            <div className="flex items-center space-x-2 text-sm text-gray-700">
              <CalendarDays className="h-4 w-4 text-blue-500" />
              <p>Nhận phòng: <span className="font-medium text-gray-900">
                {format(new Date(booking.checkIn), "PPP", { locale: vi })}
              </span></p>
            </div>
            <div className="flex items-center space-x-2 text-sm text-gray-700">
              <CalendarDays className="h-4 w-4 text-blue-500" />
              <p>Trả phòng: <span className="font-medium text-gray-900">
                {format(new Date(booking.checkOut), "PPP", { locale: vi })}
              </span></p>
            </div>
            <div className="flex items-center space-x-2 text-sm text-gray-700">
              <Wallet className="h-4 w-4 text-blue-500" />
              <p>Tổng tiền: <span className="font-medium text-gray-900">
                {new Intl.NumberFormat("vi-VN", {
                  style: "currency",
                  currency: "VND",
                }).format(booking.totalPrice)}
              </span></p>
            </div>
          </CardContent>
          <CardFooter className="flex justify-end p-4 border-t bg-gray-50/50">
            <Dialog>
              <DialogTrigger asChild>
                <Button variant="outline" className="text-blue-600 hover:text-blue-800">
                  Chi tiết
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-md">
                <DialogHeader>
                  <DialogTitle className="text-2xl font-bold">Chi tiết đặt phòng</DialogTitle>
                  <DialogDescription>
                    Mã đặt phòng: <span className="font-semibold text-gray-700">{booking.bookingReference}</span>
                  </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4 text-sm">
                  <div className="space-y-2">
                    <h4 className="font-bold text-gray-800">Thông tin phòng</h4>
                    <p className="text-gray-700">Tên phòng: <span className="font-medium">{booking.room?.name || 'Không xác định'}</span></p>
                    <p className="text-gray-700">Loại phòng: <span className="font-medium">{booking.room?.type || 'Không xác định'}</span></p>
                  </div>
                  <div className="space-y-2">
                    <h4 className="font-bold text-gray-800">Thông tin đặt phòng</h4>
                    <p className="text-gray-700">
                      <CalendarDays className="inline-block h-4 w-4 mr-2 text-blue-500" />
                      Ngày nhận phòng:{" "}
                      <span className="font-medium">
                        {format(new Date(booking.checkIn), "PPP", { locale: vi })}
                      </span>
                    </p>
                    <p className="text-gray-700">
                      <CalendarDays className="inline-block h-4 w-4 mr-2 text-blue-500" />
                      Ngày trả phòng:{" "}
                      <span className="font-medium">
                        {format(new Date(booking.checkOut), "PPP", { locale: vi })}
                      </span>
                    </p>
                    <p className="text-gray-700">
                      <Wallet className="inline-block h-4 w-4 mr-2 text-blue-500" />
                      Tổng tiền:{" "}
                      <span className="font-medium">
                        {new Intl.NumberFormat("vi-VN", {
                          style: "currency",
                          currency: "VND",
                        }).format(booking.totalPrice)}
                      </span>
                    </p>
                    <p className="text-gray-700">
                      <Tag className="inline-block h-4 w-4 mr-2 text-blue-500" />
                      Trạng thái:{" "}
                      <Badge
                        className={`${statusColors[booking.status] || 'bg-gray-500'} text-white`}
                      >
                        {statusLabels[booking.status] || booking.status}
                      </Badge>
                    </p>
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    variant="outline"
                    onClick={() => setSelectedBooking(null)}
                    className="min-w-[80px]"
                  >
                    Đóng
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
            {booking.status === "PENDING" && (
              <Button
                variant="destructive"
                onClick={() => handleCancelBooking(booking)}
                disabled={cancelBookingMutation.isPending}
                className="ml-2 min-w-[80px]"
              >
                {cancelBookingMutation.isPending && selectedBooking?.id === booking.id ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  "Hủy"
                )}
              </Button>
            )}
          </CardFooter>
        </Card>
      ))}

      <AlertDialog open={isCancelDialogOpen} onOpenChange={setIsCancelDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận hủy đặt phòng</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn hủy đặt phòng này không? Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Không</AlertDialogCancel>
            <AlertDialogAction onClick={confirmCancelBooking}>
              {cancelBookingMutation.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : null}
              Có, hủy đặt phòng
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
} 