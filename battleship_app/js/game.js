// Seleção e rotação dos navios na área de posicionamento
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".rotate-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const shipImg = btn.parentElement.querySelector(".ship-img");
            if (!shipImg) return;
            const current = shipImg.getAttribute("data-orientation") || "horizontal";
            const next = current === "horizontal" ? "vertical" : "horizontal";
            applyShipOrientation(shipImg, next);
        });
    });

    document.querySelectorAll(".ship-img").forEach(shipImg => {
        cacheShipPreviewSize(shipImg);
        applyShipOrientation(shipImg, shipImg.getAttribute("data-orientation") || "horizontal");
        shipImg.dataset.placed = "false";
    });
});
const board = document.getElementById("board");
const opponentBoard = document.getElementById("opponentBoard");
const shipsArea = document.getElementById("shipsArea");
const rotateSelectedShipButton = document.getElementById("rotateSelectedShip");
const trashModeButton = document.getElementById("trashModeButton");
const readyButton = document.getElementById("readyButton");
let selectedShip = null;
let isTrashModeActive = false;
let isPlayerReadyConfirmed = false;
let isReadySubmitting = false;
let gameSocket = null;
const SHIP_SIZES = {
    porta_avioes: 5,
    bombardeiro: 4,
    submarino: 3,
    lancha: 2
};
const SHIP_LABELS = {
    porta_avioes: "Porta-avioes",
    bombardeiro: "Bombardeiro",
    submarino: "Submarino",
    lancha: "Lancha"
};
const occupiedCells = new Set();
const placedShips = new Map();
const localShipsState = new Map();
const pendingAttacks = new Set();
const myAttackResults = new Map();
const attacksOnMyBoard = new Set();

Object.entries(SHIP_SIZES).forEach(([shipType, size]) => {
    localShipsState.set(shipType, {
        key: shipType,
        name: SHIP_LABELS[shipType] || shipType,
        size,
        hits: 0,
        destroyed: false,
        placed: false,
        cells: []
    });
});

function isShipAvailable(shipElement) {
    return shipElement?.dataset.placed !== "true";
}

function areAllShipsPlaced() {
    return placedShips.size === Object.keys(SHIP_SIZES).length;
}

function isPlacementLocked() {
    return isPlayerReadyConfirmed || isReadySubmitting;
}

function toAttackKey(x, y) {
    return `${x}-${y}`;
}

function getBoardCellByCoordinates(boardElement, row, col) {
    return boardElement?.querySelector(`.board-cell[data-row="${row}"][data-col="${col}"]`) || null;
}

function normalizeAttackResult(result) {
    const normalized = String(result || "").toUpperCase();
    if (normalized === "HIT" || normalized === "DESTROYED") {
        return "HIT";
    }

    return normalized === "MISS" ? "MISS" : null;
}

function markAttackOnBoard(boardElement, row, col, result) {
    const normalizedResult = normalizeAttackResult(result);
    if (!normalizedResult) {
        return;
    }

    const cell = getBoardCellByCoordinates(boardElement, row, col);
    if (!cell) {
        return;
    }

    cell.classList.remove("is-selected", "board-cell--miss", "board-cell--hit");
    cell.classList.add("board-cell--attacked");
    cell.classList.add(normalizedResult === "HIT" ? "board-cell--hit" : "board-cell--miss");
}

function updateBattleUi(gameState) {
    if (!opponentBoard) {
        return;
    }

    const canAttackNow = gameState?.gameStatus === "IN_PROGRESS" && gameState?.myTurn === true;
    opponentBoard.classList.toggle("board--disabled", !canAttackNow);

    if (battleHint) {
        if (gameState?.gameStatus === "PLACING_SHIPS") {
            battleHint.textContent = "Posicione seus navios no seu grid. O grid do oponente sera liberado na fase de batalha.";
        } else if (gameState?.gameStatus === "IN_PROGRESS") {
            battleHint.textContent = canAttackNow
                ? "Sua vez: clique no grid do oponente para atacar."
                : "Aguarde o turno do adversario para atacar.";
        } else if (gameState?.gameStatus === "FINISHED") {
            battleHint.textContent = "Partida encerrada.";
        } else {
            battleHint.textContent = "No ataque: bolinha branca para agua e X destacado para hit.";
        }
    }
}

