package com.cetvocab.backend.model;

public record AuthResponse(Long id, String username, String displayName, boolean guest) {}
