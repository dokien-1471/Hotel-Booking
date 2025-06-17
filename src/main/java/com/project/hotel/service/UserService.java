package com.project.hotel.service;

import com.project.hotel.dto.UserDTO;
import com.project.hotel.entity.User;
import com.project.hotel.exception.DuplicateResourceException;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.exception.ValidationException;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDTO createUser(UserDTO userDTO) throws ValidationException, DuplicateResourceException;


    UserDTO getUserById(Long id) throws ResourceNotFoundException;


    UserDTO getUserByEmail(String email) throws ResourceNotFoundException;


    List<UserDTO> getAllUsers();


    UserDTO updateUser(Long id, UserDTO userDTO)
            throws ResourceNotFoundException, ValidationException, DuplicateResourceException;


    void deleteUser(Long id) throws ResourceNotFoundException;

    User findUserEntityById(Long id);

    Optional<User> findByEmail(String email);
}