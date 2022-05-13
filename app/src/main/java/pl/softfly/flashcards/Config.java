package pl.softfly.flashcards;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Grzegorz Ziemski
 */
public class Config {

    private static Config INSTANCE;

    @NonNull
    private final Context appContext;

    private boolean databaseExternalStorage;

    private boolean testFilesExternalStorage;

    protected Config(@NonNull Context appContext) {
        this.appContext = appContext;
        try (InputStream is = appContext.getAssets().open("config.properties")) {
            Properties props = new Properties();
            props.load(is);
            databaseExternalStorage = Boolean.parseBoolean(
                    props.getProperty("database.external_storage", "")
            );
            testFilesExternalStorage = Boolean.parseBoolean(
                    props.getProperty("test.files.external_storage", "")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized Config getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Config(context);
        }
        return INSTANCE;
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public boolean isDatabaseExternalStorage() {
        return databaseExternalStorage;
    }

    public boolean isTestFilesExternalStorage() {
        return testFilesExternalStorage;
    }

    public boolean isCrashlyticsEnabled() {
        try (InputStream is = appContext.getAssets().open("config.properties")) {
            Properties props = new Properties();
            props.load(is);
            return Boolean.parseBoolean(
                    props.getProperty("crashlytics.enabled", "true")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
