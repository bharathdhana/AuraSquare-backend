package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.ResourceNotFoundException;
import com.bharath.ecommerceapi.exception.UnAuthorizedException;
import com.bharath.ecommerceapi.model.User;
import com.bharath.ecommerceapi.model.dto.response.UserResponse;
import com.bharath.ecommerceapi.repo.UserRepository;
import com.bharath.ecommerceapi.service.inf.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(
                this::mapToUserResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapToUserResponse(userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User Not Found for the Given ID: " + id)));
    }

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnAuthorizedException("User Not Found"));
    }

    @Override
    @jakarta.transaction.Transactional
    public String deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found for the Given ID: " + id));
        userRepository.delete(user);
        return "User Deleted Successfully";
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
