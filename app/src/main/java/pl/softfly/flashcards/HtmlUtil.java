package pl.softfly.flashcards;

import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Pattern;

/**
 * @author Grzegorz Ziemski
 */
public class HtmlUtil {

    private final static String DETECT_HTML_PATTERN = "\\<.*?\\>";
    private final static Pattern HTML_PATTERN = Pattern.compile(DETECT_HTML_PATTERN);
    private static HtmlUtil INSTANCE;

    public static synchronized HtmlUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HtmlUtil();
        }
        return INSTANCE;
    }

    public boolean isHtml(@Nullable String s) {
        boolean ret = false;
        if (s != null) {
            ret = HTML_PATTERN.matcher(s).find();
        }
        return ret;
    }

    public Spanned fromHtml(@NonNull String source) {
        return Html.fromHtml(
                source.replace("\n", "<br/>"),
                Html.FROM_HTML_MODE_COMPACT);
    }

}
