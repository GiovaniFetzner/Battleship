const createGameForm = document.getElementById("createGameForm");
const playerNameInput = document.getElementById("playerName");
const createGameBtn = document.getElementById("createGameBtn");
const formStatus = document.getElementById("formStatus");

createGameForm.addEventListener("submit", event => {
    event.preventDefault();
    createGame();
});

function createGame() {
    const playerName = playerNameInput.value.trim() || "Player1";
    createGameBtn.disabled = true;
    formStatus.textContent = "Criando jogo...";

    fetch("http://localhost:8080/api/game", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            type: "JOIN_GAME",
            playerName: playerName
        })
    })
        .then(res => res.json())
        .then(data => {
            if (data.type !== "GAME_STATE") {
                throw new Error("Resposta inesperada do servidor");
            }

            sessionStorage.setItem("playerName", playerName);
            if (data.gameId) {
                sessionStorage.setItem("gameId", data.gameId.toString());
            }
            sessionStorage.setItem("gameState", JSON.stringify(data));

            window.location.href = "game.html";
        })
        .catch(err => {
            console.error(err);
            formStatus.textContent = "Erro ao criar jogo. Tente novamente.";
        })
        .finally(() => {
            createGameBtn.disabled = false;
        });
}
