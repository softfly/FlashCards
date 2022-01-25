package pl.softfly.flashcards.tasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LongTasksExecutor {

    public static final long THREAD_TIMEOUT_SECONDS = 5*50;//5*60l;

    private static LongTasksExecutor INSTANCE;

    private final Queue<Task<Object>> tasksQ = new ConcurrentLinkedQueue<>();

    private Thread executorThread;

    @NonNull
    private Boolean alive = false;

    protected LongTasksExecutor() {
    }

    public synchronized void doTask(Task<Object> newTask) {
        setAlive(true);
        tasksQ.add(newTask);
        if (executorThread == null || !executorThread.isAlive()) {
            executorThread = new Thread(() -> {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                while (!tasksQ.isEmpty()) {
                    Task task = tasksQ.remove();
                    List<Task<Object>> tasks = List.of(task);
                    try {
                        executorService.invokeAny(tasks, THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        task.timeout(e);
                    } catch (@NonNull ExecutionException | InterruptedException e) {
                        task.error(e);
                    }
                }
                setAlive(false);
                executorService.shutdown();
            });
            executorThread.start();
        }
    }

    public static synchronized LongTasksExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LongTasksExecutor();
        }
        return INSTANCE;
    }

    public boolean isAlive() {
        synchronized (alive) {
            return alive;
        }
    }

    private void setAlive(@NonNull Boolean alive) {
        synchronized (alive) {
            this.alive = alive;
        }
    }
}
