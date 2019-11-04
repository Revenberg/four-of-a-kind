package info.revenberg.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		addPlayer("Player 1");
		addPlayer("Player 2");
		resetCards();
	}

	/**
	 * Reset all game piles and the deck
	 */
	public void resetCards() {
		deck = new Deck();
		deck.shuffle();

		drawPile = new Pile(165);
		drawPile.setOffset(0);

		getPile = new Pile(180);
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
			Pile p = new Pile(165);
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

	/**
	 * Tests wheter all the cards have been placed in the correct pile
	 *
	 * @return {Boolean}
	 */
	public boolean checkWin() {
		for (Pile p : finalPiles) {
			if (p.cards.size() != 13)
				return false;
		}
		return true;
	}

	/**
	 * Save the game state to save.xml file
	 */
	public void save() {

		String saveString = "";

		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document doc = docBuilder.newDocument();

			String newLine = System.getProperty("line.separator");

			Element game = doc.createElement("game");
			doc.appendChild(game);

			// This is from previous implementation, save each pile in a new line
			for (Pile p : piles)
				saveString += p.toString() + newLine;
			for (Pile p : finalPiles)
				saveString += p.toString() + newLine;
			saveString += drawPile.toString() + newLine;
			saveString += getPile.toString() + newLine;

			String[] lines = saveString.split(newLine);

			for (String pile : lines) {
				Element p = doc.createElement("pile");

				String cardStrings[] = pile.split("-");
				for (String c : cardStrings) {
					String parts[] = c.split(" of ");

					Element cardE = doc.createElement("card");
					cardE.setAttribute("value", parts[0]);
					cardE.setAttribute("suit", parts[1]);
					cardE.setAttribute("isReversed", parts[2]);

					p.appendChild(cardE);
				}

				game.appendChild(p);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource src = new DOMSource(doc);
			StreamResult res = new StreamResult(new File("save.xml"));

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(src, res);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Load the game state from save.xml file
	 */
	public void load() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse("save.xml");
			Element docEle = dom.getDocumentElement();
			NodeList nl = docEle.getChildNodes();
			int currentPileCount = 0;
			if (nl != null) {
				// Iterate through all piles
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).getNodeType() != Node.ELEMENT_NODE)
						continue;
					Element el = (Element) nl.item(i);
					if (el.getNodeName().contains("pile")) {

						NodeList cardList = el.getChildNodes();
						Pile tempPile = new Pile(165);

						if (cardList != null) {
							// Iterate through all cards
							for (int j = 0; j < cardList.getLength(); j++) {
								if (cardList.item(j).getNodeType() != Node.ELEMENT_NODE)
									continue;

								Element cardNode = (Element) cardList.item(j);

								String suitName = cardNode.getAttribute("suit");
								boolean isReversed = cardNode.getAttribute("isReversed").equals("true");
								int value = Card.valueInt(cardNode.getAttribute("value"));

								// Skip the base card
								if (value == 100)
									continue;

								// Search for the card in all piles
								Card card = null;
								Pile foundPile = null;

								for (Pile p : allPiles) {
									if ((card = p.searchCard(value, suitName)) != null) {
										foundPile = p;
										break;
									}
								}

								tempPile.addCard(card);
								foundPile.removeCard(card);

								// Face-up or face-down card
								if (isReversed) {
									card.hide();
								} else {
									card.show();
								}
							}

							// Add the cards to the correct pile
							if (currentPileCount < pileNumber) {
								piles.get(currentPileCount).merge(tempPile);
							} else if (currentPileCount < pileNumber + 4) {
								finalPiles.get(currentPileCount - pileNumber).merge(tempPile);

								// if (!tempPile.isEmpty()) {
								// Set the pile filter for final piles
								// Card c = tempPile.peekTopCard();
								// finalPiles.get(currentPileCount
								// SR - pileNumber).suitFilter = c.suit;
								// }
							} else if (currentPileCount == pileNumber + 4) {
								drawPile.merge(tempPile);
							} else {
								getPile.merge(tempPile);
							}
						}
						currentPileCount++;
					}
				}
			}

			// Draw and add the cards again so the offsets are re-calculated
			for (Pile p : allPiles) {
				ArrayList<Card> cards = new ArrayList<Card>();

				while (!p.isEmpty())
					cards.add(p.drawCard());

				for (Card card : cards)
					p.addCard(card);
			}

		} catch (Exception e) {
			e.printStackTrace();
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
