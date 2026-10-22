package com.cetvocab.backend.service;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cetvocab.backend.mapper.UserMapper;
import com.cetvocab.backend.model.AuthRequest;
import com.cetvocab.backend.model.AuthResponse;
import com.cetvocab.backend.model.UserRow;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");
    private static final int PASSWORD_MIN = 6;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(AuthRequest req, HttpServletRequest request, HttpServletResponse response) {
        validateUsername(req.username());
        validatePassword(req.password());

        String displayName = (req.displayName() == null || req.displayName().isBlank())
                ? req.username()
                : req.displayName().trim();

        if (userMapper.countByUsername(req.username()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }

        UserRow row = new UserRow();
        row.setUsername(req.username());
        row.setPasswordHash(passwordEncoder.encode(req.password()));
        row.setDisplayName(displayName);
        row.setGuest(false);
        userMapper.insert(row);

        // 注册成功后立即建立会话
        return login(new AuthRequest(req.username(), req.password(), null), request, response);
    }

    public AuthResponse login(AuthRequest req, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        UserRow row = userMapper.findByUsername(req.username());
        return new AuthResponse(row.getId(), row.getUsername(), row.getDisplayName(), row.isGuest());
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(
                SecurityContextHolder.createEmptyContext(), request, response);
    }

    public AuthResponse current(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        UserRow row = userMapper.findByUsername(auth.getName());
        if (row == null) return null;
        return new AuthResponse(row.getId(), row.getUsername(), row.getDisplayName(), row.isGuest());
    }

    public AuthResponse guest(HttpServletRequest request, HttpServletResponse response) {
        String username;
        do {
            username = "guest_" + System.currentTimeMillis()
                    + "_" + UUID.randomUUID().toString().substring(0, 6);
        } while (userMapper.countByUsername(username) > 0);

        String rawPassword = UUID.randomUUID().toString();
        UserRow row = new UserRow();
        row.setUsername(username);
        row.setPasswordHash(passwordEncoder.encode(rawPassword));
        row.setDisplayName("游客");
        row.setGuest(true);
        userMapper.insert(row);

        AuthRequest login = new AuthRequest(username, rawPassword, null);
        return login(login, request, response);
    }

    public void changePassword(String oldPassword, String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }

        validatePassword(newPassword);

        UserRow row = userMapper.findByUsername(auth.getName());
        if (row == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, row.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "原密码错误");
        }

        userMapper.updatePassword(row.getId(), passwordEncoder.encode(newPassword));
    }

    private void validateUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "用户名必须为 3-32 位字母、数字或下划线");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "密码至少 " + PASSWORD_MIN + " 位");
        }
    }
}
