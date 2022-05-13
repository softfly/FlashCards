package pl.softfly.flashcards;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.entity.Card;

/**
 * @author Grzegorz Ziemski
 */
public class CardUtil {

    private static CardUtil INSTANCE;

    private final HtmlUtil htmlUtil = HtmlUtil.getInstance();

    public static synchronized CardUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CardUtil();
        }
        return INSTANCE;
    }

    public void setTerm(@NonNull Card card, String term) {
        if (htmlUtil.isHtml(term)) {
            card.setTermHtml(true);
        }
        card.setTerm(term);
    }

    public void setDefinition(@NonNull Card card, String definition) {
        if (htmlUtil.isHtml(definition)) {
            card.setDefinitionHtml(true);
        }
        card.setDefinition(definition);
    }
}
