package com.example.battleship.repository.impl.jpa;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.game.Turn;
import com.example.battleship.domain.map.Board;
import com.example.battleship.domain.map.Cell;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.dto.persistence.BoardSnapshotDto;
import com.example.battleship.dto.persistence.CoordinateSnapshotDto;
import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.dto.persistence.PlayerSnapshotDto;
import com.example.battleship.persistence.entity.GameEntity;
import com.example.battleship.persistence.entity.GameStatus;
import com.example.battleship.persistence.mapper.GameEntityMapper;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.repository.jpa.GameJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Primary
@Profile({ "prod", "test" })
public class JpaGameRepository implements GameRepository {

    private static final Field GAME_ID_FIELD = field(Game.class, "id");
    private static final Field GAME_STATE_FIELD = field(Game.class, "state");
    private static final Field GAME_PLAYER2_FIELD = field(Game.class, "player2");
    private static final Field GAME_CURRENT_PLAYER_FIELD = field(Game.class, "currentPlayer");
    private static final Field GAME_WINNER_FIELD = field(Game.class, "winner");
    private static final Field GAME_CURRENT_TURN_FIELD = field(Game.class, "currentTurn");
    private static final Field GAME_TURN_COUNTER_FIELD = field(Game.class, "turnCounter");
    private static final Field GAME_READY_PLAYERS_FIELD = field(Game.class, "readyPlayers");
    private static final Field PLAYER_BOARD_FIELD = field(Player.class, "board");
    private static final Field PLAYER_SHIPS_PLACED_FIELD = field(Player.class, "shipsPlaced");
    private static final Field BOARD_CELLS_FIELD = field(Board.class, "cells");
    private static final Field BOARD_SHIPS_FIELD = field(Board.class, "ships");
    private static final Field CELL_SHIP_FIELD = field(Cell.class, "ship");
    private static final Field CELL_ATTACKED_FIELD = field(Cell.class, "attacked");
    private static final Field SHIP_HITS_FIELD = field(Ship.class, "hits");

    private final GameJpaRepository springDataRepository;
    private final GameEntityMapper gameEntityMapper;
    private final Map<String, UUID> gameIdsByString = new ConcurrentHashMap<>();

    public JpaGameRepository(GameJpaRepository springDataRepository, GameEntityMapper gameEntityMapper) {
        this.springDataRepository = springDataRepository;
        this.gameEntityMapper = gameEntityMapper;
    }

