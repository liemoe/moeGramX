package moe.kirao.mgx;

import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.drinkmore.Tracer;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.unsorted.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

import me.vkryl.core.reference.ReferenceList;
import me.vkryl.leveldb.LevelDB;

public class MoexSettings {
  private static final int VERSION_1 = 1;
  private static final int VERSION = VERSION_1;
  private static final AtomicBoolean hasInstance = new AtomicBoolean(false);
  private static final String KEY_VERSION = "version";
  public static final String KEY_RECENT_STICKERS_COUNT = "recent_stickers_count";
  public static final String KEY_DISABLE_CAMERA_BUTTON = "disable_camera_button";
  public static final String KEY_DISABLE_RECORD_BUTTON = "disable_record_button";
  public static final String KEY_DISABLE_COMMANDS_BUTTON = "disable_commands_button";
  public static final String KEY_DISABLE_STICKER_TIMESTAMP = "disable_sticker_timestamp";
  public static final String KEY_ENABLE_FEATURES_BUTTON = "enable_features_button";
  public static final String KEY_HIDE_PHONE_NUMBER = "hide_phone_number";
  public static final String KEY_SHOW_ID_PROFILE = "show_id_profile";
  public static final String KEY_DISABLE_SENDAS_BUTTON = "disable_sendas_button";
  private static volatile MoexSettings instance;
  private final LevelDB config;

  private MoexSettings () {
    File configDir = new File(UI.getAppContext().getFilesDir(), "moexconf");
    if (!configDir.exists() && !configDir.mkdir()) {
      throw new IllegalStateException("Unable to create working directory");
    }
    long ms = SystemClock.uptimeMillis();
    config = new LevelDB(new File(configDir, "db").getPath(), true, new LevelDB.ErrorHandler() {
      @Override public boolean onFatalError (LevelDB levelDB, Throwable error) {
        Tracer.onDatabaseError(error);
        return true;
      }

      @Override public void onError (LevelDB levelDB, String message, @Nullable Throwable error) {
        // Cannot use custom Log, since settings are not yet loaded
        android.util.Log.e(Log.LOG_TAG, message, error);
      }
    });
    int configVersion = 0;
    try {
      configVersion = Math.max(0, config.tryGetInt(KEY_VERSION));
    } catch (FileNotFoundException ignored) {
    }
    if (configVersion > VERSION) {
      Log.e("Downgrading database version: %d -> %d", configVersion, VERSION);
      config.putInt(KEY_VERSION, VERSION);
    }
    for (int version = configVersion + 1; version <= VERSION; version++) {
      SharedPreferences.Editor editor = config.edit();
      upgradeConfig(config, editor, version);
      editor.putInt(KEY_VERSION, version);
      editor.apply();
    }
    Log.i("Opened database in %dms", SystemClock.uptimeMillis() - ms);
  }

  public static MoexSettings instance () {
    if (instance == null) {
      synchronized (MoexSettings.class) {
        if (instance == null) {
          if (hasInstance.getAndSet(true)) throw new AssertionError();
          instance = new MoexSettings();
        }
      }
    }
    return instance;
  }

  public LevelDB edit () {
    return config.edit();
  }

  public void remove (String key) {
    config.remove(key);
  }

  public void putLong (String key, long value) {
    config.putLong(key, value);
  }

  public long getLong (String key, long defValue) {
    return config.getLong(key, defValue);
  }

  public long[] getLongArray (String key) {
    return config.getLongArray(key);
  }

  public void putLongArray (String key, long[] value) {
    config.putLongArray(key, value);
  }

  public void putInt (String key, int value) {
    config.putInt(key, value);
  }

  public int getInt (String key, int defValue) {
    return config.getInt(key, defValue);
  }

  public void putFloat (String key, float value) {
    config.putFloat(key, value);
  }

  public void putBoolean (String key, boolean value) {
    config.putBoolean(key, value);
  }

  public boolean getBoolean (String key, boolean defValue) {
    return config.getBoolean(key, defValue);
  }

  public void putVoid (String key) {
    config.putVoid(key);
  }

  public boolean containsKey (String key) {
    return config.contains(key);
  }

  public void putString (String key, @NonNull String value) {
    config.putString(key, value);
  }

  public String getString (String key, String defValue) {
    return config.getString(key, defValue);
  }

  public void removeByPrefix (String prefix, @Nullable SharedPreferences.Editor editor) {
    config.removeByPrefix(prefix); // editor
  }

