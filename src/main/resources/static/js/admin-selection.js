(() => {
    const debounceTimers = new WeakMap();

    const closeResults = wrapper => {
        const results = wrapper.querySelector("[data-async-user-results]");
        if (results) results.classList.add("d-none");
    };

    const renderResults = (wrapper, options) => {
        const results = wrapper.querySelector("[data-async-user-results]");
        if (!results) return;

        results.innerHTML = "";
        if (!options.length) {
            const empty = document.createElement("div");
            empty.className = "list-group-item text-muted";
            empty.textContent = "Không tìm thấy người dùng.";
            results.appendChild(empty);
        } else {
            options.forEach(option => {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "list-group-item list-group-item-action";
                button.innerHTML = `<div class="fw-semibold"></div><small class="text-muted"></small>`;
                button.querySelector("div").textContent = option.fullName || option.username;
                button.querySelector("small").textContent = `@${option.username} · ID ${option.id}`;
                button.addEventListener("click", () => selectOption(wrapper, option));
                results.appendChild(button);
            });
        }
        results.classList.remove("d-none");
    };

    const selectOption = (wrapper, option) => {
        const input = wrapper.querySelector("[data-async-user-input]");
        const hidden = wrapper.querySelector("[data-async-user-id]");
        input.value = `${option.fullName || option.username} (@${option.username})`;
        hidden.value = option.id;
        wrapper.dataset.selected = "true";
        closeResults(wrapper);
        wrapper.dispatchEvent(new CustomEvent("async-user-selected", { detail: option }));
    };

    const clearSelection = wrapper => {
        const hidden = wrapper.querySelector("[data-async-user-id]");
        if (hidden.value) {
            hidden.value = "";
            wrapper.dataset.selected = "false";
            wrapper.dispatchEvent(new CustomEvent("async-user-cleared"));
        }
    };

    const search = async wrapper => {
        const input = wrapper.querySelector("[data-async-user-input]");
        const endpoint = wrapper.dataset.endpoint;
        const minChars = Number(wrapper.dataset.minChars || 2);
        const keyword = input.value.trim();

        if (keyword.length < minChars) {
            closeResults(wrapper);
            return;
        }

        try {
            const separator = endpoint.includes("?") ? "&" : "?";
            const response = await fetch(`${endpoint}${separator}q=${encodeURIComponent(keyword)}&page=1&size=20`);
            if (!response.ok) throw new Error("lookup failed");
            const options = await response.json();
            renderResults(wrapper, Array.isArray(options) ? options : []);
        } catch (error) {
            renderResults(wrapper, []);
        }
    };

    const initAsyncUserLookup = wrapper => {
        if (wrapper.dataset.initialized === "true") return;
        wrapper.dataset.initialized = "true";

        const input = wrapper.querySelector("[data-async-user-input]");
        const hidden = wrapper.querySelector("[data-async-user-id]");
        if (!input || !hidden || !wrapper.dataset.endpoint) return;

        if (hidden.value) wrapper.dataset.selected = "true";

        input.addEventListener("input", () => {
            clearSelection(wrapper);
            const oldTimer = debounceTimers.get(wrapper);
            if (oldTimer) clearTimeout(oldTimer);
            debounceTimers.set(wrapper, setTimeout(() => search(wrapper), 300));
        });

        input.addEventListener("focus", () => {
            if (!hidden.value && input.value.trim().length >= Number(wrapper.dataset.minChars || 2))
                search(wrapper);
        });

        if (wrapper.dataset.requireSelection === "true") {
            const form = wrapper.closest("form");
            if (form) {
                form.addEventListener("submit", event => {
                    input.setCustomValidity(hidden.value ? "" : "Vui lòng chọn một người dùng từ kết quả tìm kiếm.");
                    if (!hidden.value) {
                        event.preventDefault();
                        input.reportValidity();
                    }
                });
                input.addEventListener("input", () => input.setCustomValidity(""));
            }
        }
    };

    const initAll = root => {
        (root || document).querySelectorAll("[data-async-user-lookup]").forEach(initAsyncUserLookup);
    };

    document.addEventListener("click", event => {
        document.querySelectorAll("[data-async-user-lookup]").forEach(wrapper => {
            if (!wrapper.contains(event.target)) closeResults(wrapper);
        });
    });

    window.AdminSelection = { initAll, initAsyncUserLookup };
    document.addEventListener("DOMContentLoaded", () => initAll(document));
})();
