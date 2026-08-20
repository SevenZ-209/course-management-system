package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.User;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;

public interface UserService extends UserDetailsService {

    User getUserById(Integer id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User addUser(User user);

    void updateUser(User user);

    void deleteUser(Integer id);

    List<User> getUsers(Map<String, String> params);

    List<User> getAllUsers();

    long countUsers(Map<String, String> params);

    boolean authenticate(String username, String password);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> getUsersByRole(User.UserRole role);
}