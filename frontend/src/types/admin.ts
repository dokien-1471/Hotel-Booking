import { z } from "zod";

export interface Room {
  id: number;
  roomNumber: string;
  roomType: string;
  price: number;
  available: boolean;
  description: string;
  images: string[];
  amenities: string[];
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: string;
}

export const roomSchema = z.object({
  roomNumber: z.string().min(1, "Room number is required"),
  roomType: z.string().min(1, "Room type is required"),
  price: z.number().min(0, "Price must be greater than 0"),
  description: z.string().optional(),
  amenities: z.array(z.string()).default([]),
  available: z.boolean().default(true),
  images: z.array(z.string()).default([]),
});

export type CreateRoomData = z.infer<typeof roomSchema>;

export const userSchema = z.object({
  firstName: z.string().min(2, "First name must be at least 2 characters"),
  lastName: z.string().min(2, "Last name must be at least 2 characters"),
  email: z.string().email("Invalid email format"),
  password: z.string().min(6, "Password must be at least 6 characters"),
  phoneNumber: z.string().regex(/^\+?[0-9]{10,15}$/, "Invalid phone number format"),
  role: z.string(),
});

export type CreateUserData = z.infer<typeof userSchema>;
export type UpdateUserData = Partial<CreateUserData>; 