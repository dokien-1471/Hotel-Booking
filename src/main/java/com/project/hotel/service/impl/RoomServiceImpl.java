package com.project.hotel.service.impl;

import com.project.hotel.dto.RoomDTO;
import com.project.hotel.entity.Room;
import com.project.hotel.entity.Booking;
import com.project.hotel.constant.BookingStatus;
import com.project.hotel.repository.BookingRepository;
import com.project.hotel.entity.Room;
import com.project.hotel.entity.Booking;
import com.project.hotel.constant.BookingStatus;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.exception.RoomAlreadyExistsException;
import com.project.hotel.exception.ValidationException;
import com.project.hotel.repository.RoomRepository;
import com.project.hotel.repository.BookingRepository;
import com.project.hotel.service.RoomService;
import com.project.hotel.constant.RoomType;
import com.project.hotel.util.FileUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.project.hotel.entity.Booking;
import com.project.hotel.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private static final String UPLOAD_DIR = "uploads/rooms";
    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomDTO roomDTO) {

        if (roomRepository.findByRoomNumber(roomDTO.getRoomNumber()) != null) {
            throw new RoomAlreadyExistsException("Phòng số " + roomDTO.getRoomNumber() + " đã tồn tại");
        }

        validateRoomData(roomDTO);

        Room room = convertToEntity(roomDTO);
        room.setAvailable(true);

        Room savedRoom = roomRepository.save(room);
        return convertToDTO(savedRoom);
    }

    @Override
    public RoomDTO getRoomById(Long id) {
        Room room = findRoomEntityById(id);
        return convertToDTO(room);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getAvailableRooms() {
        return roomRepository.findByIsAvailable(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getRoomsByType(String roomType) {
        return roomRepository.findByRoomType(roomType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getRoomsBySelectedAmenities(List<String> amenities) {
        return roomRepository.findBySelectedAmenities(amenities, amenities.size()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {

        Room room = findRoomEntityById(id);

        if (!room.getRoomNumber().equals(roomDTO.getRoomNumber())) {
            if (roomRepository.findByRoomNumber(roomDTO.getRoomNumber()) != null) {
                throw new RoomAlreadyExistsException("Phòng số " + roomDTO.getRoomNumber() + " đã tồn tại");
            }
        }

        validateRoomData(roomDTO);

        updateRoomFromDTO(room, roomDTO);

        Room updatedRoom = roomRepository.save(room);
        return convertToDTO(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = findRoomEntityById(id);

        // Check for active bookings
        List<Booking> activeBookings = bookingRepository.findByRoomAndStatusNot(room, BookingStatus.CANCELLED);
        if (!activeBookings.isEmpty()) {
            throw new ValidationException("Không thể xóa phòng vì có đơn đặt phòng đang hoạt động");
        }

        // Delete room images if any
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            for (String imageUrl : room.getImages()) {
                try {
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    FileUploadUtil.deleteFile(UPLOAD_DIR + "/" + fileName);
                } catch (IOException e) {
                    // Log warning but continue with room deletion
                    log.warn("Không thể xóa ảnh {}: {}", imageUrl, e.getMessage());
                }
            }
        }

        roomRepository.deleteById(id);
    }

    @Override
    public Room findRoomEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với id: " + id));
    }

    @Override
    @Transactional
    public RoomDTO addRoomImage(Long roomId, MultipartFile file) {
        Room room = findRoomEntityById(roomId);

        try {

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString() + extension;

            String filePath = FileUploadUtil.saveFile("uploads", fileName, file);

            String imageUrl = "/api/uploads/" + fileName;

            // Add image URL to room's image list
            if (room.getImages() == null) {
                room.setImages(new ArrayList<>());
            }
            room.getImages().add(imageUrl);

            // Save updated room
            Room updatedRoom = roomRepository.save(room);
            return convertToDTO(updatedRoom);
        } catch (IOException e) {
            throw new RuntimeException("Could not upload image: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RoomDTO removeRoomImage(Long roomId, String imageUrl) {
        Room room = findRoomEntityById(roomId);

        if (room.getImages().remove(imageUrl)) {
            try {

                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                FileUploadUtil.deleteFile("uploads/" + fileName);

                Room updatedRoom = roomRepository.save(room);
                return convertToDTO(updatedRoom);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete image: " + e.getMessage());
            }
        }
        throw new ResourceNotFoundException("Image not found in room: " + imageUrl);
    }

    @Override
    public List<String> getRoomImages(Long roomId) {
        Room room = findRoomEntityById(roomId);
        return room.getImages();
    }

    // Helper methods
    private void validateRoomData(RoomDTO roomDTO) {
        if (roomDTO.getRoomNumber() == null || roomDTO.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Số phòng không được để trống");
        }
        if (roomDTO.getRoomType() == null) {
            throw new IllegalArgumentException("Loại phòng không được để trống");
        }
        if (roomDTO.getPrice() == null || roomDTO.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá phòng phải lớn hơn 0");
        }
    }

    private RoomDTO convertToDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomNumber(room.getRoomNumber());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setType(room.getRoomType());
        roomDTO.setPrice(room.getPrice());
        roomDTO.setAvailable(room.isAvailable());
        roomDTO.setDescription(room.getDescription());
        roomDTO.setImages(room.getImages());
        roomDTO.setAmenities(room.getAmenities());
        roomDTO.setName("Phòng " + room.getRoomNumber());

        return roomDTO;
    }

    private Room convertToEntity(RoomDTO roomDTO) {
        Room room = new Room();
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPrice(roomDTO.getPrice());
        room.setAvailable(roomDTO.isAvailable());
        room.setDescription(roomDTO.getDescription());

        if (roomDTO.getImages() != null) {
            room.setImages(roomDTO.getImages());
        }
        if (roomDTO.getAmenities() != null) {
            room.setAmenities(roomDTO.getAmenities());
        }
        return room;
    }

    private void updateRoomFromDTO(Room room, RoomDTO roomDTO) {
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPrice(roomDTO.getPrice());
        room.setAvailable(roomDTO.isAvailable());
        room.setDescription(roomDTO.getDescription());

        if (roomDTO.getImages() != null) {
            room.setImages(roomDTO.getImages());
        }
        if (roomDTO.getAmenities() != null) {
            room.setAmenities(roomDTO.getAmenities());
        }
    }
}