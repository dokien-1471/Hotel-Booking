package com.project.hotel.service;

import com.project.hotel.dto.UserDTO;
import com.project.hotel.entity.User;

import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    User findUserEntityById(Long id);
}
