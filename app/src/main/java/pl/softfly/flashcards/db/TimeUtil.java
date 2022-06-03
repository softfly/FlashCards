package pl.softfly.flashcards.db;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Grzegorz Ziemski
 */
public class TimeUtil {

    /**
     * UTC
     */
    public static long getNowEpochSec() {
        return TimeUnit.MILLISECONDS.toSeconds(Instant.now().toEpochMilli());
    }
}