function processAttackResult(eventData) {
    const x = Number(eventData?.x);
    const y = Number(eventData?.y);
    if (!Number.isInteger(x) || !Number.isInteger(y)) {
        return;
    }

    const key = toAttackKey(x, y);
    const result = normalizeAttackResult(eventData?.result);
    if (!result) {
        return;
    }

    const isPendingAttack = pendingAttacks.has(key);
    const isGameOver = eventData?.gameOver === true;

    let wasMyAttack = isPendingAttack;

    if (!wasMyAttack && isGameOver) {
        wasMyAttack = eventData?.winner === playerName;
    }

    if (!wasMyAttack && !isGameOver) {
        wasMyAttack = eventData?.currentPlayer !== playerName;
    }

    if (wasMyAttack) {
        markAttackOnBoard(opponentBoard, y, x, result);
        myAttackResults.set(key, result);
    } else {
        markAttackOnBoard(board, y, x, result);
        attacksOnMyBoard.add(key);
    }

    pendingAttacks.delete(key);
    
    console.log("Attack processed:", { x, y, result, wasMyAttack, boardType: wasMyAttack ? "opponent" : "player" });
}

function updateReadyButtonState(gameStatus = currentGameState?.gameStatus) {
    if (!readyButton) {
        return;
    }

    readyButton.classList.remove("ships-control-btn--waiting");

    if (gameStatus === "WAITING_FOR_PLAYERS") {
        readyButton.disabled = true;
        readyButton.textContent = "Aguardando J2";
        return;
    }

    if (gameStatus === "IN_PROGRESS" || gameStatus === "FINISHED") {
        readyButton.disabled = true;
        readyButton.textContent = "Pronto";
        return;
    }

    if (isReadySubmitting) {
        readyButton.disabled = true;
        readyButton.classList.add("ships-control-btn--waiting");
        readyButton.textContent = "Enviando...";
        return;
    }

    if (isPlayerReadyConfirmed) {
        readyButton.disabled = true;
        readyButton.classList.add("ships-control-btn--waiting");
        readyButton.textContent = "Aguardando rival";
        return;
    }

    if (!areAllShipsPlaced()) {
        readyButton.disabled = true;
        readyButton.textContent = "Posicione todos";
        return;
    }

    readyButton.disabled = false;
    readyButton.textContent = "Pronto";
}

function showWaitingWarning() {
    const warningModal = document.getElementById("warningModal");
    const warningModalClose = document.getElementById("warningModalClose");

    if (!warningModal) return;

    warningModal.classList.remove("modal--hidden");
    warningModal.setAttribute("aria-hidden", "false");

    const closeModal = () => {
        warningModal.classList.add("modal--hidden");
        warningModal.setAttribute("aria-hidden", "true");
        warningModalClose?.removeEventListener("click", closeModal);
    };

    warningModalClose?.addEventListener("click", closeModal);
}

if (shipsArea) {
    shipsArea.addEventListener("click", event => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) {
            return;
        }

        const ship = target.closest(".ship-img");
        if (!ship || !isShipAvailable(ship)) {
            return;
        }

        if (currentGameState?.gameStatus === "WAITING_FOR_PLAYERS") {
            showWaitingWarning();
            return;
        }

        if (isPlacementLocked()) {
            return;
        }

        setTrashMode(false);
        selectShip(ship);
    });
}

if (trashModeButton) {
    trashModeButton.addEventListener("click", () => {
        if (currentGameState?.gameStatus === "WAITING_FOR_PLAYERS") {
            showWaitingWarning();
            return;
        }

        if (isPlacementLocked()) {
            return;
        }

        setTrashMode(!isTrashModeActive);
    });
}

if (rotateSelectedShipButton) {
    rotateSelectedShipButton.addEventListener("click", () => {
        if (currentGameState?.gameStatus === "WAITING_FOR_PLAYERS") {
            showWaitingWarning();
            return;
        }

        if (isPlacementLocked()) {
            return;
        }

        if (!selectedShip) {
            const firstAvailable = shipsArea?.querySelector('.ship-img[data-placed="false"]');
            if (firstAvailable instanceof HTMLElement) {
                selectShip(firstAvailable);
            }
        }

        if (!selectedShip || !isShipAvailable(selectedShip)) {
            return;
        }

        const current = selectedShip.getAttribute("data-orientation") || "horizontal";
        const next = current === "horizontal" ? "vertical" : "horizontal";
        applyShipOrientation(selectedShip, next);
    });
}

