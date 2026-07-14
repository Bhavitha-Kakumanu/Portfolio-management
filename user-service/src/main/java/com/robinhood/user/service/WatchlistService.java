package com.robinhood.user.service;

import com.robinhood.user.dto.CreateWatchlistRequest;
import com.robinhood.user.dto.FavoriteStockRequest;
import com.robinhood.user.dto.WatchlistGroupResponse;
import com.robinhood.user.dto.WatchlistResponse;
import com.robinhood.user.exception.UserNotFoundException;
import com.robinhood.user.exception.WatchlistAlreadyExistsException;
import com.robinhood.user.exception.WatchlistNotFoundException;
import com.robinhood.user.model.User;
import com.robinhood.user.model.Watchlist;
import com.robinhood.user.model.WatchlistGroup;
import com.robinhood.user.repository.UserRepository;
import com.robinhood.user.repository.WatchlistGroupRepository;
import com.robinhood.user.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistGroupRepository watchlistGroupRepository;
    private final UserRepository userRepository;

    public WatchlistService(WatchlistRepository watchlistRepository,
                            WatchlistGroupRepository watchlistGroupRepository,
                            UserRepository userRepository) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistGroupRepository = watchlistGroupRepository;
        this.userRepository = userRepository;
    }

    public List<WatchlistResponse> getForUser(UUID userId) {
        validateUser(userId);
        return watchlistRepository.findByUserId(userId).stream()
            .map(this::toWatchlistResponse)
            .collect(Collectors.toList());
    }

    public WatchlistResponse add(UUID userId, String symbol) {
        User user = validateUser(userId);
        var existing = watchlistRepository.findByUserIdAndSymbol(userId, symbol);
        if (existing.isPresent()) {
            return toWatchlistResponse(existing.get());
        }
        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        watchlist.setSymbol(symbol);
        watchlist.setFavorite(false);
        Watchlist saved = watchlistRepository.save(watchlist);
        return toWatchlistResponse(saved);
    }

    @Transactional
    public void remove(UUID userId, String symbol) {
        validateUser(userId);
        watchlistRepository.deleteByUserIdAndSymbol(userId, symbol);
    }

    public List<WatchlistGroupResponse> getGroupsForUser(UUID userId) {
        validateUser(userId);
        return watchlistGroupRepository.findByUserId(userId).stream()
            .map(this::toWatchlistGroupResponse)
            .collect(Collectors.toList());
    }

    public WatchlistGroupResponse createGroup(UUID userId, String name) {
        User user = validateUser(userId);
        if (watchlistGroupRepository.findByUserIdAndName(userId, name).isPresent()) {
            throw new WatchlistAlreadyExistsException("Watchlist group already exists: " + name);
        }
        WatchlistGroup group = new WatchlistGroup();
        group.setUser(user);
        group.setName(name);
        WatchlistGroup saved = watchlistGroupRepository.save(group);
        return toWatchlistGroupResponse(saved);
    }

    public WatchlistGroupResponse getGroup(UUID userId, UUID groupId) {
        WatchlistGroup group = findGroupForUser(userId, groupId);
        return toWatchlistGroupResponse(group);
    }

    public List<WatchlistResponse> getItems(UUID userId, UUID groupId) {
        WatchlistGroup group = findGroupForUser(userId, groupId);
        return group.getItems().stream()
            .map(this::toWatchlistResponse)
            .collect(Collectors.toList());
    }

    public WatchlistResponse addStockToGroup(UUID userId, UUID groupId, String symbol) {
        WatchlistGroup group = findGroupForUser(userId, groupId);
        var existing = watchlistRepository.findByWatchlistGroupIdAndSymbol(groupId, symbol);
        if (existing.isPresent()) {
            return toWatchlistResponse(existing.get());
        }
        Watchlist item = new Watchlist();
        item.setUser(group.getUser());
        item.setWatchlistGroup(group);
        item.setSymbol(symbol);
        item.setFavorite(false);
        Watchlist saved = watchlistRepository.save(item);
        return toWatchlistResponse(saved);
    }

    @Transactional
    public void removeStockFromGroup(UUID userId, UUID groupId, String symbol) {
        findGroupForUser(userId, groupId);
        watchlistRepository.deleteByWatchlistGroupIdAndSymbol(groupId, symbol);
    }

    public WatchlistResponse setFavorite(UUID userId, UUID groupId, String symbol, boolean favorite) {
        findGroupForUser(userId, groupId);
        Watchlist item = watchlistRepository.findByWatchlistGroupIdAndSymbol(groupId, symbol)
            .orElseThrow(() -> new WatchlistNotFoundException("Stock not found in watchlist group: " + symbol));
        item.setFavorite(favorite);
        Watchlist saved = watchlistRepository.save(item);
        return toWatchlistResponse(saved);
    }

    public WatchlistResponse setFavorite(UUID userId, String symbol, boolean favorite) {
        validateUser(userId);
        Watchlist item = watchlistRepository.findByUserIdAndSymbol(userId, symbol)
            .orElseThrow(() -> new WatchlistNotFoundException("Stock not found in watchlist: " + symbol));
        item.setFavorite(favorite);
        Watchlist saved = watchlistRepository.save(item);
        return toWatchlistResponse(saved);
    }

    private User validateUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private WatchlistGroup findGroupForUser(UUID userId, UUID groupId) {
        return watchlistGroupRepository.findByUserIdAndId(userId, groupId)
            .orElseThrow(() -> new WatchlistNotFoundException("Watchlist group not found: " + groupId));
    }

    private WatchlistResponse toWatchlistResponse(Watchlist watchlist) {
        return new WatchlistResponse(
            watchlist.getId(),
            watchlist.getSymbol(),
            watchlist.getCreatedAt(),
            watchlist.isFavorite(),
            watchlist.getWatchlistGroup() != null ? watchlist.getWatchlistGroup().getId() : null
        );
    }

    private WatchlistGroupResponse toWatchlistGroupResponse(WatchlistGroup group) {
        return new WatchlistGroupResponse(
            group.getId(),
            group.getName(),
            group.getCreatedAt(),
            group.getItems().size()
        );
    }
}
