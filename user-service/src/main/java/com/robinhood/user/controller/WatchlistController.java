package com.robinhood.user.controller;

import com.robinhood.user.dto.AddWatchlistRequest;
import com.robinhood.user.dto.CreateWatchlistRequest;
import com.robinhood.user.dto.FavoriteStockRequest;
import com.robinhood.user.dto.WatchlistGroupResponse;
import com.robinhood.user.dto.WatchlistResponse;
import com.robinhood.user.service.WatchlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getMyWatchlist(@AuthenticationPrincipal String userId) {
        var list = watchlistService.getForUser(UUID.fromString(userId));
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<WatchlistResponse> add(@AuthenticationPrincipal String userId,
                                                 @Valid @RequestBody AddWatchlistRequest request) {
        var resp = watchlistService.add(UUID.fromString(userId), request.getSymbol());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal String userId,
                                       @PathVariable String symbol) {
        watchlistService.remove(UUID.fromString(userId), symbol);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{symbol}/favorite")
    public ResponseEntity<WatchlistResponse> favorite(@AuthenticationPrincipal String userId,
                                                      @PathVariable String symbol,
                                                      @Valid @RequestBody FavoriteStockRequest request) {
        var item = watchlistService.setFavorite(UUID.fromString(userId), symbol, request.getFavorite());
        return ResponseEntity.ok(item);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<WatchlistGroupResponse>> getGroups(@AuthenticationPrincipal String userId) {
        var groups = watchlistService.getGroupsForUser(UUID.fromString(userId));
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/groups")
    public ResponseEntity<WatchlistGroupResponse> createGroup(@AuthenticationPrincipal String userId,
                                                              @Valid @RequestBody CreateWatchlistRequest request) {
        var group = watchlistService.createGroup(UUID.fromString(userId), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<WatchlistGroupResponse> getGroup(@AuthenticationPrincipal String userId,
                                                           @PathVariable UUID groupId) {
        var group = watchlistService.getGroup(UUID.fromString(userId), groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/groups/{groupId}/items")
    public ResponseEntity<List<WatchlistResponse>> getGroupItems(@AuthenticationPrincipal String userId,
                                                                 @PathVariable UUID groupId) {
        var items = watchlistService.getItems(UUID.fromString(userId), groupId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/groups/{groupId}/items")
    public ResponseEntity<WatchlistResponse> addGroupItem(@AuthenticationPrincipal String userId,
                                                          @PathVariable UUID groupId,
                                                          @Valid @RequestBody AddWatchlistRequest request) {
        var item = watchlistService.addStockToGroup(UUID.fromString(userId), groupId, request.getSymbol());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/groups/{groupId}/items/{symbol}")
    public ResponseEntity<Void> removeGroupItem(@AuthenticationPrincipal String userId,
                                                @PathVariable UUID groupId,
                                                @PathVariable String symbol) {
        watchlistService.removeStockFromGroup(UUID.fromString(userId), groupId, symbol);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/groups/{groupId}/items/{symbol}/favorite")
    public ResponseEntity<WatchlistResponse> favoriteGroupItem(@AuthenticationPrincipal String userId,
                                                               @PathVariable UUID groupId,
                                                               @PathVariable String symbol,
                                                               @Valid @RequestBody FavoriteStockRequest request) {
        var item = watchlistService.setFavorite(UUID.fromString(userId), groupId, symbol, request.getFavorite());
        return ResponseEntity.ok(item);
    }
}
