package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserById(String id);

    UserDTO createUser(UserDTO userDTO, String rawPassword);

    UserDTO updateUser(String id, UserDTO userDTO);

    void deleteUser(String id);

    void updateUsersStatus(List<String> ids, boolean status);
}