  public void removeByAnyPrefix (String[] prefixes, @Nullable SharedPreferences.Editor editor) {
    config.removeByAnyPrefix(prefixes); // , editor
  }

  public LevelDB config () {
    return config;
  }

  private void upgradeConfig (LevelDB config, SharedPreferences.Editor editor, int version) {
    // DO NOTHING
  }

  public interface SettingsChangeListener {
    void onSettingsChanged (String key, Object newSettings, Object oldSettings);
  }

  private ReferenceList<SettingsChangeListener> newSettingsListeners;

  public void addNewSettingsListener (SettingsChangeListener listener) {
    if (newSettingsListeners == null)
      newSettingsListeners = new ReferenceList<>();
    newSettingsListeners.add(listener);
  }

  public void removeNewSettingsListener (SettingsChangeListener listener) {
    if (newSettingsListeners != null) {
      newSettingsListeners.remove(listener);
    }
  }
  
  private void notifyNewSettingsListeners (String key, Object newSettings, Object oldSettings) {
    if (newSettingsListeners != null) {
      for (SettingsChangeListener listener : newSettingsListeners) {
        listener.onSettingsChanged(key, newSettings, oldSettings);
      }
    }
  }
  
  public int getRecentStickersCount () {
    return getInt(KEY_RECENT_STICKERS_COUNT, 20);
  }

  public void setRecentStickersCount (int count) {
    notifyNewSettingsListeners(KEY_RECENT_STICKERS_COUNT, count, getRecentStickersCount());
    putInt(KEY_RECENT_STICKERS_COUNT, count);
  }

  public boolean isDisableCameraButton () {
    return getBoolean(KEY_DISABLE_CAMERA_BUTTON, false);
  }

  public void toggleDisableCameraButton () {
    notifyNewSettingsListeners(KEY_DISABLE_CAMERA_BUTTON, !isDisableCameraButton(), isDisableCameraButton());
    putBoolean(KEY_DISABLE_CAMERA_BUTTON, !isDisableCameraButton());
  }

  public boolean isDisableRecordButton () {
    return getBoolean(KEY_DISABLE_RECORD_BUTTON, false);
  }

  public void toggleDisableRecordButton () {
    notifyNewSettingsListeners(KEY_DISABLE_RECORD_BUTTON, !isDisableRecordButton(), isDisableRecordButton());
    putBoolean(KEY_DISABLE_RECORD_BUTTON, !isDisableRecordButton());
  }

  public boolean isDisableCommandsButton () {
    return getBoolean(KEY_DISABLE_COMMANDS_BUTTON, false);
  }

  public void toggleDisableCommandsButton () {
    notifyNewSettingsListeners(KEY_DISABLE_COMMANDS_BUTTON, !isDisableCommandsButton(), isDisableCommandsButton());
    putBoolean(KEY_DISABLE_COMMANDS_BUTTON, !isDisableCommandsButton());
  }

  public boolean isDisableStickerTimestamp () {
    return getBoolean(KEY_DISABLE_STICKER_TIMESTAMP, false);
  }

  public void toggleDisableStickerTimestamp () {
    putBoolean(KEY_DISABLE_STICKER_TIMESTAMP, !isDisableStickerTimestamp());
  }

  public boolean isEnableFeaturesButton () {
    return getBoolean(KEY_ENABLE_FEATURES_BUTTON, false);
  }

  public void toggleEnableFeaturesButton () {
    putBoolean(KEY_ENABLE_FEATURES_BUTTON, !isEnableFeaturesButton());
  }
  public boolean isHidePhoneNumber () {
    return getBoolean(KEY_HIDE_PHONE_NUMBER, false);
    }

  public void toggleHidePhoneNumber () {
    putBoolean(KEY_HIDE_PHONE_NUMBER, !isHidePhoneNumber());
    }

  public boolean isShowIdProfile () {
    return getBoolean(KEY_SHOW_ID_PROFILE, false);
    }

  public void toggleShowIdProfile () {
    putBoolean(KEY_SHOW_ID_PROFILE, !isShowIdProfile());
    }

  public boolean isDisableSendAsButton () {
    return getBoolean(KEY_DISABLE_SENDAS_BUTTON, false);
    }

  public void toggleDisableSendAsButton () {
    putBoolean(KEY_DISABLE_SENDAS_BUTTON, !isDisableSendAsButton());
    }
}
