(function () {
    if (window.disableAmbient?.syncPreferences) {
        window.disableAmbient.syncPreferences();
        return;
    }

    let enabled = false;
    let styleEl = null;

    function readEnabled() {
        try {
            return !!JSON.parse(lite.getPreferences() || "{}").disable_ambient;
        } catch {
            return false;
        }
    }

    function disableViaSettings() {
        if (!enabled) return;
        const btn = document.querySelector('.ytp-settings-button');
        if (!btn || btn.hasAttribute('data-lite-ambient-processed')) return;
        btn.setAttribute('data-lite-ambient-processed', 'true');
        btn.click();
        setTimeout(() => {
            const items = document.querySelectorAll('.ytp-menuitem[role="menuitemcheckbox"]');
            for (const item of items) {
                const label = item.querySelector('.ytp-menuitem-label');
                if (label && label.textContent.trim().toLowerCase() === 'ambient mode') {
                    if (item.getAttribute('aria-checked') === 'true') {
                        item.click();
                    }
                }
            }
            btn.click();
        }, 500);
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
        document.querySelectorAll('[data-lite-ambient-processed]').forEach(el => {
            el.removeAttribute('data-lite-ambient-processed');
        });
    }

    function syncPreferences() {
        const next = readEnabled();
        if (enabled === next) return;
        enabled = next;
        if (enabled) {
            injectCSS();
            disableViaSettings();
            const obs = new MutationObserver(() => disableViaSettings());
            obs.observe(document.body, { childList: true, subtree: true });
            setTimeout(() => obs.disconnect(), 30000);
        } else {
            removeCSS();
        }
    }

    window.addEventListener("litePreferencesChanged", syncPreferences, true);
    window.disableAmbient = { syncPreferences };
    window.disableAmbientInjected = true;
    syncPreferences();
})();
