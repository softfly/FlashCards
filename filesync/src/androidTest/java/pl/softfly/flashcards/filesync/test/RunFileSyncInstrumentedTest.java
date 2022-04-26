package pl.softfly.flashcards.filesync.test;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;

import org.junit.runner.RunWith;

import java.io.File;

import io.cucumber.android.runner.CucumberAndroidJUnitRunner;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import pl.softfly.flashcards.BuildConfig;
import pl.softfly.flashcards.db.storage.StorageDb;
import pl.softfly.flashcards.filesync.db.FileSyncDatabaseUtil;

/**
 * @author Grzegorz Ziemski
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "features",
        strict = true,
        //tags="@single"
        tags = "not @disabled"
)
public class RunFileSyncInstrumentedTest extends CucumberAndroidJUnitRunner {

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onCreate(@NonNull final Bundle bundle) {
        bundle.putString("plugin", getPluginConfigurationString());
        new File(getAbsoluteReportsPath()).mkdirs();

        StorageDb<? extends RoomDatabase> storageDb = FileSyncDatabaseUtil
                .getInstance(getTargetContext())
                .getStorageDb();

        if (BuildConfig.DEBUG) {
            storageDb.listDatabases(new File(storageDb.getDbFolder()))
                    .forEach(storageDb::removeDatabase2);
        }
        super.onCreate(bundle);
    }

    @NonNull
    private String getPluginConfigurationString() {
        String cucumber = "cucumber";
        String separator = "--";
        return "junit:" + getCucumberXml(cucumber) + separator +
                "html:" + getCucumberHtml(cucumber);
    }

    @NonNull
    private String getCucumberHtml(String cucumber) {
        return getAbsoluteReportsPath() + "/" + cucumber + ".html";
    }

    @NonNull
    private String getCucumberXml(String cucumber) {
        return getAbsoluteReportsPath() + "/" + cucumber + ".xml";
    }

    @NonNull
    private String getAbsoluteReportsPath() {
        File directory = getTargetContext().getExternalFilesDir(null);
        return new File(directory, "reports").getAbsolutePath();
    }
}