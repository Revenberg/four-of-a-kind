package info.revenberg.game;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JLayeredPane;

import info.revenberg.game.Card.Suit;

public class Pile extends JLayeredPane {
    private static final long serialVersionUID = 1L;

    Card base;
    ArrayList<Card> cards;
    int offset = 15;
    int width;
    Pile parent;
    PileType type;

    public enum PileType {
        Draw, Get, FIELD, PLAYER
    };

    /**
     * Class constructor
     * 
     * @param width
     */
    public Pile(int width) {
        cards = new ArrayList<Card>();
        this.width = width;

        base = new Card(100, Suit.A);
        add(base, 1, 0);

        //type = PileType.Normal;
        type = PileType.FIELD;
    }

    /**
     * Adds a new card to the top of the pile. No checking is done, card is always
     * added
     * 
     * @param {Card} c The card to be added
     */
    public void addCard(Card c) {
        c.setLocation(0, offset * cards.size());
        cards.add(c);

        this.add(c, 1, 0);
        updateSize();
    }

    /**
     * Removes a card from the pile No checking is done, card is always remove
     * 
     * @param {Card} c The card to be removed
     */
    public void removeCard(Card c) {
        cards.remove(c);
        this.remove(c);

        updateSize();
    }

    /**
     * Returns the first card of the pile, without removing it
     * 
     * @return {Card}
     */
    public Card peekTopCard() {
        return cards.get(cards.size() - 1);
    }

    /**
     * Draws a card from the pile. Pack must not be empty.
     * 
     * @return First card in pack
     */
    public Card drawCard() {
        Card c = cards.get(0);
        removeCard(c);

        return c;
    }

    /**
     * Sets the width of the pile column. This is mostyl used for adding padding.
     */
    public void setWidth(int width) {
        this.width = width;
        updateSize();
    }

    /**
     * Updates pile size based on the number of cards in it
     */
    public void updateSize() {
        int height = base.getSize().height;

        if (!cards.isEmpty()) {
            height += offset * (cards.size() - 1);
        }

        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
    }

    /**
     * Changes the offset of the pile
     * 
     * @param {Integer} offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
        updateSize();
    }

    /**
     * Breaks the pile into two piles The top half is kept in this pile
     * 
     * @param {Card} first The card where the break starts
     * @return
     */
    public Pile split(Card first) {
        Pile p = new Pile(Card.width + 20);

        for (int i = 0; i < cards.size(); ++i) {
            if (cards.get(i) == first) {
                for (; i < cards.size();) {
                    p.addCard(cards.get(i));
                    removeCard(cards.get(i));
                }
            }
        }

        p.parent = this;

        return p;
    }

    /**
     * Merge the current pile with the given pile The given pile is placed on top
     * 
     * @param {Pile} p The pile to merge with
     */
    public void merge(Pile p) {
        for (Card c : p.cards)
            addCard(c);

        updateSize();
    }

    /**
     * Searches for a card in the pack based on value and suit name.
     * 
     * @param {int}    value
     * @param {String} suitName
     * @return {Card} The found card
     */
    public Card searchCard(int value, String suitName) {

        for (Card c : cards) {
            if (c.value == value && c.suit.name().equals(suitName))
                return c;
        }

        return null;
    }

    /**
     * Checks wether the pile is empty or not
     * 
     * @return {Boolean} True if the pile is empty
     */
    public boolean isEmpty() {
        return cards.size() == 0;
    }

    /**
     * Solitaire conditions to check if a move is valid
     */
    public boolean acceptsPile(Pile p) {
        // Can not add to itself
        if (this == p)
            return false;

        Card newCard = p.cards.get(0);
   //     Card topCard;
        switch (type) {
        case Draw:
            break;
        case Get:
            break;
        case FIELD:
            if (p.cards.size() > 1) {
                return false;
            }
            if (cards.isEmpty()) {
                return true;
            }
            break;
        case PLAYER:
            if (cards.isEmpty()) {
                return true;
            }

            boolean sameColor = true;
            boolean sameValue = true;

            for (Card card : cards) {
                sameColor = sameColor && (newCard.suit == card.suit);
                sameValue = sameValue && (newCard.value == card.value);
            }

            if (!sameValue && !sameColor) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    /**
     * Returns a string that contains all the cards in the pile.
     * 
     * @return {String} "-" Separated cards
     */
    public String toString() {
        String result = "";

        result += base.saveAsString() + "-";

        for (Card c : cards) {
            result += c.saveAsString() + "-";
        }

        return result;
    }

    // Change baseline, so pile is aligned to top
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }    
}
