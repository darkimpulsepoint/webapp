package by.darkimpulsepoint.todoapp.dao;

import by.darkimpulsepoint.todoapp.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User create(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    User update(User user);
    boolean delete(Long id);
}