function selectShip(shipElement) {
    if (selectedShip) {
        selectedShip.classList.remove("ship-img--selected");
    }

    selectedShip = shipElement;
    selectedShip.classList.add("ship-img--selected");
}

function setTrashMode(active) {
    isTrashModeActive = active;

    if (!trashModeButton) {
        return;
    }

    trashModeButton.classList.toggle("ships-control-btn--active", isTrashModeActive);
    trashModeButton.setAttribute("aria-pressed", isTrashModeActive ? "true" : "false");
}

function cacheShipPreviewSize(shipElement) {
    if (shipElement.dataset.baseWidth && shipElement.dataset.baseHeight) {
        return;
    }

    const rect = shipElement.getBoundingClientRect();
    const baseWidth = Math.round(rect.width || shipElement.naturalWidth || 0);
    const baseHeight = Math.round(rect.height || shipElement.naturalHeight || 0);

    if (baseWidth > 0) {
        shipElement.dataset.baseWidth = String(baseWidth);
    }

    if (baseHeight > 0) {
        shipElement.dataset.baseHeight = String(baseHeight);
    }
}

function applyShipOrientation(shipElement, orientation) {
    cacheShipPreviewSize(shipElement);

    const frame = shipElement.parentElement;
    const baseWidth = Number(shipElement.dataset.baseWidth);
    const baseHeight = Number(shipElement.dataset.baseHeight);

    shipElement.setAttribute("data-orientation", orientation);

    if (!frame || !baseWidth || !baseHeight) {
        shipElement.style.transform = orientation === "vertical" ? "rotate(90deg)" : "rotate(0deg)";
        return;
    }

    if (orientation === "vertical") {
        frame.style.width = `${baseHeight}px`;
        frame.style.height = `${baseWidth}px`;
        shipElement.style.width = `${baseWidth}px`;
        shipElement.style.height = `${baseHeight}px`;
        shipElement.style.transform = "translate(-50%, -50%) rotate(90deg)";
    } else {
        frame.style.width = `${baseWidth}px`;
        frame.style.height = `${baseHeight}px`;
        shipElement.style.width = "100%";
        shipElement.style.height = "100%";
        shipElement.style.transform = "none";
    }
}

function placeShipOnBoard(shipElement, cell) {
    if (!board || !shipElement || !cell) {
        return false;
    }

    const shipType = shipElement.getAttribute("data-ship");
    if (!shipType || placedShips.has(shipType)) {
        return false;
    }

    const orientation = shipElement.getAttribute("data-orientation") || "horizontal";
    const size = SHIP_SIZES[shipType] || 1;
    const startRow = Number(cell.dataset.row);
    const startCol = Number(cell.dataset.col);
    const targetCells = getTargetCells(startRow, startCol, size, orientation);

    if (!targetCells || hasCollision(targetCells)) {
        return false;
    }

    const firstCell = getBoardCell(targetCells[0].row, targetCells[0].col);
    const lastCell = getBoardCell(
        targetCells[targetCells.length - 1].row,
        targetCells[targetCells.length - 1].col
    );

    if (!firstCell || !lastCell) {
        return false;
    }

    const boardRect = board.getBoundingClientRect();
    const firstRect = firstCell.getBoundingClientRect();
    const lastRect = lastCell.getBoundingClientRect();

    const overlay = document.createElement("div");
    overlay.className = `board-ship board-ship--${orientation}`;
    overlay.setAttribute("data-ship", shipType);
    overlay.style.left = `${firstRect.left - boardRect.left}px`;
    overlay.style.top = `${firstRect.top - boardRect.top}px`;
    const overlayWidth = lastRect.right - firstRect.left;
    const overlayHeight = lastRect.bottom - firstRect.top;

    overlay.style.width = `${overlayWidth}px`;
    overlay.style.height = `${overlayHeight}px`;

    const shipImage = document.createElement("img");
    shipImage.className = "board-ship-image";
    shipImage.src = shipElement.getAttribute("src") || "";
    shipImage.alt = "";
    shipImage.setAttribute("aria-hidden", "true");
    shipImage.draggable = false;
    if (orientation === "vertical") {
        shipImage.classList.add("board-ship-image--vertical");
        shipImage.style.width = `${overlayHeight}px`;
        shipImage.style.height = `${overlayWidth}px`;
    }
    overlay.appendChild(shipImage);

    board.appendChild(overlay);

    targetCells.forEach(targetCell => {
        occupiedCells.add(cellKey(targetCell.row, targetCell.col));
        const segment = getBoardCell(targetCell.row, targetCell.col);
        if (segment) {
            segment.classList.add("has-ship");
        }
    });

    placedShips.set(shipType, targetCells);
    updateLocalShipPlacement(shipType, targetCells);
    persistLocalShipsState();
    shipElement.dataset.placed = "true";
    shipElement.style.opacity = 0.5;
    shipElement.classList.remove("ship-img--selected");
    if (selectedShip === shipElement) {
        selectedShip = null;
    }

    updateReadyButtonState();

    return true;
}

