package pl.pk.pietrzak.ogryzek.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.pk.pietrzak.ogryzek.entity.UserRepository;
import pl.pk.pietrzak.ogryzek.entity.Users;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Powinien zwrocic wszystkich uzytkownikow")
    void shouldReturnAllUsers() {
        Users user1 = new Users();
        user1.setId(1);
        user1.setUsername("TestUser1");

        Users user2 = new Users();
        user2.setId(2);
        user2.setUsername("TestUser2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<Users> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("TestUser1", result.get(0).getUsername());
        assertEquals("TestUser2", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Powinien zwrocic uzytkownika po id")
    void shouldReturnUserById() {
        Users user = new Users();
        user.setId(1);
        user.setUsername("TestUser");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Optional<Users> result = userService.getUserById(1);

        assertTrue(result.isPresent());
        assertEquals("TestUser", result.get().getUsername());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Powinien zwrocic pusty Optional gdy uzytkownik nie istnieje")
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Users> result = userService.getUserById(999);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Powinien zapisac nowego uzytkownika")
    void shouldCreateUser() {
        Users user = new Users();
        user.setUsername("NowyUser");
        user.setProjects(new HashSet<>());

        when(userRepository.save(user)).thenReturn(user);

        Users result = userService.createUser(user);

        assertEquals("NowyUser", result.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Powinien zaktualizowac uzytkownika")
    void shouldUpdateUser() {
        Users existingUser = new Users();
        existingUser.setId(1);
        existingUser.setUsername("StaryUser");
        existingUser.setProjects(new HashSet<>());

        Users updatedUser = new Users();
        updatedUser.setUsername("NowyUser");
        updatedUser.setProjects(new HashSet<>());

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        Users result = userService.updateUser(1, updatedUser);

        assertEquals("NowyUser", result.getUsername());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Powinien usunac uzytkownika po id")
    void shouldDeleteUser() {
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        userService.deleteUser(1);

        verify(userRepository, times(1)).existsById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Powinien rzucic blad 404 gdy uzytkownik nie istnieje podczas usuwania")
    void shouldThrowExceptionWhenDeleteUserNotFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUser(1);
        });

        assertEquals(404, exception.getStatusCode().value());
        verify(userRepository, times(1)).existsById(1);
    }

    @Test
    @DisplayName("Powinien rzucic blad gdy uzytkownik nie istnieje podczas aktualizacji")
    void shouldThrowExceptionWhenUpdateUserNotFound() {
        Users updatedUser = new Users();
        updatedUser.setUsername("Updated");

        when(userRepository.findById(1)).thenReturn(java.util.Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1, updatedUser);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1);
    }
}