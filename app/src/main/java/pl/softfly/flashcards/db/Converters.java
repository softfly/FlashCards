package pl.softfly.flashcards.db;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
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

    @TypeConverter
    public static LocalDateTime fromTimestampToLocalDateTime(Long value) {
        return value == null ? null :
                LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(value),
                        TimeZone.getDefault().toZoneId()
                );
    }

    @TypeConverter
    public static Long localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null :
                dateTime.atZone(ZoneId.systemDefault())
                        .toEpochSecond();
    }

}
