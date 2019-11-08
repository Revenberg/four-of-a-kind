package info.revenberg.game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.io.*;

/**
 * Card class to store the information of single card
 * 
 * @member {Suit} suit The suit of the card (Spades,Hearts,Diamonds,Clubs)
 * @member {Integer} value The value of the card (1->13)
 */
public class Card extends JPanel {
	private static final long serialVersionUID = 1L;
	public static String imagePath = "";
	public static String templateName = "Demo";
	public static int width = 200;
	public static int height = 120;

	public int value;
	public Suit suit;
	private BufferedImage image;
	private BufferedImage backImage;
	boolean isReversed;
	Point positionOffset;

	/**
	 * Enum to store the suit values
	 */
	public enum Suit {
		A(1), B(2), C(3), D(4);

		public int value;

		private Suit(int value) {
			this.value = value;
		}
	};

	/**
	 * Converts the value of the card to a string
	 * 
	 * @param {Integer} value The value of the card
	 */
	public static String valueString(int value) {
		return Integer.toString(value);
	}

	/**
	 * Converts the value of the card to a int
	 * 
	 * @param {String} value The value of the card
	 */
	public static int valueInt(String value) {
		return Integer.parseInt(value);
	}

	/**
	 * toString method, eg: "1 of A"
	 * 
	 * @return {String} Description of the current card
	 */
	public String toString() {
		return valueString(value) + " of " + suit.name();
	}

	/**
	 * Returns a string that can be used to re-initialize the card
	 * 
	 * @return {String} Class properties, " of " separated.
	 */
	public String saveAsString() {
		return valueString(value) + " of " + suit.name() + " of " + isReversed;
	}

	/**
	 * Class constructor
	 * 
	 * @param {Integer} value The value of the card, in [1,14]
	 * @param {Suit}    suit The suit of the card
	 */
	public Card(int value, Suit suit) {
		this.value = value;
		this.suit = suit;
		isReversed = false;

		try {
			// Load the image for the current file
			String filename = "";
			if (value == 100) {
				filename = imagePath + "/" + templateName + "/front.png";
			} else {
				filename = imagePath + "/" + templateName + "/cards/" + this.toString().toLowerCase() + ".png";
			}
			File file = new File(filename);
			image = ImageIO.read(file);

			// Load the backimage
			file = new File(imagePath + "/" + templateName + "/back.png");
			if (!file.exists()) {
				System.out.println(file.getAbsolutePath());
			}
			backImage = ImageIO.read(file);

			setBounds(0, 0, image.getWidth(), image.getHeight());
		} catch (IOException e) {
			e.getMessage();
		}

		positionOffset = new Point(0, 0);
		setSize(new Dimension(width, height));
		setOpaque(false);
	}

	/**
	 * Turns the card with the back up
	 */
	public void hide() {
		isReversed = true;
	}

	/**
	 * Turns the card with the face up
	 */
	public void show() {
		isReversed = false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		BufferedImage img = image;
		if (isReversed)
			img = backImage;

		g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
	}

}