    @Override
    public void save(Game game) {
        GameSnapshotDto snapshot = toSnapshot(game);
        GameEntity entity = springDataRepository.findAggregateById(snapshot.getId())
                .orElseGet(() -> {
                    GameEntity created = GameEntity.createNew();
                    created.assignId(snapshot.getId());
                    return created;
                });
        gameEntityMapper.updateEntityFromSnapshot(snapshot, entity);
        springDataRepository.saveAndFlush(entity);
        gameIdsByString.put(game.getId(), snapshot.getId());
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return resolveUuid(gameId)
                .flatMap(springDataRepository::findAggregateById)
                .map(gameEntityMapper::toSnapshot)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(String gameId) {
        resolveUuid(gameId).ifPresent(springDataRepository::deleteById);
        gameIdsByString.remove(gameId);
    }

    @Override
    public Map<String, Game> findAll() {
        Map<String, Game> result = new HashMap<>();
        for (GameEntity entity : springDataRepository.findAll()) {
            Game game = toDomain(gameEntityMapper.toSnapshot(entity));
            result.put(game.getId(), game);
            gameIdsByString.put(game.getId(), UUID.fromString(game.getId()));
        }
        return result;
    }

    private GameSnapshotDto toSnapshot(Game game) {
        GameSnapshotDto snapshot = new GameSnapshotDto();
        UUID gameId = UUID.fromString(game.getId());
        snapshot.setId(gameId);
        snapshot.setStatus(GameStatus.valueOf(game.getState().name()));
        snapshot.setTurnCounter(game.getTurnCounter());
        snapshot.setPlayers(new ArrayList<>());
        snapshot.setAttacks(new ArrayList<>());

        Player player1 = game.getPlayer1();
        UUID player1Id = playerUuid(player1.getName(), game.getId(), 0);
        snapshot.getPlayers().add(toPlayerSnapshot(player1, player1Id, 0));

        Player player2 = game.getPlayer2();
        if (player2 != null) {
            UUID player2Id = playerUuid(player2.getName(), game.getId(), 1);
            snapshot.getPlayers().add(toPlayerSnapshot(player2, player2Id, 1));
        }

        if (game.getCurrentPlayer() != null) {
            snapshot.setCurrentTurnPlayerId(playerUuid(game.getCurrentPlayer().getName(), game.getId(),
                    seatByName(game, game.getCurrentPlayer().getName())));
        }

        if (game.getWinner() != null) {
            snapshot.setWinnerPlayerId(playerUuid(game.getWinner().getName(), game.getId(),
                    seatByName(game, game.getWinner().getName())));
        }

        return snapshot;
    }

    private PlayerSnapshotDto toPlayerSnapshot(Player player, UUID playerId, int seatNumber) {
        PlayerSnapshotDto snapshot = new PlayerSnapshotDto();
        snapshot.setId(playerId);
        snapshot.setName(player.getName());
        snapshot.setSeatNumber(seatNumber);
        snapshot.setReady(player.hasPlacedShips());
        snapshot.setBoard(toBoardSnapshot(player.getBoard()));
        return snapshot;
    }

    private BoardSnapshotDto toBoardSnapshot(Board board) {
        BoardSnapshotDto snapshot = new BoardSnapshotDto();
        snapshot.setWidth(10);
        snapshot.setHeight(10);
        snapshot.setShipCells(new HashSet<>());
        snapshot.setHitCells(new HashSet<>());
        snapshot.setMissCells(new HashSet<>());

        Cell[][] cells = (Cell[][]) getField(BOARD_CELLS_FIELD, board);
        for (int x = 0; x < cells.length; x++) {
            for (int y = 0; y < cells[x].length; y++) {
                Cell cell = cells[x][y];
                if (cell.hasShip()) {
                    snapshot.getShipCells().add(new CoordinateSnapshotDto(x, y));
                }
                if (cell.isAttacked()) {
                    CoordinateSnapshotDto coordinate = new CoordinateSnapshotDto(x, y);
                    if (cell.hasShip()) {
                        snapshot.getHitCells().add(coordinate);
                    } else {
                        snapshot.getMissCells().add(coordinate);
                    }
                }
            }
        }

        return snapshot;
    }

    private Game toDomain(GameSnapshotDto snapshot) {
        List<PlayerSnapshotDto> players = snapshot.getPlayers();
        if (players == null || players.isEmpty()) {
            throw new IllegalStateException("Snapshot must contain at least one player");
        }

        PlayerSnapshotDto p1Snapshot = players.stream()
                .filter(p -> p.getSeatNumber() == 0)
                .findFirst()
                .orElse(players.get(0));

        Game game = new Game(new Player(p1Snapshot.getName()));
        setField(GAME_ID_FIELD, game, snapshot.getId().toString());

        Player player1 = game.getPlayer1();
        restoreBoard(player1, p1Snapshot.getBoard());
        setField(PLAYER_SHIPS_PLACED_FIELD, player1, p1Snapshot.isReady());

        PlayerSnapshotDto p2Snapshot = players.stream()
                .filter(p -> p.getSeatNumber() == 1)
                .findFirst()
                .orElse(null);

        if (p2Snapshot != null) {
            Player player2 = new Player(p2Snapshot.getName());
            game.addPlayer2(player2);
            restoreBoard(player2, p2Snapshot.getBoard());
            setField(PLAYER_SHIPS_PLACED_FIELD, player2, p2Snapshot.isReady());
            setField(GAME_PLAYER2_FIELD, game, player2);
        }

        setField(GAME_STATE_FIELD, game, GameState.valueOf(snapshot.getStatus().name()));

        Set<String> readyPlayers = new HashSet<>();
        for (PlayerSnapshotDto playerSnapshot : players) {
            if (playerSnapshot.isReady()) {
                readyPlayers.add(playerSnapshot.getName());
            }
        }
        setField(GAME_READY_PLAYERS_FIELD, game, readyPlayers);

        if (snapshot.getCurrentTurnPlayerId() != null) {
            Player currentPlayer = findPlayerBySnapshotId(game, snapshot.getCurrentTurnPlayerId());
            setField(GAME_CURRENT_PLAYER_FIELD, game, currentPlayer);
            setField(GAME_TURN_COUNTER_FIELD, game, snapshot.getTurnCounter());
            setField(GAME_CURRENT_TURN_FIELD, game,
                    currentPlayer != null ? new Turn(currentPlayer, snapshot.getTurnCounter()) : null);
        }

        if (snapshot.getWinnerPlayerId() != null) {
            setField(GAME_WINNER_FIELD, game, findPlayerBySnapshotId(game, snapshot.getWinnerPlayerId()));
        }

        gameIdsByString.put(game.getId(), snapshot.getId());
        return game;
    }

    private void restoreBoard(Player player, BoardSnapshotDto boardSnapshot) {
        if (boardSnapshot == null) {
            return;
        }

        Board board = (Board) getField(PLAYER_BOARD_FIELD, player);
        Cell[][] cells = (Cell[][]) getField(BOARD_CELLS_FIELD, board);
        @SuppressWarnings("unchecked")
        List<Ship> ships = (List<Ship>) getField(BOARD_SHIPS_FIELD, board);

        ships.clear();

        if (boardSnapshot.getShipCells() != null) {
            for (CoordinateSnapshotDto coordinate : boardSnapshot.getShipCells()) {
                int x = coordinate.x();
                int y = coordinate.y();
                Cell cell = cells[x][y];
                Ship ship = new Ship("persisted", 1);
                setField(CELL_SHIP_FIELD, cell, ship);
                ships.add(ship);
            }
        }

        if (boardSnapshot.getMissCells() != null) {
            for (CoordinateSnapshotDto coordinate : boardSnapshot.getMissCells()) {
                Cell cell = cells[coordinate.x()][coordinate.y()];
                setField(CELL_ATTACKED_FIELD, cell, true);
            }
        }

        if (boardSnapshot.getHitCells() != null) {
            for (CoordinateSnapshotDto coordinate : boardSnapshot.getHitCells()) {
                Cell cell = cells[coordinate.x()][coordinate.y()];
                setField(CELL_ATTACKED_FIELD, cell, true);
                Object shipObj = getField(CELL_SHIP_FIELD, cell);
                if (shipObj == null) {
                    Ship ship = new Ship("persisted", 1);
                    setField(CELL_SHIP_FIELD, cell, ship);
                    ships.add(ship);
                    shipObj = ship;
                }
                setField(SHIP_HITS_FIELD, shipObj, 1);
            }
        }
    }

    private Optional<UUID> resolveUuid(String gameId) {
        UUID mapped = gameIdsByString.get(gameId);
        if (mapped != null) {
            return Optional.of(mapped);
        }

        try {
            return Optional.of(UUID.fromString(gameId));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Player findPlayerBySnapshotId(Game game, UUID playerId) {
        if (playerId == null) {
            return null;
        }

        if (game.getPlayer1() != null
                && playerUuid(game.getPlayer1().getName(), game.getId(), 0).equals(playerId)) {
            return game.getPlayer1();
        }

        if (game.getPlayer2() != null
                && playerUuid(game.getPlayer2().getName(), game.getId(), 1).equals(playerId)) {
            return game.getPlayer2();
        }

        return null;
    }

    private int seatByName(Game game, String name) {
        if (game.getPlayer1() != null && game.getPlayer1().getName().equals(name)) {
            return 0;
        }
        if (game.getPlayer2() != null && game.getPlayer2().getName().equals(name)) {
            return 1;
        }
        return 0;
    }

    private UUID playerUuid(String playerName, String gameId, int seat) {
        String token = gameId + ":" + seat + ":" + playerName;
        return UUID.nameUUIDFromBytes(token.getBytes(StandardCharsets.UTF_8));
    }

    private static Field field(Class<?> type, String name) {
        try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException("Unable to access field " + name + " in " + type.getName(), ex);
        }
    }

    private static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Unable to read field " + field.getName(), ex);
        }
    }

    private static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Unable to set field " + field.getName(), ex);
        }
    }
}
