// Rotação dos navios na área de drag and drop
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".rotate-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const shipImg = btn.parentElement.querySelector(".ship-img");
            if (!shipImg) return;
            const current = shipImg.getAttribute("data-orientation") || "horizontal";
            const next = current === "horizontal" ? "vertical" : "horizontal";
            shipImg.setAttribute("data-orientation", next);
            shipImg.style.transform = next === "vertical" ? "rotate(90deg)" : "rotate(0deg)";
        });
    });
});
const board = document.getElementById("board");
const shipsArea = document.getElementById("shipsArea");
// Drag and Drop dos navios
let draggedShip = null;

if (shipsArea) {
    shipsArea.addEventListener("dragstart", (e) => {
        if (e.target.classList.contains("ship-img")) {
            draggedShip = e.target;
        }
    });
    shipsArea.addEventListener("dragend", () => {
        draggedShip = null;
    });
}

if (board) {
    board.addEventListener("dragover", (e) => {
        e.preventDefault();
    });
    board.addEventListener("drop", (e) => {
        e.preventDefault();
        if (!draggedShip) return;
        const cell = e.target.closest(".board-cell");
        if (!cell) return;
        // Clona o navio e mantém orientação
        const clone = draggedShip.cloneNode(true);
        const orientation = draggedShip.getAttribute("data-orientation") || "horizontal";
        clone.setAttribute("data-orientation", orientation);
        clone.style.transform = orientation === "vertical" ? "rotate(90deg)" : "rotate(0deg)";
        cell.appendChild(clone);
        // Marca navio como usado
        draggedShip.style.opacity = 0.5;
        draggedShip.setAttribute("draggable", false);
    });
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

board.addEventListener("click", event => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
        return;
    }
    if (!target.classList.contains("board-cell")) {
        return;
    }
    target.classList.toggle("is-selected");
});


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