package com.bottledrop.userservice.controller;
import jakarta.validation.Valid;
import com.bottledrop.userservice.model.User;
import com.bottledrop.userservice.model.Role;
import com.bottledrop.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;




@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        return ResponseEntity.ok(service.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password) {
        Optional<User> user = service.login(username, password);
        return user.map(u -> ResponseEntity.ok("Login successful"))
                   .orElse(ResponseEntity.status(401).body("Login failed"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return service.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable Long id,
                                           @RequestParam Role role) {
        return service.assignRole(id, role)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}