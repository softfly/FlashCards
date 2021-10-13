package pl.softfly.flashcards.db;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * SQLite use SECONDS timestamp.
 * Java use MILLISECONDS timestamp.
 * Convert for compatibility.
 *
 * @author Grzegorz Ziemski
 */
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(TimeUnit.SECONDS.toMillis(value));
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : TimeUnit.MILLISECONDS.toSeconds(date.getTime());
    }
}
