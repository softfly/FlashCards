package pl.softfly.flashcards;

import android.text.Html;
import android.text.Spanned;

import java.util.regex.Pattern;

/**
 * @author Grzegorz Ziemski
 */
public class HtmlUtil {

    private static HtmlUtil INSTANCE;

    private final static String DETECT_HTML_PATTERN = "\\<.*?\\>";

    private final static Pattern HTML_PATTERN = Pattern.compile(DETECT_HTML_PATTERN);

    public static synchronized HtmlUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HtmlUtil();
        }
        return INSTANCE;
    }

    public boolean isHtml(String s) {
        boolean ret = false;
        if (s != null) {
            ret = HTML_PATTERN.matcher(s).find();
        }
        return ret;
    }

    public Spanned fromHtml(String source) {
        return Html.fromHtml(
                source.replace("\n", "<br/>"),
                Html.FROM_HTML_MODE_COMPACT);
    }

}
