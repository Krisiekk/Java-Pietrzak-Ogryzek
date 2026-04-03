package pl.pk.pietrzak.ogryzek.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.pk.pietrzak.ogryzek.entity.Users;
import pl.pk.pietrzak.ogryzek.service.UserService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkich użytkowników")
    void shouldGetAllUsers() throws Exception {
        Users user1 = new Users();
        user1.setId(1);
        user1.setUsername("User1");

        Users user2 = new Users();
        user2.setId(2);
        user2.setUsername("User2");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("User1"))
                .andExpect(jsonPath("$[1].username").value("User2"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Powinien zwrócić użytkownika po ID")
    void shouldGetUserById() throws Exception {
        Users user = new Users();
        user.setId(1);
        user.setUsername("User1");
        user.setProjects(new HashSet<>());

        when(userService.getUserById(1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("User1"));

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    @DisplayName("Powinien utworzyć nowego użytkownika")
    void shouldCreateUser() throws Exception {
        Users user = new Users();
        user.setId(1);
        user.setUsername("NewUser");
        user.setProjects(new HashSet<>());

        when(userService.createUser(any(Users.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("NewUser"));

        verify(userService, times(1)).createUser(any(Users.class));
    }

    @Test
    @DisplayName("Powinien zaktualizować użytkownika")
    void shouldUpdateUser() throws Exception {
        Users updatedUser = new Users();
        updatedUser.setId(1);
        updatedUser.setUsername("UpdatedUser");
        updatedUser.setProjects(new HashSet<>());

        when(userService.updateUser(eq(1), any(Users.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("UpdatedUser"));

        verify(userService, times(1)).updateUser(eq(1), any(Users.class));
    }

    @Test
    @DisplayName("Powinien usunąć użytkownika")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1);
    }
}

