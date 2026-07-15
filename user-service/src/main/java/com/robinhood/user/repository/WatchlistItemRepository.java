package com.robinhood.user.repository;

import com.robinhood.user.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WatchlistItemRepository extends JpaRepository<Watchlist, UUID> {

    Optional<Watchlist> findByWatchlistGroupIdAndSymbol(UUID watchlistGroupId, String symbol);

    void deleteByWatchlistGroupIdAndSymbol(UUID watchlistGroupId, String symbol);
}
