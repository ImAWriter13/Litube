package com.hhst.youtubelite.extension;

import com.tencent.mmkv.MMKV;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manager that stores extension toggles and restores defaults.
 */
@Singleton
public class ExtensionManager {
	private static final String KEY_VERSION = "preferences:version";

	private final MMKV mmkv;

	@Inject
	public ExtensionManager(MMKV mmkv) {
		this.mmkv = mmkv;
		initializeDefaultPreferences();
	}

	private void initializeDefaultPreferences() {
		initializeGesturePreferences();
		for (Map.Entry<String, Boolean> entry : Constant.DEFAULT_PREFERENCES.entrySet()) {
			String key = prefKey(entry.getKey());
			if (!mmkv.contains(key)) {
				mmkv.encode(key, entry.getValue());
			}
		}
		for (Map.Entry<String, String> entry : Constant.DEFAULT_COLORS.entrySet()) {
			String key = prefKey(entry.getKey());
			if (!mmkv.contains(key)) {
				mmkv.encode(key, entry.getValue());
			}
		}
		for (Map.Entry<String, String> entry : Constant.DEFAULT_STRINGS.entrySet()) {
			String key = prefKey(entry.getKey());
			if (!mmkv.contains(key)) {
				mmkv.encode(key, entry.getValue());
			}
		}
	}

	private void initializeGesturePreferences() {
		for (String key : Constant.GESTURE_KEYS) {
			if (mmkv.contains(prefKey(key))) {
				return;
			}
		}
		boolean enabled = true;
		String key = prefKey(Constant.ENABLE_PLAYER_GESTURES);
		if (mmkv.contains(key)) {
			enabled = mmkv.decodeBool(key, true);
		}
		for (String gesture : Constant.GESTURE_KEYS) {
			mmkv.encode(prefKey(gesture), enabled);
		}
	}

	private String prefKey(String key) {
		return "preferences:" + key;
	}

	public void setEnabled(String key, boolean enable) {
		String pref = prefKey(key);
		boolean changed = !mmkv.contains(pref) || mmkv.decodeBool(pref, Boolean.TRUE.equals(Constant.DEFAULT_PREFERENCES.getOrDefault(key, false))) != enable;
		mmkv.encode(pref, enable);
		if (changed) {
			bumpVersion();
		}
	}

	public boolean isEnabled(String key) {
		return mmkv.decodeBool(prefKey(key), Boolean.TRUE.equals(Constant.DEFAULT_PREFERENCES.getOrDefault(key, false)));
	}

	public void setString(String key, String value) {
		String pref = prefKey(key);
		String old = mmkv.decodeString(pref, "");
		if (!old.equals(value)) {
			mmkv.encode(pref, value);
			bumpVersion();
		}
	}

	public String getString(String key) {
		String defaultValue = Constant.DEFAULT_COLORS.getOrDefault(key, Constant.DEFAULT_STRINGS.getOrDefault(key, ""));
		return mmkv.decodeString(prefKey(key), defaultValue);
	}

	public void resetToDefault() {
		boolean changed = false;
		for (Map.Entry<String, Boolean> entry : Constant.DEFAULT_PREFERENCES.entrySet()) {
			String key = prefKey(entry.getKey());
			boolean value = entry.getValue();
			if (!mmkv.contains(key) || mmkv.decodeBool(key, value) != value) {
				changed = true;
			}
			mmkv.encode(key, value);
		}
		for (Map.Entry<String, String> entry : Constant.DEFAULT_COLORS.entrySet()) {
			String key = prefKey(entry.getKey());
			String value = entry.getValue();
			if (!value.equals(mmkv.decodeString(key, value))) {
				changed = true;
			}
			mmkv.encode(key, value);
		}
		for (Map.Entry<String, String> entry : Constant.DEFAULT_STRINGS.entrySet()) {
			String key = prefKey(entry.getKey());
			String value = entry.getValue();
			if (!value.equals(mmkv.decodeString(key, value))) {
				changed = true;
			}
			mmkv.encode(key, value);
		}
		if (changed) {
			bumpVersion();
		}
	}

	public Map<String, Object> getAllPreferences() {
		Map<String, Object> allPreferences = new HashMap<>();
		for (String key : Constant.DEFAULT_PREFERENCES.keySet()) {
			allPreferences.put(key, isEnabled(key));
		}
		for (String key : Constant.DEFAULT_COLORS.keySet()) {
			allPreferences.put(key, getString(key));
		}
		for (String key : Constant.DEFAULT_STRINGS.keySet()) {
			allPreferences.put(key, getString(key));
		}
		return allPreferences;
	}

	public long version() {
		return mmkv.decodeLong(KEY_VERSION, 0L);
	}

	private void bumpVersion() {
		mmkv.encode(KEY_VERSION, version() + 1L);
	}

}

