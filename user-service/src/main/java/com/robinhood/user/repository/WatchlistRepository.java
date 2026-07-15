package com.robinhood.user.repository;

import com.robinhood.user.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

    List<Watchlist> findByUserId(UUID userId);

    Optional<Watchlist> findByUserIdAndSymbol(UUID userId, String symbol);

    Optional<Watchlist> findByWatchlistGroupIdAndSymbol(UUID watchlistGroupId, String symbol);

    void deleteByUserIdAndSymbol(UUID userId, String symbol);

    void deleteByWatchlistGroupIdAndSymbol(UUID watchlistGroupId, String symbol);
}
