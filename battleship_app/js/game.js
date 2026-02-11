const output = document.getElementById("output");
const createGameForm = document.getElementById("createGameForm");

document
    .getElementById("createGameBtn")
    .addEventListener("click", createGame);

createGameForm.addEventListener("submit", event => {
    event.preventDefault();
    createGame();
});

function createGame() {
    const playerName =
        document.getElementById("playerName").value || "Player1";

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
            console.log("Resposta:", data);
            output.textContent = JSON.stringify(data, null, 2);

            if (data.type === "GAME_STATE") {
                alert("Jogo criado! ID: " + data.gameId);
            }
        })
        .catch(err => {
            console.error(err);
            output.textContent = "Erro ao criar jogo";
        });
}
