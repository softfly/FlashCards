package pl.softfly.flashcards.filesync.test;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/assets",
        //glue = "pl.softfly.flashcards.filesync.test",
        //strict = true,
        //tags="@single"
        tags="not @disabled"
)
public class RunFileSyncUnitTest {
}
