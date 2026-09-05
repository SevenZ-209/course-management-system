package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserRepository {

    User getUserById(Integer id);

    User getUserByIdForUpdate(Integer id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User addUser(User user);

    void updateUser(User user);

    void deleteUser(Integer id);

    List<User> getUsers(Map<String, String> params);

    List<User> getAllUsers();

    long countUsers(Map<String, String> params);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> getUsersByRole(User.UserRole role);

    List<User> searchUsersByRole(User.UserRole role, String keyword, int page, int size);
}