package com.robinhood.user.repository;

import com.robinhood.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// JpaRepository<EntityType, PrimaryKeyType>
// Spring Data auto-generates SQL for all standard CRUD operations at startup.
// Custom finders follow a naming convention: findBy<FieldName>
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Spring Data translates this to: SELECT * FROM users WHERE email = ? AND enabled = ?
    Optional<User> findByEmailAndEnabled(String email, boolean enabled);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
