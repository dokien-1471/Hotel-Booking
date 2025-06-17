import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/providers/AuthProvider";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import { ProfileForm } from "@/components/profile/ProfileForm";
import { BookingHistory } from "@/components/profile/BookingHistory";
import { api } from "@/lib/api";
import { Loader2, User, Calendar, CreditCard, Mail, Phone, MapPin } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircle } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import ProfileNavbar from "@/components/ProfileNavbar";

interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address?: string;
  role: string;
}

interface Booking {
  id: number;
  bookingReference: string;
  checkIn: string;
  checkOut: string;
  status: "PENDING" | "CONFIRMED" | "CANCELLED" | "COMPLETED";
  totalPrice: number;
  room: {
    id: number;
    name: string;
    type: string;
  };
}

export default function Profile() {
  const { user, loading: isLoadingAuth } = useAuth();
  const [activeTab, setActiveTab] = useState("profile");

  console.log("Profile page - user from useAuth:", user);
  console.log("Profile page - isLoadingAuth:", isLoadingAuth);

  const {
    data: userData,
    isLoading: isLoadingUser,
    error: userError,
    refetch: refetchUser,
  } = useQuery<User>({
    queryKey: ["user", user?.id],
    queryFn: async () => {
      console.log("Fetching user data for ID:", user?.id);
      try {
        const response = await api.get(`/users/${user?.id}`);
        console.log("User data fetched successfully:", response.data);
        return response.data;
      } catch (error: any) {
        console.error("Error fetching user data:", error);
        if (error.response?.status === 403) {
          throw new Error("Bạn không có quyền xem thông tin này");
        }
        throw error;
      }
    },
    enabled: !!user?.id,
    retry: 1,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  console.log("Profile page - userData:", userData);
  console.log("Profile page - isLoadingUser:", isLoadingUser);
  console.log("Profile page - userError:", userError);

  const {
    data: bookings,
    isLoading: isLoadingBookings,
    error: bookingsError,
    refetch: refetchBookings,
  } = useQuery<Booking[]>({
    queryKey: ["bookings", user?.id],
    queryFn: async () => {
      console.log("Fetching bookings for user ID:", user?.id);
      try {
        const response = await api.get(`/api/bookings/user/${user?.id}`);
        // Transform the data to match the expected format
        const transformedBookings = response.data.map((booking: any) => ({
          id: booking.id,
          bookingReference: booking.bookingReference,
          checkIn: booking.checkInDate,
          checkOut: booking.checkOutDate,
          status: booking.status,
          totalPrice: booking.totalPrice,
          room: booking.room ? {
            id: booking.room.id,
            name: booking.room.name,
            type: booking.room.type
          } : {
            id: booking.roomId,
            name: 'Phòng không xác định',
            type: 'Loại phòng không xác định'
          }
        }));
        console.log("Transformed bookings:", transformedBookings);
        return transformedBookings;
      } catch (error: any) {
        console.error("Error fetching bookings:", error);
        if (error.response?.status === 403) {
          throw new Error("Bạn không có quyền xem lịch sử đặt phòng");
        }
        throw error;
      }
    },
    enabled: !!user?.id && activeTab === "bookings",
    retry: 1,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  console.log("Profile page - bookings:", bookings);
  console.log("Profile page - isLoadingBookings:", isLoadingBookings);
  console.log("Profile page - bookingsError:", bookingsError);

  if (isLoadingAuth) {
    return (
      <>
        <ProfileNavbar />
        <div className="flex items-center justify-center min-h-[calc(100vh-64px)]">
          <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
        </div>
      </>
    );
  }

  if (!user) {
    return (
      <>
        <ProfileNavbar />
        <div className="container mx-auto py-10">
          <Card className="max-w-2xl mx-auto">
            <CardHeader>
              <CardTitle className="text-2xl text-center">Vui lòng đăng nhập</CardTitle>
              <CardDescription className="text-center">
                Bạn cần đăng nhập để xem thông tin cá nhân
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </>
    );
  }

  const renderError = (error: Error, refetch: () => void) => (
    <Alert variant="destructive" className="mb-4">
      <AlertCircle className="h-4 w-4" />
      <AlertTitle>Lỗi</AlertTitle>
      <AlertDescription className="flex justify-between items-center">
        <span>{error.message}</span>
        <Button variant="outline" size="sm" onClick={() => refetch()}>
          Thử lại
        </Button>
      </AlertDescription>
    </Alert>
  );

  const getInitials = (firstName: string, lastName: string) => {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  };

  return (
    <>
      <ProfileNavbar />
      <div className="container mx-auto py-10 px-4">
        <div className="max-w-5xl mx-auto space-y-6">
          {/* Profile Header */}
          <Card className="overflow-hidden">
            <div className="h-32 bg-white" />
            <div className="px-6 pb-6">
              <div className="flex flex-col md:flex-row items-center md:items-end -mt-16 space-y-4 md:space-y-0 md:space-x-6">
                <Avatar className="h-32 w-32 border-4 border-white shadow-lg">
                  <AvatarImage src={`https://api.dicebear.com/7.x/initials/svg?seed=${user.firstName} ${user.lastName}`} />
                  <AvatarFallback className="text-4xl bg-blue-100 text-blue-600">
                    {getInitials(user.firstName, user.lastName)}
                  </AvatarFallback>
                </Avatar>
                <div className="text-center md:text-left">
                  <h1 className="text-2xl font-bold text-gray-900">
                    {user.firstName} {user.lastName}
                  </h1>
                  <p className="text-gray-600">{user.email}</p>
                  <div className="flex flex-wrap justify-center md:justify-start gap-4 mt-2">
                    <div className="flex items-center text-sm text-gray-600">
                      <Phone className="h-4 w-4 mr-1" />
                      {user.phone || "Chưa cập nhật"}
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <MapPin className="h-4 w-4 mr-1" />
                      {user.address || "Chưa cập nhật địa chỉ"}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </Card>

          {/* Main Content */}
          <Card>
            <CardHeader className="border-b">
              <CardTitle className="text-xl">Quản lý tài khoản</CardTitle>
              <CardDescription>
                Cập nhật thông tin cá nhân và xem lịch sử đặt phòng
              </CardDescription>
            </CardHeader>
            <CardContent className="p-0">
              <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
                <TabsList className="w-full justify-start border-b rounded-none h-auto p-0">
                  <TabsTrigger
                    value="profile"
                    className="data-[state=active]:border-b-2 data-[state=active]:border-blue-600 rounded-none px-6 py-3"
                  >
                    Thông tin cá nhân
                  </TabsTrigger>
                  <TabsTrigger
                    value="bookings"
                    className="data-[state=active]:border-b-2 data-[state=active]:border-blue-600 rounded-none px-6 py-3"
                  >
                    Lịch sử đặt phòng
                  </TabsTrigger>
                </TabsList>

                <TabsContent value="profile" className="p-6">
                  {userError ? (
                    renderError(userError as Error, refetchUser)
                  ) : isLoadingUser ? (
                    <div className="space-y-4">
                      <Skeleton className="h-12 w-full" />
                      <Skeleton className="h-12 w-full" />
                      <Skeleton className="h-12 w-full" />
                    </div>
                  ) : userData ? (
                    <ProfileForm user={userData} onSuccess={refetchUser} />
                  ) : null}
                </TabsContent>

                <TabsContent value="bookings" className="p-6">
                  {bookingsError ? (
                    renderError(bookingsError as Error, refetchBookings)
                  ) : isLoadingBookings ? (
                    <div className="space-y-4">
                      <Skeleton className="h-32 w-full" />
                      <Skeleton className="h-32 w-full" />
                    </div>
                  ) : bookings ? (
                    <BookingHistory bookings={bookings} />
                  ) : (
                    <Card className="text-center py-8">
                      <p className="text-muted-foreground text-lg mb-4">Không thể tải lịch sử đặt phòng</p>
                      <Button onClick={() => refetchBookings()}>Thử lại</Button>
                    </Card>
                  )}
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
} 