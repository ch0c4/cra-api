package org.johan.cra.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(String username, List<String> roles, String access_token) {}
