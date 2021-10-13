package pl.softfly.flashcards.tasks;

import java.util.concurrent.Callable;

public interface Task<T> extends Callable<T> {

    void timeout(Exception e);

    void error(Exception e);
}
