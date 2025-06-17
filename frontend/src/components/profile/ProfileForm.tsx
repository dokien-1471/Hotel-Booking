import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import { Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const profileFormSchema = z.object({
  firstName: z.string().min(2, "Tên phải có ít nhất 2 ký tự"),
  lastName: z.string().min(2, "Họ phải có ít nhất 2 ký tự"),
  email: z.string().email("Email không hợp lệ"),
  phone: z.string().min(10, "Số điện thoại phải có ít nhất 10 ký tự"),
  address: z.string().optional(),
});

type ProfileFormValues = z.infer<typeof profileFormSchema>;

interface ProfileFormProps {
  user: any;
}

export function ProfileForm({ user }: ProfileFormProps) {
  const [isEditing, setIsEditing] = useState(false);
  const queryClient = useQueryClient();

  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileFormSchema),
    defaultValues: {
      firstName: user?.firstName || "",
      lastName: user?.lastName || "",
      email: user?.email || "",
      phone: user?.phone || "",
      address: user?.address || "",
    },
  });

  const updateProfileMutation = useMutation({
    mutationFn: async (data: ProfileFormValues) => {
      try {
        const response = await api.put(`/api/users/${user.id}`, data);
        return response.data;
      } catch (error: any) {
        if (error.response?.status === 403) {
          throw new Error("Bạn không có quyền cập nhật thông tin này");
        }
        throw error;
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user", user.id] });
      toast.success("Cập nhật thông tin thành công");
      setIsEditing(false);
    },
    onError: (error: any) => {
      toast.error(error.message || "Có lỗi xảy ra khi cập nhật thông tin");
    },
  });

  function onSubmit(data: ProfileFormValues) {
    updateProfileMutation.mutate(data);
  }

  const handleCancel = () => {
    form.reset({
      firstName: user?.firstName || "",
      lastName: user?.lastName || "",
      email: user?.email || "",
      phone: user?.phone || "",
      address: user?.address || "",
    });
    setIsEditing(false);
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-xl font-semibold">Thông tin cá nhân</CardTitle>
        {!isEditing && (
          <Button variant="outline" onClick={() => setIsEditing(true)}>
            Chỉnh sửa
          </Button>
        )}
      </CardHeader>
      <CardContent className="pt-4">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-y-4 gap-x-6">
              <div className="space-y-2">
                {isEditing ? (
                  <FormField
                    control={form.control}
                    name="firstName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Tên</FormLabel>
                        <FormControl>
                          <Input {...field} disabled={updateProfileMutation.isPending} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                ) : (
                  <>
                    <FormLabel>Tên</FormLabel>
                    <p className="text-gray-700 font-medium">{user.firstName || "Chưa cập nhật"}</p>
                  </>
                )}
              </div>

              <div className="space-y-2">
                {isEditing ? (
                  <FormField
                    control={form.control}
                    name="lastName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Họ</FormLabel>
                        <FormControl>
                          <Input {...field} disabled={updateProfileMutation.isPending} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                ) : (
                  <>
                    <FormLabel>Họ</FormLabel>
                    <p className="text-gray-700 font-medium">{user.lastName || "Chưa cập nhật"}</p>
                  </>
                )}
              </div>

              <div className="space-y-2">
                {isEditing ? (
                  <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Email</FormLabel>
                        <FormControl>
                          <Input {...field} type="email" disabled={updateProfileMutation.isPending} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                ) : (
                  <>
                    <FormLabel>Email</FormLabel>
                    <p className="text-gray-700 font-medium">{user.email || "Chưa cập nhật"}</p>
                  </>
                )}
              </div>

              <div className="space-y-2">
                {isEditing ? (
                  <FormField
                    control={form.control}
                    name="phone"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Số điện thoại</FormLabel>
                        <FormControl>
                          <Input {...field} disabled={updateProfileMutation.isPending} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                ) : (
                  <>
                    <FormLabel>Số điện thoại</FormLabel>
                    <p className="text-gray-700 font-medium">{user.phone || "Chưa cập nhật"}</p>
                  </>
                )}
              </div>

              <div className="space-y-2 col-span-1 md:col-span-2">
                {isEditing ? (
                  <FormField
                    control={form.control}
                    name="address"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Địa chỉ</FormLabel>
                        <FormControl>
                          <Input {...field} disabled={updateProfileMutation.isPending} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                ) : (
                  <>
                    <FormLabel>Địa chỉ</FormLabel>
                    <p className="text-gray-700 font-medium">{user.address || "Chưa cập nhật"}</p>
                  </>
                )}
              </div>
            </div>

            {isEditing && (
              <div className="flex justify-end space-x-2 pt-4 border-t mt-6">
                <Button type="button" variant="outline" onClick={handleCancel} disabled={updateProfileMutation.isPending}>
                  Hủy
                </Button>
                <Button
                  type="submit"
                  disabled={updateProfileMutation.isPending}
                >
                  {updateProfileMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Đang cập nhật...
                    </>
                  ) : (
                    "Lưu thay đổi"
                  )}
                </Button>
              </div>
            )}
          </form>
        </Form>
      </CardContent>
    </Card>
  );
} 