function getPlacedShipTypeByCell(row, col) {
    for (const [shipType, cells] of placedShips.entries()) {
        if (cells.some(placedCell => placedCell.row === row && placedCell.col === col)) {
            return shipType;
        }
    }

    return null;
}

function removePlacedShip(shipType) {
    const cells = placedShips.get(shipType);
    if (!cells || cells.length === 0) {
        return false;
    }

    const overlay = board?.querySelector(`.board-ship[data-ship="${shipType}"]`);
    if (overlay) {
        overlay.remove();
    }

    cells.forEach(({ row, col }) => {
        occupiedCells.delete(cellKey(row, col));
        const boardCell = getBoardCell(row, col);
        if (boardCell) {
            boardCell.classList.remove("has-ship");
        }
    });

    placedShips.delete(shipType);
    updateLocalShipPlacement(shipType, []);
    persistLocalShipsState();

    const shipElement = shipsArea?.querySelector(`.ship-img[data-ship="${shipType}"]`);
    if (shipElement instanceof HTMLElement) {
        shipElement.dataset.placed = "false";
        shipElement.style.opacity = "";
        selectShip(shipElement);
    }

    updateReadyButtonState();

    return true;
}

function cellKey(row, col) {
    return `${row}-${col}`;
}

function getBoardCell(row, col) {
    return board?.querySelector(`.board-cell[data-row="${row}"][data-col="${col}"]`) || null;
}

function getTargetCells(startRow, startCol, size, orientation) {
    const cells = [];

    for (let offset = 0; offset < size; offset += 1) {
        const row = orientation === "vertical" ? startRow + offset : startRow;
        const col = orientation === "horizontal" ? startCol + offset : startCol;

        if (row > 9 || col > 9) {
            return null;
        }

        cells.push({ row, col });
    }

    return cells;
}

function hasCollision(cells) {
    return cells.some(({ row, col }) => occupiedCells.has(cellKey(row, col)));
}

function toStoredCells(cells) {
    return Array.isArray(cells)
        ? cells
            .map(cell => ({ row: Number(cell.row), col: Number(cell.col) }))
            .filter(cell => Number.isInteger(cell.row) && Number.isInteger(cell.col))
        : [];
}

function updateLocalShipPlacement(shipType, cells) {
    const ship = localShipsState.get(shipType);
    if (!ship) {
        return;
    }

    const normalizedCells = toStoredCells(cells);
    ship.cells = normalizedCells;
    ship.placed = normalizedCells.length > 0;
    ship.hits = 0;
    ship.destroyed = false;
}

function getLocalShipsStorageKey() {
    return `localShipsState:${gameId}:${playerName}`;
}

function persistLocalShipsState() {
    const payload = Array.from(localShipsState.values());
    sessionStorage.setItem(getLocalShipsStorageKey(), JSON.stringify(payload));
}

function hydrateLocalShipsState() {
    const raw = sessionStorage.getItem(getLocalShipsStorageKey());
    if (!raw) {
        return;
    }

    try {
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) {
            return;
        }

        parsed.forEach(item => {
            if (!item || typeof item !== "object") {
                return;
            }

            const key = typeof item.key === "string" ? item.key : null;
            if (!key || !localShipsState.has(key)) {
                return;
            }

            const ship = localShipsState.get(key);
            const cells = toStoredCells(item.cells);
            ship.cells = cells;
            ship.placed = cells.length > 0;
            ship.hits = Math.max(0, Math.min(Number(item.hits) || 0, ship.size));
            ship.destroyed = ship.hits >= ship.size || item.destroyed === true;
        });
    } catch (error) {
        console.error("Falha ao restaurar estado local de navios:", error);
    }
}

