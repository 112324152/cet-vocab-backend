package com.cetvocab.backend.model;

public record PasswordChangeRequest(String oldPassword, String newPassword) {}
