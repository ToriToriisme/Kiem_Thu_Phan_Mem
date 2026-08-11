package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.UserDTO;
import com.example.horse_racing_management.entity.Role;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.repository.RoleRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

        ```
        @Mock
        private UserRepository userRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private UserServiceImpl userService;

        private User user;
        private Role role;
        private UserDTO userDTO;

        @BeforeEach
        void setUp() {

                role = new Role();
                role.setKey("ROLE_SPECTATOR");

                user = new User();
                user.setId("user-001");
                user.setUsername("testuser");
                user.setEmail("test@gmail.com");
                user.setFullName("Test User");
                user.setBalance(100.0);
                user.setStatus(true);
                user.setRole(role);
                user.setPassword("encoded-password");

                userDTO = new UserDTO();
                userDTO.setId("user-001");
                userDTO.setUsername("testuser");
                userDTO.setEmail("test@gmail.com");
                userDTO.setFullName("Test User");
                userDTO.setBalance(100.0);
                userDTO.setStatus(true);
                userDTO.setRole("ROLE_SPECTATOR");
        }

        // 1. TEST getAllUsers()

        @Test
        void getAllUsers_whenUsersExist_returnsUserList() {

                when(userRepository.findAll())
                                .thenReturn(List.of(user));

                List<UserDTO> result = userService.getAllUsers();

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("testuser", result.get(0).getUsername());
                assertEquals("test@gmail.com", result.get(0).getEmail());

                verify(userRepository, times(1))
                                .findAll();
        }

        @Test
        void getAllUsers_whenNoUsers_returnsEmptyList() {

                when(userRepository.findAll())
                                .thenReturn(Collections.emptyList());

                List<UserDTO> result = userService.getAllUsers();

                assertNotNull(result);
                assertTrue(result.isEmpty());

                verify(userRepository, times(1))
                                .findAll();
        }

        // 2. TEST getUserById()

        @Test
        void getUserById_whenUserExists_returnsUserDTO() {

                when(userRepository.findById("user-001"))
                                .thenReturn(Optional.of(user));

                UserDTO result = userService.getUserById("user-001");

                assertNotNull(result);
                assertEquals("user-001", result.getId());
                assertEquals("testuser", result.getUsername());
                assertEquals("test@gmail.com", result.getEmail());

                verify(userRepository, times(1))
                                .findById("user-001");
        }

        @Test
        void getUserById_whenUserNotFound_throwsException() {

                when(userRepository.findById("unknown"))
                                .thenReturn(Optional.empty());

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> userService.getUserById("unknown"));

                assertEquals(
                                "User not found with id: unknown",
                                exception.getMessage());

                verify(userRepository, times(1))
                                .findById("unknown");
        }

        // 3. TEST createUser()

        @Test
        void createUser_whenUsernameAvailable_createsUserSuccessfully() {

                UserDTO newUserDTO = new UserDTO();
                newUserDTO.setUsername("newuser");
                newUserDTO.setEmail("new@gmail.com");
                newUserDTO.setFullName("New User");
                newUserDTO.setRole("ROLE_SPECTATOR");
                newUserDTO.setBalance(50.0);

                when(userRepository.existsByUsername("newuser"))
                                .thenReturn(false);

                when(passwordEncoder.encode("123456"))
                                .thenReturn("encoded-password");

                when(roleRepository.findByKey("ROLE_SPECTATOR"))
                                .thenReturn(Optional.of(role));

                User savedUser = new User();
                savedUser.setId("user-new");
                savedUser.setUsername("newuser");
                savedUser.setEmail("new@gmail.com");
                savedUser.setFullName("New User");
                savedUser.setBalance(50.0);
                savedUser.setStatus(true);
                savedUser.setRole(role);
                savedUser.setPassword("encoded-password");

                when(userRepository.save(any(User.class)))
                                .thenReturn(savedUser);

                UserDTO result = userService.createUser(
                                newUserDTO,
                                "123456");

                assertNotNull(result);

                assertEquals(
                                "newuser",
                                result.getUsername());

                assertEquals(
                                "new@gmail.com",
                                result.getEmail());

                verify(userRepository)
                                .existsByUsername("newuser");

                verify(passwordEncoder)
                                .encode("123456");

                verify(roleRepository)
                                .findByKey("ROLE_SPECTATOR");

                verify(userRepository)
                                .save(any(User.class));
        }

        @Test
        void createUser_passwordIsEncoded_beforeSaving() {

                UserDTO newUserDTO = new UserDTO();
                newUserDTO.setUsername("passworduser");
                newUserDTO.setEmail("password@gmail.com");
                newUserDTO.setFullName("Password User");
                newUserDTO.setRole("ROLE_SPECTATOR");

                when(userRepository.existsByUsername("passworduser"))
                                .thenReturn(false);

                when(passwordEncoder.encode("123456"))
                                .thenReturn("encoded-123456");

                when(roleRepository.findByKey("ROLE_SPECTATOR"))
                                .thenReturn(Optional.of(role));

                when(userRepository.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                UserDTO result = userService.createUser(
                                newUserDTO,
                                "123456");

                assertNotNull(result);

                verify(passwordEncoder)
                                .encode("123456");

                verify(userRepository)
                                .save(argThat(savedUser -> "encoded-123456"
                                                .equals(savedUser.getPassword())
                                                && !"123456"
                                                                .equals(savedUser.getPassword())));
        }

        @Test
        void createUser_whenUsernameAlreadyExists_throwsException() {

                when(userRepository.existsByUsername("testuser"))
                                .thenReturn(true);

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> userService.createUser(
                                                userDTO,
                                                "123456"));

                assertEquals(
                                "Username is already taken",
                                exception.getMessage());

                verify(userRepository)
                                .existsByUsername("testuser");

                verify(userRepository, never())
                                .save(any(User.class));

                verify(passwordEncoder, never())
                                .encode(anyString());
        }

        // 4. TEST updateUser()

        @Test
        void updateUser_whenUserExists_updatesUserSuccessfully() {

                when(userRepository.findById("user-001"))
                                .thenReturn(Optional.of(user));

                when(roleRepository.findByKey("ROLE_SPECTATOR"))
                                .thenReturn(Optional.of(role));

                when(userRepository.save(any(User.class)))
                                .thenReturn(user);

                UserDTO updateDTO = new UserDTO();
                updateDTO.setFullName("Updated Name");
                updateDTO.setEmail("updated@gmail.com");
                updateDTO.setRole("ROLE_SPECTATOR");
                updateDTO.setBalance(500.0);

                UserDTO result = userService.updateUser(
                                "user-001",
                                updateDTO);

                assertNotNull(result);
                assertEquals(
                                "Updated Name",
                                result.getFullName());

                assertEquals(
                                "updated@gmail.com",
                                result.getEmail());

                assertEquals(
                                500.0,
                                result.getBalance());

                verify(userRepository)
                                .findById("user-001");

                verify(roleRepository)
                                .findByKey("ROLE_SPECTATOR");

                verify(userRepository)
                                .save(user);
        }

        @Test
        void updateUser_whenUserNotFound_throwsException() {

                when(userRepository.findById("unknown"))
                                .thenReturn(Optional.empty());

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> userService.updateUser(
                                                "unknown",
                                                userDTO));

                assertEquals(
                                "User not found with id: unknown",
                                exception.getMessage());

                verify(userRepository)
                                .findById("unknown");

                verify(userRepository, never())
                                .save(any(User.class));
        }

        // 5. TEST deleteUser()

        @Test
        void deleteUser_whenUserExists_deletesSuccessfully() {

                when(userRepository.existsById("user-001"))
                                .thenReturn(true);

                userService.deleteUser("user-001");

                verify(userRepository, times(1))
                                .existsById("user-001");

                verify(userRepository, times(1))
                                .deleteById("user-001");
        }

        @Test
        void deleteUser_whenUserNotFound_throwsException() {

                when(userRepository.existsById("unknown"))
                                .thenReturn(false);

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> userService.deleteUser("unknown"));

                assertEquals(
                                "User not found with id: unknown",
                                exception.getMessage());

                verify(userRepository)
                                .existsById("unknown");

                verify(userRepository, never())
                                .deleteById(anyString());
        }

        // 6. TEST updateUsersStatus()

@Test
void updateUsersStatus_updatesStatusSuccessfully() {

    User user2 = new User();

    user2.setId("user-002");
    user2.setUsername("user2");
    user2.setStatus(true);

    List<User> users =
            Arrays.asList(user, user2);

    List<String> ids =
            Arrays.asList(
                    "user-001",
                    "user-002"
            );

    when(userRepository.findAllById(ids))
            .thenReturn(users);

    when(userRepository.saveAll(users))
            .thenReturn(users);

    userService.updateUsersStatus(
            ids,
            false
    );

    assertFalse(user.getStatus());
    assertFalse(user2.getStatus());

    verify(userRepository)
            .findAllById(ids);

    verify(userRepository)
            .saveAll(users);
}```

}

// run test: mvnw.cmd -Dtest=UserServiceImplTest test