function applyAttackToLocalShips(eventData) {
    if (!eventData || (eventData.type !== "ATTACK_RESULT" && eventData.type !== "attack_result")) {
        return;
    }

    const wasAttackOnMyBoard =
        eventData.currentPlayer === playerName ||
        (eventData.gameOver === true && eventData.winner && eventData.winner !== playerName);
    if (!wasAttackOnMyBoard) {
        return;
    }

    const result = String(eventData.result || "").toUpperCase();
    if (result !== "HIT" && result !== "DESTROYED") {
        return;
    }

    const x = Number(eventData.x);
    const y = Number(eventData.y);
    if (!Number.isInteger(x) || !Number.isInteger(y)) {
        return;
    }

    for (const ship of localShipsState.values()) {
        const matchesCell = ship.cells.some(cell => cell.row === y && cell.col === x);
        if (!matchesCell || ship.destroyed) {
            continue;
        }

        ship.hits = Math.min(ship.hits + 1, ship.size);
        ship.destroyed = ship.hits >= ship.size;
        persistLocalShipsState();
        return;
    }
}

function buildLocalHudShips() {
    return Array.from(localShipsState.values()).map(ship => ({
        name: ship.name,
        size: ship.size,
        hits: ship.hits,
        destroyed: ship.destroyed,
        placed: ship.placed
    }));
}

function getShipsForHud(gameState) {
    const apiShips = Array.isArray(gameState?.myShips) ? gameState.myShips : [];
    if (apiShips.length > 0) {
        return apiShips;
    }

    return buildLocalHudShips();
}

function getShipsRemainingForHud(gameState, ships) {
    if (Array.isArray(gameState?.myShips) && gameState.myShips.length > 0) {
        return ships.filter(ship => ship?.destroyed !== true).length;
    }

    if (gameState?.gameStatus === "WAITING_FOR_PLAYERS") {
        return null;
    }

    if (gameState?.gameStatus === "PLACING_SHIPS") {
        return Object.keys(SHIP_SIZES).length;
    }

    const placedShipsCount = ships.filter(ship => ship?.placed === true).length;
    if (placedShipsCount === 0) {
        return null;
    }

    return ships.filter(ship => ship?.placed === true && ship?.destroyed !== true).length;
}

function getShipStatusLabel(ship, gameStatus) {
    if (ship?.destroyed === true) {
        return "destruido";
    }

    if (ship?.placed === false && gameStatus === "PLACING_SHIPS") {
        return "nao posicionado";
    }

    return "ativo";
}
const hudPlayerName = document.getElementById("hudPlayerName");
const hudGameId = document.getElementById("hudGameId");
const hudGameStatus = document.getElementById("hudGameStatus");
const hudMyTurn = document.getElementById("hudMyTurn");
const hudShipsRemaining = document.getElementById("hudShipsRemaining");
const hudMyAttacks = document.getElementById("hudMyAttacks");
const hudShips = document.getElementById("hudShips");
const waitingMessage = document.getElementById("waitingMessage");
const socketStatus = document.getElementById("socketStatus");
const copyGameId = document.getElementById("copyGameId");
const phaseTransition = document.getElementById("phaseTransition");
const phaseTransitionTitle = document.getElementById("phaseTransitionTitle");
const phaseTransitionSubtitle = document.getElementById("phaseTransitionSubtitle");
const battleHint = document.getElementById("battleHint");
let currentGameState = null;
let lastRenderedGameStatus = null;
let phaseTransitionTimeoutId = null;
let lastRenderedMyTurn = null;
let myTurnBlinkTimeoutId = null;

const gameLog = document.getElementById("gameLog");

const playerName = sessionStorage.getItem("playerName");
const gameId = sessionStorage.getItem("gameId");
const savedState = sessionStorage.getItem("gameState");

if (!playerName || !gameId) {
    window.location.href = "index.html";
} else {
    hydrateLocalShipsState();
    buildBoard();

    if (savedState) {
        try {
            renderHud(JSON.parse(savedState), playerName);
        } catch (error) {
            console.error(error);
        }
    }

    openWebSocket();
    bindCopyGameId();
}

