(function () {
    const DEFAULT_LOCAL_API_BASE_URL = "http://localhost:8080/api/game";
    const API_PATH = "/api/game";

    function trimTrailingSlashes(value) {
        return String(value || "").replace(/\/+$/, "");
    }

    function resolveApiBaseUrl() {
        const explicitBaseUrl = window.BATTLESHIP_API_BASE_URL;

        if (typeof explicitBaseUrl === "string" && explicitBaseUrl.trim()) {
            return trimTrailingSlashes(explicitBaseUrl.trim());
        }

        // Use same origin if available; fallback to localhost:8080 only if on port 8080
        const sameOriginUrl = trimTrailingSlashes(window.location.origin) + API_PATH;
        if (window.location.port === "8080") {
            return DEFAULT_LOCAL_API_BASE_URL;
        }

        if (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1") {
            return sameOriginUrl;
        }

        return sameOriginUrl;
    }

    function resolveApiUrl(path = "") {
        const baseUrl = resolveApiBaseUrl();
        const normalizedPath = String(path || "").replace(/^\/+/, "");

        if (!normalizedPath) {
            return baseUrl;
        }

        return `${baseUrl}/${normalizedPath}`;
    }

    function resolveWebSocketUrl(path = "/ws/game", searchParams = {}) {
        const apiUrl = new URL(resolveApiBaseUrl(), window.location.href);
        const protocol = apiUrl.protocol === "https:" ? "wss:" : "ws:";
        const socketUrl = new URL(path, `${protocol}//${apiUrl.host}`);

        Object.entries(searchParams).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== "") {
                socketUrl.searchParams.set(key, value);
            }
        });

        return socketUrl.toString();
    }

    window.BattleshipConfig = {
        resolveApiBaseUrl,
        resolveApiUrl,
        resolveWebSocketUrl
    };
})();