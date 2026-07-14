package com.robinhood.user.repository;

import com.robinhood.user.model.WatchlistGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistGroupRepository extends JpaRepository<WatchlistGroup, UUID> {

    List<WatchlistGroup> findByUserId(UUID userId);

    Optional<WatchlistGroup> findByUserIdAndId(UUID userId, UUID id);

    Optional<WatchlistGroup> findByUserIdAndName(UUID userId, String name);
}
