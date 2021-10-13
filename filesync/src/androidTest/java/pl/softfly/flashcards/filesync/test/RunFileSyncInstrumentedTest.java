package pl.softfly.flashcards.filesync.test;

import android.os.Bundle;

import org.junit.runner.RunWith;

import java.io.File;

import io.cucumber.android.runner.CucumberAndroidJUnitRunner;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import pl.softfly.flashcards.filesync.db.SyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.SyncDeckDatabaseUtil;

/**
 * @author Grzegorz Ziemski
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "features",
        strict = true,
        //tags="@single"
        tags="not @disabled"
)
public class RunFileSyncInstrumentedTest extends CucumberAndroidJUnitRunner {

    @Override
    public void onCreate(final Bundle bundle) {
        bundle.putString("plugin", getPluginConfigurationString());
        new File(getAbsoluteReportsPath()).mkdirs();

        SyncDeckDatabaseUtil syncDeckDatabaseUtil = SyncDatabaseUtil.
                getInstance(getContext())
                .getSyncDeckDatabaseUtil();

        syncDeckDatabaseUtil.listDatabases()
                .forEach(syncDeckDatabaseUtil::removeDatabase);
        super.onCreate(bundle);
    }

    private String getPluginConfigurationString() {
        String cucumber = "cucumber";
        String separator = "--";
        return "junit:" + getCucumberXml(cucumber) + separator +
                "html:" + getCucumberHtml(cucumber);
    }

    private String getCucumberHtml(String cucumber) {
        return getAbsoluteReportsPath() + "/" + cucumber + ".html";
    }

    private String getCucumberXml(String cucumber) {
        return getAbsoluteReportsPath() + "/" + cucumber + ".xml";
    }

    private String getAbsoluteReportsPath() {
        File directory = getTargetContext().getExternalFilesDir(null);
        return new File(directory, "reports").getAbsolutePath();
    }
}