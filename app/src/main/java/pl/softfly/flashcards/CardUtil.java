package pl.softfly.flashcards;

import pl.softfly.flashcards.entity.Card;

/**
 * @author Grzegorz Ziemski
 */
public class CardUtil {

    private static CardUtil INSTANCE;

    private HtmlUtil htmlUtil = HtmlUtil.getInstance();

    public static synchronized CardUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CardUtil();
        }
        return INSTANCE;
    }

    public void setTerm(Card card, String term) {
        if (htmlUtil.isHtml(term)) {
            card.setTermHtml(true);
        }
        card.setTerm(term);
    }

    public void setDefinition(Card card, String definition) {
        if (htmlUtil.isHtml(definition)) {
            card.setTermHtml(true);
        }
        card.setTerm(definition);
    }
}
