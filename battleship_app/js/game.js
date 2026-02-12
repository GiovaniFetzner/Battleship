const output = document.getElementById("output");
const createGameForm = document.getElementById("createGameForm");
const boardSection = document.getElementById("boardSection");
const board = document.getElementById("board");
let boardReady = false;

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
                if (!boardReady) {
                    buildBoard();
                    boardReady = true;
                }
                boardSection.hidden = false;
                alert("Jogo criado! ID: " + data.gameId);
            }
        })
        .catch(err => {
            console.error(err);
            output.textContent = "Erro ao criar jogo";
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
