package com.bottledrop.userservice.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.bottledrop.userservice.model.Role;
import com.bottledrop.userservice.model.User;
import com.bottledrop.userservice.repository.UserRepository;
import java.util.Optional;





@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User registerUser(User user) {
        user.setRole(Role.VISITOR);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));  // default role
        return repo.save(user);
    }

    public Optional<User> login(String username, String password) {
        return repo.findByUsername(username)
                   .filter(user -> new BCryptPasswordEncoder().matches(password, user.getPassword()));
    }

    public Optional<User> getUser(Long id) {
        return repo.findById(id);
    }

    public Optional<User> assignRole(Long id, Role newRole) {
        Optional<User> user = repo.findById(id);
        user.ifPresent(u -> {
            u.setRole(newRole);
            repo.save(u);
        });
        return user;
    }
}