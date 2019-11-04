package info.revenberg.game;

import java.awt.TextField;

public class Player extends Pile {
    private static final long serialVersionUID = 1L;

    private String name = "";
    private int score = 0;

    public TextField tname;
    public TextField tscore;		            
            
    public Player(int width, String name) {
        super(width);
        this.setOffset(30);
		this.type = PileType.PLAYER;
			
        this.name = name;

        tname = new TextField(name);
		tname.setBounds(50, 100, 200, 30);
		tscore = new TextField(Integer.toString(score));
        tscore.setBounds(50, 100, 200, 30);           
    }

    public String getName() {
        return this.name;
    }

    public int getScore() {
        return score;
    }

    public int score() {
        this.score++;
        tscore.setText(Integer.toString(score));
        return this.score;
    }
}