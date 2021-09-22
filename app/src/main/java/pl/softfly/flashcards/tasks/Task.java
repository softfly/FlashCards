package pl.softfly.flashcards.tasks;

import java.util.concurrent.Callable;

public interface Task<T> extends Callable<T> {

    void timeout();

    void error();
}
