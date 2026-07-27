(function () {
    if (window.amoledTheme?.syncPreferences) {
        window.amoledTheme.syncPreferences();
        return;
    }

    const STYLE_ID = "lite-amoled";
    const CSS = `:root {
        --yt-spec-base-background: #000 !important;
        --yt-spec-raised-background: #000 !important;
        --yt-spec-menu-background: #000 !important;
        --yt-spec-surface-background: #000 !important;
        --yt-spec-badge-chip-background: #111 !important;
        --yt-spec-general-background-a: #0a0a0a !important;
        --yt-spec-general-background-b: #000 !important;
        --yt-spec-general-background-c: #111 !important;
        --yt-spec-error-background: #000 !important;
        --yt-spec-additive-background: #000 !important;
        --yt-spec-brand-background-primary: #000 !important;
        --yt-spec-brand-background-secondary: #111 !important;
        --yt-spec-static-overlay-background-solid: #000 !important;
        --yt-spec-static-brand-red: #ff0000 !important;
        --yt-spec-outline: #1a1a1a !important;
        --yt-spec-outline-inverse-medium: #000 !important;
    }
    html[dark="true"], html[dark="true"] body,
    ytd-app, ytd-browse, ytd-watch, ytd-search,
    ytm-app, ytm-browse, ytm-watch,
    #page-manager, .yt-core-dark-theme {
        background-color: #000 !important;
    }`;
    let enabled = false;

    function readEnabled() {
        try {
            return !!JSON.parse(lite.getPreferences() || "{}").amoled_theme;
        } catch {
            return false;
        }
    }

    function apply() {
        if (document.getElementById(STYLE_ID)) return;
        const style = document.createElement("style");
        style.id = STYLE_ID;
        style.textContent = CSS;
        document.head.appendChild(style);
    }

    function remove() {
        const el = document.getElementById(STYLE_ID);
        if (el) el.remove();
    }

    function syncPreferences() {
        const next = readEnabled();
        if (enabled === next) return;
        enabled = next;
        if (enabled) apply();
        else remove();
    }

    window.addEventListener("litePreferencesChanged", syncPreferences, true);
    window.amoledTheme = { syncPreferences };
    window.amoledThemeInjected = true;
    syncPreferences();
})();
