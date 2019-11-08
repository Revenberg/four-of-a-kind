package info.revenberg.game;

import info.revenberg.game.Engine;

public class Game {
	public Engine game;
	public GUI gui;	

	public Game() {						
		game = new Engine();
		gui = new GUI(game);
	}

	public static void main(String[] args) throws InterruptedException {							
		GUI.imagePath = "D:/git/four-of-a-kind/src/main/resources/images";
		Card.imagePath  = "D:/git/four-of-a-kind/src/main/resources/images";
		new Game();
	}
}