package pl.pk.pietrzak.ogryzek.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.pk.pietrzak.ogryzek.entity.UserRepository;
import pl.pk.pietrzak.ogryzek.entity.Users;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users createUser(Users user) {
        return userRepository.save(user);
    }

    public Users updateUser(Integer id, Users userData) {
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uzytkownik o podanym id nie istnieje"));

        existingUser.setUsername(userData.getUsername());
        existingUser.setProjects(userData.getProjects());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uzytkownik o podanym id nie istnieje");
        }
        userRepository.deleteById(id);
    }
}