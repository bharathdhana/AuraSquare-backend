package com.bharath.ecommerceapi.service.impl;

import com.bharath.ecommerceapi.exception.BadRequestException;
import com.bharath.ecommerceapi.model.Cart;
import com.bharath.ecommerceapi.model.User;
import com.bharath.ecommerceapi.model.UserPrincipal;
import com.bharath.ecommerceapi.model.Wishlist;
import com.bharath.ecommerceapi.model.dto.request.LoginRequest;
import com.bharath.ecommerceapi.model.dto.request.RegisterRequest;
import com.bharath.ecommerceapi.model.dto.response.AuthResponse;
import com.bharath.ecommerceapi.model.enums.Role;
import com.bharath.ecommerceapi.repo.CartRepository;
import com.bharath.ecommerceapi.repo.UserRepository;
import com.bharath.ecommerceapi.repo.WishlistRepository;
import com.bharath.ecommerceapi.service.inf.IAuthService;
import com.bharath.ecommerceapi.service.inf.IJwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final AuthenticationManager authenticationManager;
    private final IJwtService jwtService;
    private final BCryptPasswordEncoder encoder = new  BCryptPasswordEncoder(10);

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email already Exists!");
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        if (savedUser.getRole() == Role.USER) {
            Cart cart = Cart.builder()
                    .user(savedUser)
                    .build();
            cartRepository.save(cart);

            Wishlist wishlist = Wishlist.builder()
                    .user(savedUser)
                    .build();
            wishlistRepository.save(wishlist);
        }
        return "User Created Successfully!";
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        if (authentication.isAuthenticated()) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return AuthResponse.builder()
                    .token(jwtService.generateToken(request.getEmail()))
                    .role(userPrincipal.getUser().getRole().name())
                    .build();
        }
        return AuthResponse.builder()
                .token("")
                .role("")
                .build();
    }
}
