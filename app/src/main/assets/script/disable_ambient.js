(function () {
    if (window.disableAmbient?.syncPreferences) {
        window.disableAmbient.syncPreferences();
        return;
    }

    let enabled = false;
    let styleEl = null;
    let observer = null;

    function readEnabled() {
        try {
            return !!JSON.parse(lite.getPreferences() || "{}").disable_ambient;
        } catch {
            return false;
        }
    }

    function toggleAmbientSwitch() {
        if (!enabled) return;
        const switches = document.querySelectorAll(
            'switch-list-item-view-model button[role="switch"], ' +
            'yt-switch-list-item-view-model button[role="switch"]'
        );
        for (const btn of switches) {
            if (btn.hasAttribute('data-lite-ambient-done')) continue;
            const item = btn.closest('yt-list-item-view-model, switch-list-item-view-model');
            if (!item) continue;
            const label = item.querySelector('.ytAttributedStringHost, .ytListItemViewModelTitle');
            if (!label) continue;
            if (!label.textContent.toLowerCase().includes('ambient')) continue;
            if (btn.getAttribute('aria-checked') === 'true') {
                btn.click();
            }
            btn.setAttribute('data-lite-ambient-done', 'true');
        }
    }

    function injectCSS() {
        if (styleEl) return;
        styleEl = document.createElement('style');
        styleEl.id = 'lite-no-ambient';
        styleEl.textContent = `
            .ytp-ambient, .ytp-ambient-canvas,
            ytm-ambient, ytm-ambient-mode,
            #cinematic-container, .cinematic-container,
            [data-ambient], [aria-label="Ambient mode"],
            .html5-video-player[data-ambient="true"] canvas,
            .ytp-gradient-top[style*="background"],
            .ytp-gradient-bottom[style*="background"] {
                display: none !important;
            }
        `;
        document.head.appendChild(styleEl);
    }

    function removeCSS() {
        if (styleEl) {
            styleEl.remove();
            styleEl = null;
        }
    }

    function startObserver() {
        if (observer) observer.disconnect();
        observer = new MutationObserver(() => {
            if (enabled) toggleAmbientSwitch();
        });
        if (document.body) {
            observer.observe(document.body, { childList: true, subtree: true });
        }
    }

    function stopObserver() {
        if (observer) {
            observer.disconnect();
            observer = null;
        }
    }

    function syncPreferences() {
        const next = readEnabled();
        if (enabled === next) {
            if (enabled) toggleAmbientSwitch();
            return;
        }
        enabled = next;
        if (enabled) {
            injectCSS();
            toggleAmbientSwitch();
            startObserver();
        } else {
            stopObserver();
            removeCSS();
        }
    }

    window.addEventListener("litePreferencesChanged", syncPreferences, true);
    window.disableAmbient = { syncPreferences };
    window.disableAmbientInjected = true;
    syncPreferences();
})();
