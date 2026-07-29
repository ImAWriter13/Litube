package com.hhst.youtubelite.extractor;

import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hhst.youtubelite.extractor.potoken.PoTokenContextStore;
import com.hhst.youtubelite.extractor.potoken.PoTokenWebViewContext;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that builds extractor auth snapshots.
 */
@Singleton
public final class AuthContextFactory {

	@NonNull
	private final PoTokenContextStore store;

	@Inject
	public AuthContextFactory(@NonNull PoTokenContextStore store) {
		this.store = store;
	}

	@NonNull
	public AuthContext create(@NonNull String url) {
		PoTokenWebViewContext page = store.getSnapshot();
		String cookieUrl = page != null ? page.url() : url;
		String cookies = normalize(CookieManager.getInstance().getCookie(cookieUrl));
		boolean loggedInFromCookies = hasLoginCookie(cookies);
		boolean loggedIn = (page != null && page.loggedIn()) || loggedInFromCookies;
		return new AuthContext(
						"webview",
						cookies,
						firstNonBlank(page != null ? page.visitorData() : null, getCookieValue(cookies, "VISITOR_INFO1_LIVE")),
						page != null ? page.dataSyncId() : null,
						page != null ? page.clientVersion() : null,
						page != null ? page.sessionIndex() : null,
						loggedIn,
						page != null && page.premium(),
						System.currentTimeMillis());
	}

	private boolean hasLoginCookie(@Nullable String cookies) {
		// When the PoToken snapshot is not yet available (e.g. the user opens a
		// video directly without first loading a watch page in the webview),
		// we can still detect a logged-in YouTube session from the presence of
		// the SAPISID/__Secure-3PAPISID cookies. This enables propagating the
		// session to the innertube clients so that age-restricted videos are
		// not gated as anonymous.
		if (cookies == null || cookies.isEmpty()) {
			return false;
		}
		return getCookieValue(cookies, "SAPISID") != null
				|| getCookieValue(cookies, "__Secure-3PAPISID") != null
				|| getCookieValue(cookies, "__Secure-1PAPISID") != null;
	}

	@Nullable
	private String firstNonBlank(@Nullable String first,
	                             @Nullable String second) {
		return first != null ? first : second;
	}

	@Nullable
	private String getCookieValue(@Nullable String cookies,
	                              @NonNull String name) {
		if (cookies == null || cookies.isEmpty()) {
			return null;
		}
		String prefix = name + "=";
		for (String part : cookies.split(";")) {
			String trimmed = part.trim();
			if (trimmed.startsWith(prefix) && trimmed.length() > prefix.length()) {
				return trimmed.substring(prefix.length());
			}
		}
		return null;
	}

	@Nullable
	private String normalize(@Nullable String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
