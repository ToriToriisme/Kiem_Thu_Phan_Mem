package com.example.horse_racing_management.service.impl;

import com.example.horse_racing_management.dto.UserDTO;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.Role;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.horse_racing_management.repository.RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toDTO(user);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO, String rawPassword) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        if (userDTO.getRole() != null) {
            Role role = roleRepository.findByKey(userDTO.getRole())
                    .orElseGet(() -> roleRepository.findByKey("ROLE_SPECTATOR").orElse(null));
            user.setRole(role);
        } else {
            user.setRole(roleRepository.findByKey("ROLE_SPECTATOR").orElse(null));
        }
        user.setBalance(userDTO.getBalance() != null ? userDTO.getBalance() : 0.0);

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUser(String id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getRole() != null) {
            roleRepository.findByKey(userDTO.getRole()).ifPresent(user::setRole);
        }
        if (userDTO.getBalance() != null) {
            user.setBalance(userDTO.getBalance());
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void updateUsersStatus(List<String> ids, boolean status) {
        List<User> users = (List<User>) userRepository.findAllById(ids);
        users.forEach(user -> user.setStatus(status));
        userRepository.saveAll(users);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().getKey() : null);
        dto.setFullName(user.getFullName());
        dto.setBalance(user.getBalance());
        dto.setStatus(user.getStatus() != null ? user.getStatus() : true);
        return dto;
    }
}