function openWebSocket() {
    gameSocket = new WebSocket(
        `ws://localhost:8080/ws/game?gameId=${encodeURIComponent(gameId)}&playerName=${encodeURIComponent(playerName)}`
    );

    gameSocket.addEventListener("open", () => {
        socketStatus.textContent = "Conectado";
        console.log("WebSocket conectado");
        updateReadyButtonState();
    });

    gameSocket.addEventListener("message", event => {
        try {
            console.log("Mensagem recebida:", event.data);

            const data = JSON.parse(event.data);

            console.log("WebSocket message:", data);

            if (data.type === "GAME_STATE_UPDATED") {
                fetchGameState();
                return;
            }

            if (data.type === "PLAYER_READY") {
                if (data.playerName === playerName) {
                    isPlayerReadyConfirmed = true;
                    isReadySubmitting = false;
                }
                updateReadyButtonState();
                fetchGameState();
                return;
            }

            if (data.type === "GAME_START") {
                isReadySubmitting = false;
                isPlayerReadyConfirmed = true;
                updateReadyButtonState();
                fetchGameState();
                return;
            }

            if (data.type === "ATTACK_RESULT") {
                processAttackResult(data);
                applyAttackToLocalShips(data);
                if (currentGameState) {
                    renderHud(currentGameState, playerName);
                }
                return;
            }

            if (data.type === "ERROR") {
                isReadySubmitting = false;
                pendingAttacks.clear();
                updateReadyButtonState();
                console.error("Erro do servidor:", data.message);
                return;
            }

            if (data.gameStatus && data.player1Name && data.player2Name) {
                renderHud(data, playerName);
            }

        } catch (error) {
            console.error(error);
        }
    });

    gameSocket.addEventListener("close", () => {
        socketStatus.textContent = "Desconectado";
        updateReadyButtonState();
    });

    gameSocket.addEventListener("error", () => {
        socketStatus.textContent = "Erro";
        updateReadyButtonState();
    });
}


function buildBoard() {
    const buildGrid = boardElement => {
        if (!boardElement) {
            return;
        }

        boardElement.innerHTML = "";
        for (let row = 0; row < 10; row += 1) {
            for (let col = 0; col < 10; col += 1) {
                const cell = document.createElement("button");
                cell.type = "button";
                cell.className = "board-cell";
                cell.dataset.row = row.toString();
                cell.dataset.col = col.toString();
                cell.setAttribute("role", "gridcell");
                cell.setAttribute("aria-label", `Linha ${row + 1}, Coluna ${col + 1}`);
                boardElement.appendChild(cell);
            }
        }
    };

    buildGrid(board);
    buildGrid(opponentBoard);
}

function showPhaseTransition(title, subtitle) {
    if (!phaseTransition || !phaseTransitionTitle || !phaseTransitionSubtitle) {
        return;
    }

    phaseTransitionTitle.textContent = title;
    phaseTransitionSubtitle.textContent = subtitle;
    phaseTransition.setAttribute("aria-hidden", "false");
    phaseTransition.classList.remove("phase-transition--visible");
    void phaseTransition.offsetWidth;
    phaseTransition.classList.add("phase-transition--visible");

    if (phaseTransitionTimeoutId) {
        window.clearTimeout(phaseTransitionTimeoutId);
    }

    phaseTransitionTimeoutId = window.setTimeout(() => {
        phaseTransition.classList.remove("phase-transition--visible");
        phaseTransition.setAttribute("aria-hidden", "true");
        phaseTransitionTimeoutId = null;
    }, 2300);
}

function maybeShowPhaseTransition(previousStatus, gameState) {
    const currentStatus = gameState?.gameStatus;

    if (!previousStatus || !currentStatus || previousStatus === currentStatus) {
        return;
    }

    if (previousStatus === "WAITING_FOR_PLAYERS" && currentStatus === "PLACING_SHIPS") {
        showPhaseTransition("Fase de posicionamento", "Os dois jogadores entraram. Posicione seus navios.");
        return;
    }

    if (previousStatus === "PLACING_SHIPS" && currentStatus === "IN_PROGRESS") {
        blinkMyTurnField();
        showPhaseTransition(
            "Fase de ataque iniciada",
            gameState.myTurn ? "Sua vez de atacar." : "Aguarde o ataque do adversario."
        );
        return;
    }

    if (currentStatus === "FINISHED") {
        showPhaseTransition("Jogo finalizado", gameState.winner ? `Vencedor: ${gameState.winner}` : "Partida encerrada.");
    }
}

function blinkMyTurnField() {
    if (!hudMyTurn) {
        return;
    }

    hudMyTurn.classList.remove("hud-value--blink-turn");
    void hudMyTurn.offsetWidth;
    hudMyTurn.classList.add("hud-value--blink-turn");

    if (myTurnBlinkTimeoutId) {
        window.clearTimeout(myTurnBlinkTimeoutId);
    }

    myTurnBlinkTimeoutId = window.setTimeout(() => {
        hudMyTurn.classList.remove("hud-value--blink-turn");
        myTurnBlinkTimeoutId = null;
    }, 950);
}

