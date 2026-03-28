package pl.pk.pietrzak.ogryzek.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pk.pietrzak.ogryzek.entity.Users;
import pl.pk.pietrzak.ogryzek.service.UserService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Uzytkownicy", description = "Endpointy do zarzadzania uzytkownikami")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkich uzytkownikow", description = "Zwraca liste wszystkich uzytkownikow")
    public List<Users> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public Users createUser(@RequestBody Users user) {
        return userService.createUser(user);
    @Operation(summary = "Utworz nowego uzytkownika", description = "Tworzy nowego uzytkownika na podstawie przekazanych danych")
    public Users createUser(
            @Parameter(description = "Dane uzytkownika do utworzenia", required = true)
            @RequestBody Users user) {
        return userRepository.save(user);
    }
}