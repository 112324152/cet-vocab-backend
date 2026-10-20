package com.cetvocab.backend.model;

import java.util.List;

public record BatchLookupRequest(List<String> words) {}