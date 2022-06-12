package pl.softfly.flashcards.filesync.test.algorithms;

import androidx.annotation.NonNull;

import org.junit.Assert;

import java.util.LinkedList;
import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import pl.softfly.flashcards.filesync.worker.SyncExcelToDeckWorker;

/**
 * @author Grzegorz Ziemski
 */
public class SuccessNotificationStepDefs {

    SyncExcelToDeckWorker.SyncSuccessNotification successNotificationFactory = new SyncExcelToDeckWorker.SyncSuccessNotification();

    List<String> results = new LinkedList<>();

    @Given(value = "Statistical data after synchronization:")
    public void statistical_data(@NonNull DataTable dataTable) {
        for (List<String> row : dataTable.asLists()) {
            int deckAdded = Integer.parseInt(row.get(0));
            int deckUpdated = Integer.parseInt(row.get(1));
            int deckDeleted = Integer.parseInt(row.get(2));
            int fileAdded = Integer.parseInt(row.get(3));
            int fileUpdated = Integer.parseInt(row.get(4));
            int fileDeleted = Integer.parseInt(row.get(5));
            results.add(successNotificationFactory.create(deckAdded, deckUpdated, deckDeleted, fileAdded, fileUpdated, fileDeleted));
        }
    }

    @Then(value = "The expected messages:")
    public void the_expected_deck_with_cards(DataTable dataTable) {
        for (List<String> row : dataTable.asLists()) {
            Assert.assertEquals(row.get(0), results.remove(0));
        }
    }

}
