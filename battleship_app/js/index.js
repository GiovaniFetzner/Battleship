const createGameForm = document.getElementById("createGameForm");
const playerNameInput = document.getElementById("playerName");
const gameIdInput = document.getElementById("gameId");
const createGameBtn = document.getElementById("createGameBtn");
const formStatus = document.getElementById("formStatus");
const refreshGamesBtn = document.getElementById("refreshGamesBtn");
const availableGamesStatus = document.getElementById("availableGamesStatus");
const availableGamesList = document.getElementById("availableGamesList");
const joinRoomModal = document.getElementById("joinRoomModal");
const joinRoomModalGameText = document.getElementById("joinRoomModalGameText");
const joinRoomPlayerName = document.getElementById("joinRoomPlayerName");
const confirmJoinRoomBtn = document.getElementById("confirmJoinRoomBtn");
const cancelJoinRoomBtn = document.getElementById("cancelJoinRoomBtn");

const API_BASE_URL = window.BattleshipConfig.resolveApiBaseUrl();

let isSubmitting = false;
let pendingJoinGameId = "";

createGameForm.addEventListener("submit", event => {
    event.preventDefault();
    createOrJoinGame();
});

refreshGamesBtn.addEventListener("click", fetchAvailableGames);

confirmJoinRoomBtn.addEventListener("click", handleConfirmJoinRoom);
cancelJoinRoomBtn.addEventListener("click", closeJoinRoomModal);
joinRoomModal.querySelector(".modal-overlay").addEventListener("click", closeJoinRoomModal);

joinRoomPlayerName.addEventListener("keydown", event => {
    if (event.key === "Enter") {
        event.preventDefault();
        handleConfirmJoinRoom();
    }
});

fetchAvailableGames();

async function createOrJoinGame(selectedGameId = "") {
    if (isSubmitting) {
        return;
    }

    const playerName = playerNameInput.value.trim() || "Player1";
    const gameId = selectedGameId || gameIdInput.value.trim();

    setSubmissionState(true);

    if (selectedGameId) {
        gameIdInput.value = selectedGameId;
    }

    formStatus.textContent = gameId ? "Entrando no jogo..." : "Criando jogo...";

    let endpoint = API_BASE_URL;
    let payload = { playerName };

    if (gameId) {
        endpoint = `${API_BASE_URL}/${encodeURIComponent(gameId)}/join`;
    }

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error("Erro na requisição");
        }

        const data = await response.json();

        console.log("Resposta recebida do backend:", data);

        const resolvedGameId = data.gameId || gameId;

        if (!resolvedGameId) {
            throw new Error("GameId não retornado");
        }

        // Salva playerName e gameId
        sessionStorage.setItem("playerName", playerName);
        sessionStorage.setItem("gameId", resolvedGameId);
        sessionStorage.setItem("gameState", JSON.stringify(data));

        window.location.href = "game.html";

    } catch (err) {
        console.error(err);
        formStatus.textContent = gameId
            ? "Erro ao entrar no jogo."
            : "Erro ao criar jogo.";
    } finally {
        setSubmissionState(false);
    }
}

async function fetchAvailableGames() {
    availableGamesStatus.textContent = "Carregando partidas...";
    refreshGamesBtn.disabled = true;

    try {
        const response = await fetch(window.BattleshipConfig.resolveApiUrl("available"));

        if (!response.ok) {
            throw new Error("Erro ao buscar partidas disponíveis");
        }

        const availableGames = await response.json();
        renderAvailableGames(availableGames);
    } catch (err) {
        console.error(err);
        availableGamesList.innerHTML = "";
        availableGamesStatus.textContent = "Nao foi possivel carregar as partidas.";
    } finally {
        refreshGamesBtn.disabled = isSubmitting;
    }
}

function renderAvailableGames(availableGames) {
    availableGamesList.innerHTML = "";

    if (!Array.isArray(availableGames) || availableGames.length === 0) {
        availableGamesStatus.textContent = "Nenhuma partida aguardando jogador.";
        return;
    }

    const fragment = document.createDocumentFragment();

    availableGames.forEach(game => {
        const item = document.createElement("li");
        item.className = "available-games-item";

        const meta = document.createElement("div");
        meta.className = "available-games-item-meta";

        const idText = document.createElement("p");
        idText.className = "available-games-item-id";
        idText.textContent = `Sala ${game.gameId}`;

        const hostText = document.createElement("p");
        hostText.className = "available-games-item-host";
        hostText.textContent = `Criada por ${game.createdByPlayer}`;

        const joinBtn = document.createElement("button");
        joinBtn.type = "button";
        joinBtn.className = "join-game-btn";
        joinBtn.textContent = "Selecionar";
        joinBtn.disabled = isSubmitting;
        joinBtn.addEventListener("click", event => {
            event.stopPropagation();
            openJoinRoomModal(game.gameId, game.createdByPlayer);
        });

        item.addEventListener("click", () => {
            openJoinRoomModal(game.gameId, game.createdByPlayer);
        });

        meta.appendChild(idText);
        meta.appendChild(hostText);
        item.appendChild(meta);
        item.appendChild(joinBtn);

        fragment.appendChild(item);
    });

    availableGamesList.appendChild(fragment);
    availableGamesStatus.textContent = `${availableGames.length} partida(s) aguardando jogador.`;
}

function setSubmissionState(isLoading) {
    isSubmitting = isLoading;

    createGameBtn.disabled = isLoading;
    refreshGamesBtn.disabled = isLoading;

    const joinButtons = availableGamesList.querySelectorAll(".join-game-btn");
    joinButtons.forEach(button => {
        button.disabled = isLoading;
    });
}

function openJoinRoomModal(gameId, createdByPlayer) {
    if (isSubmitting) {
        return;
    }

    pendingJoinGameId = gameId;

    const hostLabel = createdByPlayer || "jogador";
    joinRoomModalGameText.textContent = `Sala ${gameId} criada por ${hostLabel}.\nInforme seu nome para entrar.`;

    const currentName = playerNameInput.value.trim();
    joinRoomPlayerName.value = currentName;

    joinRoomModal.classList.remove("modal--hidden");
    joinRoomModal.setAttribute("aria-hidden", "false");

    setTimeout(() => {
        joinRoomPlayerName.focus();
        joinRoomPlayerName.select();
    }, 20);
}

function closeJoinRoomModal() {
    pendingJoinGameId = "";
    joinRoomModal.classList.add("modal--hidden");
    joinRoomModal.setAttribute("aria-hidden", "true");
}

function handleConfirmJoinRoom() {
    if (!pendingJoinGameId || isSubmitting) {
        return;
    }

    const typedName = joinRoomPlayerName.value.trim();

    if (typedName) {
        playerNameInput.value = typedName;
    }

    const gameId = pendingJoinGameId;
    closeJoinRoomModal();
    createOrJoinGame(gameId);
}