function renderHud(gameState, displayName) {
    const previousStatus = lastRenderedGameStatus;
    const nextStatus = gameState?.gameStatus || null;

    currentGameState = gameState || null;
    maybeShowPhaseTransition(previousStatus, gameState);
    lastRenderedGameStatus = nextStatus;

    if (gameState.gameStatus === "IN_PROGRESS" || gameState.gameStatus === "FINISHED") {
        isPlayerReadyConfirmed = true;
        isReadySubmitting = false;
    }

    hudPlayerName.textContent = displayName;
    hudGameId.textContent = gameState.gameId ? `#${gameState.gameId}` : "#-";
    let statusLabel = "-";
    if (gameState.gameStatus === "WAITING_FOR_PLAYERS") {
        statusLabel = "Aguardando outro jogador";
    } else if (gameState.gameStatus === "PLACING_SHIPS") {
        statusLabel = "Posicione seus navios";
    } else if (gameState.gameStatus === "IN_PROGRESS") {
        statusLabel = "Batalha em andamento";
    } else if (gameState.gameStatus === "FINISHED") {
        statusLabel = gameState.winner ? `Vitória de ${gameState.winner}` : "Jogo finalizado";
    } else if (gameState.gameStatus) {
        statusLabel = gameState.gameStatus;
    }
    hudGameStatus.textContent = statusLabel;
    const currentMyTurn = typeof gameState.myTurn === "boolean" ? gameState.myTurn : null;
    hudMyTurn.textContent =
        currentMyTurn === null ? "-" : (currentMyTurn ? "Sim" : "Nao");

    if (lastRenderedMyTurn !== null && currentMyTurn !== null && currentMyTurn !== lastRenderedMyTurn) {
        blinkMyTurnField();
    }

    lastRenderedMyTurn = currentMyTurn;

    hudMyAttacks.textContent = myAttackResults.size.toString();

    if (waitingMessage) {
        waitingMessage.hidden = gameState.gameStatus !== "WAITING_FOR_PLAYERS";
    }

    updateBattleUi(gameState);

    updateReadyButtonState(gameState.gameStatus);

    hudShips.innerHTML = "";
    const ships = getShipsForHud(gameState);
    const shipsRemaining = getShipsRemainingForHud(gameState, ships);
    hudShipsRemaining.textContent = shipsRemaining === null ? "-" : shipsRemaining.toString();

    if (ships.length === 0) {
        const emptyRow = document.createElement("tr");
        const emptyCell = document.createElement("td");
        emptyCell.colSpan = 4;
        emptyCell.textContent = "Sem navios";
        emptyRow.appendChild(emptyCell);
        hudShips.appendChild(emptyRow);
        return;
    }

    ships.forEach(ship => {
        const row = document.createElement("tr");
        const shipName = ship?.name ?? "Desconhecido";
        const size = typeof ship?.size === "number" ? ship.size : "-";
        const hits = typeof ship?.hits === "number" ? ship.hits : "-";
        const destroyed = getShipStatusLabel(ship, gameState.gameStatus);

        row.innerHTML = `
            <td>${shipName}</td>
            <td>${size}</td>
            <td>${hits}</td>
            <td>${destroyed}</td>
        `;
        hudShips.appendChild(row);
    });
}

function getPlacementOrientation(cells) {
    if (!Array.isArray(cells) || cells.length < 2) {
        return "HORIZONTAL";
    }

    return cells[0].row === cells[1].row ? "HORIZONTAL" : "VERTICAL";
}

function sendGameMessage(payload) {
    if (!gameSocket || gameSocket.readyState !== WebSocket.OPEN) {
        throw new Error("WebSocket nao conectado");
    }

    gameSocket.send(JSON.stringify(payload));
}

function sendAttack(row, col) {
    const x = col;
    const y = row;
    const key = toAttackKey(x, y);

    if (myAttackResults.has(key) || pendingAttacks.has(key)) {
        return;
    }

    pendingAttacks.add(key);

    try {
        sendGameMessage({
            type: "ATTACK",
            gameId,
            playerName,
            x,
            y
        });
    } catch (error) {
        pendingAttacks.delete(key);
        throw error;
    }
}

