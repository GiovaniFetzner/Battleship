CREATE TABLE games (
    id UUID PRIMARY KEY,
    status VARCHAR(40) NOT NULL,
    current_turn_player_id UUID,
    winner_player_id UUID,
    turn_counter INTEGER NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT chk_games_status
        CHECK (status IN ('WAITING_FOR_PLAYERS', 'PLACING_SHIPS', 'IN_PROGRESS', 'FINISHED'))
);

CREATE TABLE players (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL,
    name VARCHAR(60) NOT NULL,
    ready BOOLEAN NOT NULL,
    seat_number INTEGER NOT NULL,
    CONSTRAINT fk_players_game
        FOREIGN KEY (game_id)
        REFERENCES games (id)
);

CREATE TABLE boards (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL UNIQUE,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    CONSTRAINT fk_boards_player
        FOREIGN KEY (player_id)
        REFERENCES players (id)
);

CREATE TABLE attacks (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL,
    attacker_player_id UUID NOT NULL,
    target_x INTEGER NOT NULL,
    target_y INTEGER NOT NULL,
    result VARCHAR(40) NOT NULL,
    attacked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_attacks_game
        FOREIGN KEY (game_id)
        REFERENCES games (id),
    CONSTRAINT fk_attacks_attacker
        FOREIGN KEY (attacker_player_id)
        REFERENCES players (id),
    CONSTRAINT chk_attacks_result
        CHECK (result IN ('HIT', 'MISS', 'DESTROYED'))
);

CREATE TABLE board_ship_cells (
    board_id UUID NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    CONSTRAINT pk_board_ship_cells PRIMARY KEY (board_id, x, y),
    CONSTRAINT fk_board_ship_cells_board
        FOREIGN KEY (board_id)
        REFERENCES boards (id)
);

CREATE TABLE board_hit_cells (
    board_id UUID NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    CONSTRAINT pk_board_hit_cells PRIMARY KEY (board_id, x, y),
    CONSTRAINT fk_board_hit_cells_board
        FOREIGN KEY (board_id)
        REFERENCES boards (id)
);

CREATE TABLE board_miss_cells (
    board_id UUID NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    CONSTRAINT pk_board_miss_cells PRIMARY KEY (board_id, x, y),
    CONSTRAINT fk_board_miss_cells_board
        FOREIGN KEY (board_id)
        REFERENCES boards (id)
);

CREATE INDEX idx_players_game_id ON players (game_id);
CREATE INDEX idx_attacks_game_id ON attacks (game_id);
CREATE INDEX idx_attacks_attacker_player_id ON attacks (attacker_player_id);
