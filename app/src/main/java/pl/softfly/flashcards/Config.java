package pl.softfly.flashcards;

import android.content.Context;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Grzegorz Ziemski
 */
public class Config {

    private static Config INSTANCE;

    private boolean databaseExternalStorage;

    private boolean testFilesExternalStorage;

    protected Config(Context appContext) {
        try {
            InputStream is = appContext.getAssets().open("config.properties");
            Properties props = new Properties();
            props.load(is);

            databaseExternalStorage = Boolean.parseBoolean(
                    props.getProperty("database.external_storage", "")
            );
            testFilesExternalStorage = Boolean.parseBoolean(
                    props.getProperty("test.files.external_storage", "")
            );

            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized Config getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Config(context);
        }
        return INSTANCE;
    }

    public boolean isDatabaseExternalStorage() {
        return databaseExternalStorage;
    }

    public boolean isTestFilesExternalStorage() {
        return testFilesExternalStorage;
    }
}
