package org.johan.cra.services;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.stream.StreamSupport;
import org.johan.cra.domains.entities.UserEntity;
import org.johan.cra.domains.requests.RegisterRequest;
import org.johan.cra.domains.responses.UserResponse;
import org.johan.cra.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

@Singleton
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public void register(RegisterRequest request) {
    var optionalUser = repository.findByEmail(request.getEmail());
    if (optionalUser.isPresent()) {
      throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Email already exist");
    }

    var newUser =
        new UserEntity(
            null,
            request.getEmail(),
            BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12)),
            "Default");
    repository.save(newUser);
  }

  public List<UserResponse> all() {
    var users = repository.findByRole("Default");
    return users.stream()
        .map(user -> new UserResponse(user.getId(), user.getEmail(), user.getRole()))
        .toList();
  }
}
