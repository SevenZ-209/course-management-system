package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.UserRepository;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Integer id) {
        return userRepository.getUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public User addUser(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new IllegalArgumentException("Username đã tồn tại!");

        if (userRepository.existsByEmail(user.getEmail()))
            throw new IllegalArgumentException("Email đã tồn tại!");

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        if (user.getRole() == null)
            user.setRole(User.UserRole.STUDENT);

        if (user.getStatus() == null)
            user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.addUser(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteUser(id);
    }

    @Override
    public List<User> getUsers(Map<String, String> params) {
        return userRepository.getUsers(params);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public long countUsers(Map<String, String> params) {
        return userRepository.countUsers(params);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = userRepository.getUserByUsername(username);

        return user != null
                && user.getStatus() == User.UserStatus.ACTIVE
                && passwordEncoder.matches(password, user.getPasswordHash());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUserByUsername(username);

        if (user == null)
            throw new UsernameNotFoundException("Tài khoản không tồn tại!");

        if (user.getStatus() != User.UserStatus.ACTIVE)
            throw new DisabledException("Tài khoản đã bị khóa hoặc ngừng hoạt động!");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }

    @Override
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.getUsersByRole(role);
    }

    @Override
    public List<User> searchUsersByRole(User.UserRole role, String keyword, int page, int size) {
        return userRepository.searchUsersByRole(role, keyword, page, size);
    }
}