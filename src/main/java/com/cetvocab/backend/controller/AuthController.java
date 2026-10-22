package com.cetvocab.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.model.AuthRequest;
import com.cetvocab.backend.model.AuthResponse;
import com.cetvocab.backend.model.PasswordChangeRequest;
import com.cetvocab.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest req,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        return authService.register(req, request, response);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        return authService.login(req, request, response);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(HttpServletRequest request) {
        AuthResponse current = authService.current(request);
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(current);
    }

    @PostMapping("/guest")
    public AuthResponse guest(HttpServletRequest request, HttpServletResponse response) {
        return authService.guest(request, response);
    }

    @PostMapping("/password")
    public void changePassword(@RequestBody PasswordChangeRequest req) {
        authService.changePassword(req.oldPassword(), req.newPassword());
    }
}
