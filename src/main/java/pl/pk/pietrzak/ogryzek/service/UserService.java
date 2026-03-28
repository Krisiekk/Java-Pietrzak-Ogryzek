package pl.pk.pietrzak.ogryzek.service;

import java.util.List;
import org.springframework.stereotype.Service;
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
}