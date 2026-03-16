const createGameForm = document.getElementById("createGameForm");
const playerNameInput = document.getElementById("playerName");
const gameIdInput = document.getElementById("gameId");
const createGameBtn = document.getElementById("createGameBtn");
const formStatus = document.getElementById("formStatus");

createGameForm.addEventListener("submit", event => {
    event.preventDefault();
    createOrJoinGame();
});

async function createOrJoinGame() {
    const playerName = playerNameInput.value.trim() || "Player1";
    const gameId = gameIdInput.value.trim();

    createGameBtn.disabled = true;
    formStatus.textContent = gameId ? "Entrando no jogo..." : "Criando jogo...";

    let endpoint = "http://localhost:8080/api/game";
    let payload = { playerName };

    if (gameId) {
        endpoint = `http://localhost:8080/api/game/${encodeURIComponent(gameId)}/join`;
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
        createGameBtn.disabled = false;
    }
}
