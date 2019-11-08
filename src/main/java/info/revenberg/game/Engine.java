package info.revenberg.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import info.revenberg.game.Pile;
import info.revenberg.game.Card.Suit;
import info.revenberg.game.Pile.PileType;

/**
 * Core class of the application. Contains all objects and states of the game
 */
public class Engine {
	ArrayList<Pile> piles;
	ArrayList<Player> finalPiles;
	Pile drawPile, getPile;
	ArrayList<Pile> allPiles;
	public final int pileNumber = 12;
	public Deck deck;
	private ArrayList<String> playerslist = new ArrayList<String>();

	/**
	 * Class constructor
	 */
	public Engine() {
		addPlayer("Speler 1");
		addPlayer("Speler 2");
		resetCards();
	}

	/**
	 * Reset all game piles and the deck
	 */
	public void resetCards() {
		int i = new File(Card.imagePath + "/" + Card.templateName + "/cards/").list().length;

		deck = new Deck(i / 4 );
		deck.shuffle();

		drawPile = new Pile(Card.width + 20);
		drawPile.setOffset(0);

		getPile = new Pile(Card.width + 35);
		getPile.setOffset(0);

		finalPiles = new ArrayList<Player>();
		piles = new ArrayList<Pile>();

		allPiles = new ArrayList<Pile>();
		allPiles.add(drawPile);
		allPiles.add(getPile);

		for (String name : playerslist) {
			Player p = new Player(200, name);
			finalPiles.add(p);
			allPiles.add(p);
		}

	}

	/**
	 * Setup the initial game state
	 */
	public void setupGame() {
		// Generate piles
		drawPile.type = PileType.Draw;
		getPile.type = PileType.Get;

		for (int i = 1; i <= pileNumber; ++i) {
			Pile p = new Pile(Card.width + 20);
			p.setOffset(0);
			p.type = PileType.FIELD;

			piles.add(p);
			allPiles.add(p);
		}

		while (deck.size() > 0) {
			Card card = deck.drawCard();
			card.hide();
			drawPile.addCard(card);
		}
	}

	public void cleanPiles() {
		for (Pile fp : finalPiles) {
			while (!fp.cards.isEmpty()) {
				Card c = fp.peekTopCard();
				for (Pile p : piles) {
					if (p.cards.isEmpty()) {
						p.addCard(c);
						p.setOffset(0);
						fp.removeCard(c);
						break;
					}
				}
			}
		}
		if (getPile.cards.size() > 0) {
			Card c = getPile.peekTopCard();
			for (Pile p : piles) {
				if (p.cards.isEmpty()) {
					p.addCard(c);
					p.setOffset(0);
					getPile.removeCard(c);
					break;
				}
			}
		}
	}

	/**
	 * Draw a card from the draw pile and place it into the get pile
	 */
	public void drawCard() {			
			
		if (!drawPile.cards.isEmpty()) {
			Card drew = drawPile.drawCard();
			drew.isReversed = false;
			cleanPiles();

			if (getPile.cards.size() == 0) {
				getPile.addCard(drew);
			}			
		} 				
	}

	/**
	 * When a normal pile is clicked, if the top card is reversed show it
	 *
	 * @param {Pile} p
	 */
	public void clickPile(Pile p) {
		if (!p.cards.isEmpty()) {
			Card c = p.cards.get(p.cards.size() - 1);
			if (c.isReversed) {
				c.isReversed = false;
			}
		}
	}

	/**
	 * Reverse the Get pile and place it again for Draw
	 */
	public void turnGetPile() {
		if (!drawPile.cards.isEmpty())
			return;

		while (!getPile.cards.isEmpty()) {
			Card c = getPile.drawCard();
			c.isReversed = true;

			drawPile.addCard(c);
		}
	}

	public ArrayList<String> getPlayerslist() {
		return this.playerslist;
	}

	public void addPlayer(String name) {
		playerslist.add(name);
	}

	public void removePlayer(String name) {
		playerslist.remove(name);
	}

	public boolean searchForFour() {

		HashMap<Suit, Integer> sameColor = new HashMap<Suit, Integer>();
		HashMap<Integer, Integer> sameValue = new HashMap<Integer, Integer>();

		for (Pile pile : allPiles) {
			if (pile.cards.size() > 0) {
				Card c = pile.cards.get(0);
				if (!c.isReversed) {					
					if (sameColor.get(c.suit) == null) {
						sameColor.put(c.suit, 1);
					} else {
						sameColor.put(c.suit, sameColor.get(c.suit) + 1);
					}					
				}
				if (sameValue.get(c.value) == null) {
					sameValue.put(c.value, 1);
				} else {
					sameValue.put(c.value, sameValue.get(c.value) + 1);
				}
			}
		}
		for (Suit suit : sameColor.keySet()) {
			if (sameColor.get(suit) > 3) {
				return true;
			}
		}
		for (Integer value : sameValue.keySet()) {
			if (sameValue.get(value) > 3) {
				return true;
			}
		}
		return false;
	}
}
