package by.darkimpulsepoint.todoapp.service;

import by.darkimpulsepoint.todoapp.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(String username, String email, String password);
    Optional<User> authenticate(String username, String password);
    Optional<User> findById(Long id);
    List<User> findAll();
    boolean deleteUser(Long id);
}
