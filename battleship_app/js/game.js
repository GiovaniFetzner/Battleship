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
const shipsArea = document.getElementById("shipsArea");
const rotateSelectedShipButton = document.getElementById("rotateSelectedShip");
const trashModeButton = document.getElementById("trashModeButton");
let selectedShip = null;
let isTrashModeActive = false;
const SHIP_SIZES = {
    porta_avioes: 5,
    bombardeiro: 4,
    submarino: 3,
    lancha: 2
};
const occupiedCells = new Set();
const placedShips = new Map();

function isShipAvailable(shipElement) {
    return shipElement?.dataset.placed !== "true";
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

        setTrashMode(false);
        selectShip(ship);
    });
}

if (trashModeButton) {
    trashModeButton.addEventListener("click", () => {
        setTrashMode(!isTrashModeActive);
    });
}

if (rotateSelectedShipButton) {
    rotateSelectedShipButton.addEventListener("click", () => {
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
    shipElement.dataset.placed = "true";
    shipElement.style.opacity = 0.5;
    shipElement.classList.remove("ship-img--selected");
    if (selectedShip === shipElement) {
        selectedShip = null;
    }

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

    const shipElement = shipsArea?.querySelector(`.ship-img[data-ship="${shipType}"]`);
    if (shipElement instanceof HTMLElement) {
        shipElement.dataset.placed = "false";
        shipElement.style.opacity = "";
        selectShip(shipElement);
    }

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
let currentGameState = null;

const gameLog = document.getElementById("gameLog");

const playerName = sessionStorage.getItem("playerName");
const gameId = sessionStorage.getItem("gameId");
const savedState = sessionStorage.getItem("gameState");

if (!playerName || !gameId) {
    window.location.href = "index.html";
} else {
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
    const socket = new WebSocket(
        `ws://localhost:8080/ws/game?gameId=${encodeURIComponent(gameId)}&playerName=${encodeURIComponent(playerName)}`
    );

    socket.addEventListener("open", () => {
        socketStatus.textContent = "Conectado";
        console.log("WebSocket conectado");
    });

    socket.addEventListener("message", event => {
        try {
            console.log("Mensagem recebida:", event.data);

            const data = JSON.parse(event.data);

            console.log("WebSocket message:", data);

            if (data.type === "GAME_STATE_UPDATED") {
                fetchGameState();
                return;
            }

            if (data.gameStatus && data.player1Name && data.player2Name) {
                renderHud(data, playerName);
            }

        } catch (error) {
            console.error(error);
        }
    });

    socket.addEventListener("close", () => {
        socketStatus.textContent = "Desconectado";
    });

    socket.addEventListener("error", () => {
        socketStatus.textContent = "Erro";
    });
}


function buildBoard() {
    board.innerHTML = "";
    for (let row = 0; row < 10; row += 1) {
        for (let col = 0; col < 10; col += 1) {
            const cell = document.createElement("button");
            cell.type = "button";
            cell.className = "board-cell";
            cell.dataset.row = row.toString();
            cell.dataset.col = col.toString();
            cell.setAttribute("role", "gridcell");
            cell.setAttribute("aria-label", `Linha ${row + 1}, Coluna ${col + 1}`);
            board.appendChild(cell);
        }
    }
}

function renderHud(gameState, displayName) {
    currentGameState = gameState || null;

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
    hudMyTurn.textContent =
        typeof gameState.myTurn === "boolean" ? (gameState.myTurn ? "Sim" : "Nao") : "-";
    hudShipsRemaining.textContent =
        typeof gameState.myShipsRemaining === "number"
            ? gameState.myShipsRemaining.toString()
            : "-";
    hudMyAttacks.textContent =
        gameState.myAttacks === null || typeof gameState.myAttacks === "undefined"
            ? "Nenhum"
            : JSON.stringify(gameState.myAttacks);

    if (waitingMessage) {
        waitingMessage.hidden = gameState.gameStatus !== "WAITING_FOR_PLAYERS";
    }

    hudShips.innerHTML = "";
    const ships = Array.isArray(gameState.myShips) ? gameState.myShips : [];
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
        const destroyed = ship?.destroyed === true ? "destruido" : "ativo";

        row.innerHTML = `
            <td>${shipName}</td>
            <td>${size}</td>
            <td>${hits}</td>
            <td>${destroyed}</td>
        `;
        hudShips.appendChild(row);
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

        if (gameStatus === "PLACING_SHIPS") {
            if (isTrashModeActive) {
                const row = Number(target.dataset.row);
                const col = Number(target.dataset.col);
                const placedShipType = getPlacedShipTypeByCell(row, col);

                if (placedShipType) {
                    removePlacedShip(placedShipType);
                    setTrashMode(false);
                }

                return;
            }

            if (selectedShip && isShipAvailable(selectedShip)) {
                placeShipOnBoard(selectedShip, target);
            }

            return;
        }

        if (gameStatus !== "IN_PROGRESS") {
            return;
        }

        target.classList.toggle("is-selected");
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