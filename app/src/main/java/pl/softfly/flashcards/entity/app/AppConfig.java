package pl.softfly.flashcards.entity.app;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Grzegorz Ziemski
 */
@Entity(tableName = "Core_App_Config")
public class AppConfig {

    public static final String DARK_MODE = "DarkMode";

    public static final String DARK_MODE_DEFAULT = "System";

    public static final String DARK_MODE_ON = "On";

    public static final String DARK_MODE_OFF = "Off";

    public static final List<String> DARK_MODE_OPTIONS = Arrays.asList(DARK_MODE_DEFAULT, DARK_MODE_ON, DARK_MODE_OFF);

    @PrimaryKey
    @NonNull
    private String key;

    private String value;

    public AppConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
