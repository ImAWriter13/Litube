(function () {
    if (window.amoledTheme?.syncPreferences) {
        window.amoledTheme.syncPreferences();
        return;
    }

    const AMOLED_STYLE_ID = "lite-amoled-bg";
    const COLOR_STYLE_ID = "lite-amoled-color";
    let enabled = false;
    let customColorEnabled = false;
    let customColor = "#39FF14";

    function buildAmoledCSS() {
        return `
            html, body, ytd-app, ytd-browse, ytd-watch-flexy, #content,
            ytd-masthead, #guide-content { background: #000 !important; }
            #author-comment-badge,
            ytd-author-comment-badge-renderer,
            #author-comment-badge *,
            ytd-author-comment-badge-renderer * {
                background: #222 !important;
                background-color: #222 !important;
            }
            .ytp-ce-element,
            .ytp-ce-covering-element,
            .ytp-ce-element-shadow,
            .ytp-ce-covering-overlay,
            .ytp-ce-video,
            .ytp-ce-playlist,
            .ytp-ce-channel,
            .ytp-endscreen-content,
            .ytp-show-tiles .ytp-videowall-still,
            .ytp-endscreen-previous,
            .ytp-endscreen-next,
            .html5-endscreen,
            .videowall-endscreen,
            .ytp-player-content,
            .ytp-cards-teaser,
            .ytp-ce-shadow {
                display: none !important;
                opacity: 0 !important;
                visibility: hidden !important;
            }
            .annotation,
            .annotation-type-custom,
            .iv-branding,
            .ytp-iv-video-content,
            .ytp-cards-button {
                display: none !important;
            }
        `;
    }

    function buildColorCSS(color) {
        return `
            /* Sostituisci la variabile brand red di YouTube */
            :root {
                --yt-spec-static-brand-red: ${color} !important;
                --yt-spec-brand-color-primary: ${color} !important;
                --yt-spec-call-to-action: ${color} !important;
                --yt-spec-call-to-action-reverse: ${color} !important;
                --yt-spec-themed-red: ${color} !important;
                --yt-spec-brand-red: ${color} !important;
                --yt-spec-brand-button-background: ${color} !important;
            }

            /* Barra di progresso del video player */
            .ytp-play-progress { background: ${color} !important; }
            .ytp-load-progress { background: ${color} !important; }
            .ytp-scrubber-button { background: ${color} !important; }
            .ytp-scrubber-button::before { background: ${color} !important; background-color: ${color} !important; }

            /* Cerchio di caricamento (spinner) */
            .ytp-spinner-container .ytp-spinner-circle,
            .ytp-spinner-circle {
                border-color: ${color} !important;
                border-top-color: ${color} !important;
            }
            .ytp-spinner-layer { fill: ${color} !important; }
            .ytp-loading-spinner { border-color: ${color} !important; }

            /* Barra riproduzione video sulle card (resume playback) */
            #progress.ytd-thumbnail-overlay-resume-playback-renderer { background: ${color} !important; }
            ytd-thumbnail-overlay-resume-playback-renderer #progress { background: ${color} !important; }

            /* Nomi canali */
            #channel-name, #channel-name *, #owner-name a { color: ${color} !important; }
            ytd-channel-name a { color: ${color} !important; }

            /* Link */
            yt-formatted-string a,
            #description-inner a,
            .yt-core-attributed-string a,
            a[href*="/hashtag/"] { color: ${color} !important; }

            /* HD Badge */
            .ytp-settings-button.ytp-hd-quality-badge::after {
                content: "HD" !important;
                background: ${color} !important;
                color: #fff !important;
                -webkit-text-fill-color: #fff !important;
                -webkit-background-clip: border-box !important;
                background-clip: border-box !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                text-align: center !important;
                line-height: normal !important;
                font-weight: bold !important;
                font-size: 9px !important;
                text-shadow: none !important;
                opacity: 1 !important;
                filter: none !important;
                mix-blend-mode: normal !important;
            }

            /* Badge verificati nei commenti */
            #comments ytd-badge-supported-renderer,
            #comments ytd-badge-supported-renderer *,
            #comments .badge,
            #comments .badge *,
            #comments yt-icon,
            ytd-comment-renderer ytd-badge-supported-renderer,
            ytd-comment-renderer ytd-badge-supported-renderer *,
            ytd-comment-renderer yt-icon.ytd-badge-supported-renderer,
            ytd-comment-thread-renderer yt-icon,
            #author-text-badge yt-icon,
            #author-text-badge svg,
            #author-text-badge path {
                color: ${color} !important;
                fill: ${color} !important;
            }

            /* Badge LIVE */
            .badge-style-live,
            ytd-badge-supported-renderer .badge-style-live,
            .ytp-live-badge .ytp-live-badge-text {
                color: ${color} !important;
                border-color: ${color} !important;
            }
            .ytp-live-badge::before { background: ${color} !important; }

            /* Pulsanti attivi/selezionati nel player */
            .ytp-settings-button[aria-pressed="true"] { color: ${color} !important; }
            .ytp-settings-button[aria-pressed="true"] svg { fill: ${color} !important; }
            .ytp-chapter-title { color: ${color} !important; }

            /* Linea temporale capitoli */
            .ytp-chapter-hover-container .ytp-tooltip-text { color: ${color} !important; }

            /* Pulsante Iscriviti */
            ytd-button-renderer yt-formatted-string[has-link],
            ytd-subscribe-button-renderer paper-button,
            ytd-subscribe-button-renderer yt-button-renderer {
                background-color: ${color} !important;
            }
            ytd-subscribe-button-renderer[subscribe-button][is-subscribed] paper-button {
                color: ${color} !important;
            }

            /* Indicatore di riproduzione (punto rosso) */
            ytd-thumbnail-overlay-now-playing-renderer[is-now-playing] #overlay.ytd-thumbnail-overlay-now-playing-renderer {
                background: ${color} !important;
            }
            #preview ytd-thumbnail-overlay-now-playing-renderer {
                background: ${color} !important;
            }

            /* Notifica toast di YouTube */
            yt-notification-renderer #action-button yt-button-renderer {
                background-color: ${color} !important;
            }
            paper-toast, ytd-notification-action-renderer {
                --yt-spec-call-to-action: ${color} !important;
            }

            /* Heart/commento like */
            ytd-comment-action-buttons-renderer #like-button yt-icon,
            ytd-comment-action-buttons-renderer #like-button yt-icon svg,
            ytd-comment-action-buttons-renderer #like-button yt-icon path,
            ytd-comment-action-buttons-renderer[ engagement-bar-style-heart-label] #like-button yt-icon {
                color: ${color} !important;
                fill: ${color} !important;
            }

            /* Segno di spunta verificato */
            ytd-author-comment-badge-renderer #author-comment-badge yt-icon svg,
            ytd-author-comment-badge-renderer #author-comment-badge yt-icon path {
                fill: ${color} !important;
            }

            /* Barra di ricerca attiva */
            ytd-searchbox[has-focus] #search-form.non-empty.ytd-searchbox {
                border-color: ${color} !important;
            }

            /* Pulsante menu attivo */
            ytd-guide-entry[active] .guide-icon.ytd-guide-entry,
            ytd-guide-entry[active] .guide-entry-text.ytd-guide-entry {
                color: ${color} !important;
            }
            ytd-guide-entry[active] .guide-icon.ytd-guide-entry svg {
                fill: ${color} !important;
            }

            /* Chip attivo nella home */
            yt-chip-cloud-chip-renderer[selected],
            yt-chip-cloud-renderer yt-chip-cloud-chip-renderer[selected],
            ytd-feed-filter-chip-bar-renderer yt-chip-cloud-chip-renderer[selected] {
                background-color: ${color} !important;
                color: #fff !important;
            }
            yt-chip-cloud-chip-renderer[selected] {
                background-color: ${color} !important;
                color: #fff !important;
            }

            /* Toggle switch in YouTube settings */
            tp-yt-paper-toggle-button[checked] .toggle-button.tp-paper-toggle-button {
                background-color: ${color} !important;
            }

            /* Spinner YouTube (massimo sforzo) */
            tp-yt-paper-spinner .spinner-layer,
            tp-yt-paper-spinner-lite .spinner-layer {
                border-color: ${color} !important;
            }
        `;
    }

    function readEnabled() {
        try {
            const prefs = JSON.parse(lite.getPreferences() || "{}");
            enabled = !!prefs.amoled_theme;
            customColorEnabled = !!prefs.amoled_custom_color_enabled;
            if (customColorEnabled && prefs.amoled_custom_color) {
                customColor = prefs.amoled_custom_color;
            } else {
                customColor = "#39FF14";
            }
        } catch {
            enabled = false;
            customColorEnabled = false;
            customColor = "#39FF14";
        }
    }

    function applyOrCreate(id, css) {
        let el = document.getElementById(id);
        if (el) {
            el.textContent = css;
        } else {
            el = document.createElement("style");
            el.id = id;
            el.textContent = css;
            document.head.appendChild(el);
        }
    }

    function removeStyle(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function ensureApplied() {
        if (enabled) {
            applyOrCreate(AMOLED_STYLE_ID, buildAmoledCSS());
        } else {
            removeStyle(AMOLED_STYLE_ID);
        }

        if (customColorEnabled) {
            applyOrCreate(COLOR_STYLE_ID, buildColorCSS(customColor));
        } else if (enabled) {
            applyOrCreate(COLOR_STYLE_ID, buildColorCSS("#39FF14"));
        } else {
            removeStyle(COLOR_STYLE_ID);
        }
    }

    function syncPreferences() {
        readEnabled();
        ensureApplied();
    }

    window.addEventListener("litePreferencesChanged", syncPreferences, true);
    window.amoledTheme = { syncPreferences };
    window.amoledThemeInjected = true;
    if (document.head) {
        syncPreferences();
    } else {
        var ready = function () {
            if (document.head) {
                syncPreferences();
            } else {
                requestAnimationFrame(ready);
            }
        };
        requestAnimationFrame(ready);
    }
})();
