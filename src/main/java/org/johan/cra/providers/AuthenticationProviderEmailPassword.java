package org.johan.cra.providers;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import org.johan.cra.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;

@Singleton
public class AuthenticationProviderEmailPassword implements AuthenticationProvider {

  private final UserRepository repository;

  public AuthenticationProviderEmailPassword(UserRepository repository) {
    this.repository = repository;
  }

  @Override
  public Publisher<AuthenticationResponse> authenticate(
      HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> request) {
    return Flux.create(
        emitter -> {
          var optionalUser = repository.findByEmail((String) request.getIdentity());
          if (optionalUser.isEmpty()) {
            emitter.error(AuthenticationResponse.exception());
            return;
          }
          var user = optionalUser.get();

          if (!BCrypt.checkpw((String) request.getSecret(), user.getPassword())) {
            emitter.error(AuthenticationResponse.exception());
            return;
          }
          emitter.next(AuthenticationResponse.success(user.getEmail(), List.of(user.getRole())));
          emitter.complete();
        },
        FluxSink.OverflowStrategy.ERROR);
  }
}