function sendShipsPlacementAndReady() {
    if (!areAllShipsPlaced()) {
        return;
    }

    for (const [shipType, cells] of placedShips.entries()) {
        const firstCell = cells[0];
        if (!firstCell) {
            continue;
        }

        sendGameMessage({
            type: "PLACE_SHIP",
            gameId,
            playerName,
            shipType,
            size: SHIP_SIZES[shipType],
            x: firstCell.col,
            y: firstCell.row,
            orientation: getPlacementOrientation(cells)
        });
    }

    sendGameMessage({
        type: "PLAYER_READY",
        gameId,
        playerName
    });
}

function bindCopyGameId() {
    if (!copyGameId) {
        return;
    }

    copyGameId.addEventListener("click", async () => {
        const rawGameId = (hudGameId.textContent || "").replace("#", "").trim();
        if (!rawGameId || rawGameId === "-") {
            return;
        }

        try {
            if (navigator.clipboard?.writeText) {
                await navigator.clipboard.writeText(rawGameId);
            } else {
                const fallback = document.createElement("textarea");
                fallback.value = rawGameId;
                fallback.setAttribute("readonly", "");
                fallback.style.position = "absolute";
                fallback.style.left = "-9999px";
                document.body.appendChild(fallback);
                fallback.select();
                document.execCommand("copy");
                document.body.removeChild(fallback);
            }

            showCopyFeedback("Copiado!");
        } catch (error) {
            console.error(error);
            showCopyFeedback("Falha ao copiar");
        }
    });
}

function showCopyFeedback(message) {
    if (!copyGameIdFeedback) {
        return;
    }
    copyGameIdFeedback.textContent = message;
    window.setTimeout(() => {
        copyGameIdFeedback.textContent = "";
    }, 1500);
}

if (board) {
    board.addEventListener("click", event => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) {
            return;
        }
        if (!target.classList.contains("board-cell")) {
            return;
        }

        const gameStatus = currentGameState?.gameStatus;

        if (gameStatus === "WAITING_FOR_PLAYERS") {
            showWaitingWarning();
            return;
        }

        if (gameStatus === "PLACING_SHIPS") {
            if (isPlacementLocked()) {
                return;
            }

            if (isTrashModeActive) {
                const row = Number(target.dataset.row);
                const col = Number(target.dataset.col);
                const placedShipType = getPlacedShipTypeByCell(row, col);

                if (placedShipType) {
                    removePlacedShip(placedShipType);
                    setTrashMode(false);
                    if (currentGameState) {
                        renderHud(currentGameState, playerName);
                    }
                }

                return;
            }

            if (selectedShip && isShipAvailable(selectedShip)) {
                const placed = placeShipOnBoard(selectedShip, target);
                if (placed && currentGameState) {
                    renderHud(currentGameState, playerName);
                }
            }

            return;
        }

        if (gameStatus !== "IN_PROGRESS") {
            return;
        }
    });
}

if (opponentBoard) {
    opponentBoard.addEventListener("click", event => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) {
            return;
        }

        if (!target.classList.contains("board-cell")) {
            return;
        }

        if (currentGameState?.gameStatus !== "IN_PROGRESS") {
            return;
        }

        if (currentGameState?.myTurn !== true) {
            return;
        }

        const row = Number(target.dataset.row);
        const col = Number(target.dataset.col);
        if (!Number.isInteger(row) || !Number.isInteger(col)) {
            return;
        }

        try {
            sendAttack(row, col);
        } catch (error) {
            console.error("Falha ao enviar ataque:", error);
        }
    });
}

if (readyButton) {
    readyButton.addEventListener("click", () => {
        if (currentGameState?.gameStatus !== "PLACING_SHIPS") {
            return;
        }

        if (isPlacementLocked() || !areAllShipsPlaced()) {
            updateReadyButtonState();
            return;
        }

        try {
            isReadySubmitting = true;
            updateReadyButtonState();
            sendShipsPlacementAndReady();
        } catch (error) {
            isReadySubmitting = false;
            console.error("Falha ao enviar status de pronto:", error);
            updateReadyButtonState();
        }
    });
}


async function fetchGameState() {
    try {
        const response = await fetch(
            `http://localhost:8080/api/game/${gameId}?playerName=${playerName}`
        );

        if (!response.ok) {
            console.error("Erro ao buscar estado:", response.status);
            return;
        }

        const state = await response.json();

        sessionStorage.setItem("gameState", JSON.stringify(state));
        renderHud(state, playerName);

    } catch (error) {
        console.error("Erro ao buscar estado:", error);
    }
}