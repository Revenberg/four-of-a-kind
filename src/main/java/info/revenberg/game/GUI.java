package info.revenberg.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import info.revenberg.game.Pile;

public class GUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	public static String imagePath;
	private JMenuBar menuBar;
	private JDialog popupDialog = null;

	Map<String, String> displayText;
	JPanel gameArea;
	JPanel columns;
	JPanel topColumns;
	JPanel playerColumns;
	JLayeredPane lp;
	Engine game;

	Pile tempPile;
	Point mouseOffset;

	// players
	private static JPanel players;
	private static JPanel topPanel;
	private static JPanel buttonPanel;

	private JLabel nameLabel;
	private JTextField nameInput;
	private JButton addButton;

	/**
	 * GUI class constructor
	 */
	public GUI(Engine game) {
		this.game = game;

		// Initialize stuff
		createTextMap();

		// Window settings
		setTitle("Four of a kind");
		setSize(900, 700);

		try {
			setContentPane(new JPanelWithBackground(imagePath + "/" + "background.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		setLayout(new BorderLayout());

		gameArea = new JPanel();
		gameArea.setOpaque(false);
		gameArea.setLayout(new BoxLayout(gameArea, BoxLayout.PAGE_AXIS));

		// Center the window
		setLocationRelativeTo(null);

		// Window close event
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Add GUI elements
		createTopMenu();

		// VELDEN
		// Flow layout to display multiple columns on the same row
		// FlowLayout flow = new FlowLayout(FlowLayout.CENTER);
		FlowLayout flow = new FlowLayout(FlowLayout.CENTER);
		flow.setAlignOnBaseline(true);

		// Add the columns panel
		columns = new JPanel();
		columns.setOpaque(false);
		columns.setLayout(new GridLayout(3, 4));
		columns.setMinimumSize(new Dimension(500, 900));

		// Add the top columns panel
		FlowLayout topFlow = new FlowLayout(FlowLayout.LEFT);
		topFlow.setAlignOnBaseline(true);

		topColumns = new JPanel();
		topColumns.setOpaque(false);
		topColumns.setLayout(topFlow);

		players = new JPanel();
		players.setOpaque(false);
		players.setLayout(topFlow);
		players.setLayout(new BoxLayout(players, BoxLayout.Y_AXIS));
		buildPanel();

		playerColumns = new JPanel();
		playerColumns.setOpaque(false);
		playerColumns.setLayout(flow);

		gameArea.add(topColumns);
		gameArea.add(columns);
		gameArea.add(playerColumns);

		// layers.add(dragLayer, JLayeredPane.DRAG_LAYER);
		add(gameArea);

		// Display the window
		lp = getLayeredPane();
		setVisible(true);

		// Auxiliarry elements
		mouseOffset = new Point(0, 0);

		initialize();
	}

	/**
	 * Add cards from the game to the GUI
	 */
	private void initialize() {
		topColumns.removeAll();
		playerColumns.removeAll();
		columns.removeAll();

		// Add a listener for each card
		for (Card c : game.deck.cards) {
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
		}

		game.setupGame();
		for (Pile p : game.piles) {
			columns.add(p);
		}

		topColumns.add(game.drawPile);
		topColumns.add(game.getPile);
		topColumns.add(players);

		for (Pile p : game.finalPiles) {
			JPanel jp = new JPanel();

			Player player = (Player) p;
			jp.add(player.tname);
			jp.add(player.tscore);
			jp.add(p);
			jp.add(new JLabel(" "));
			jp.add(new JLabel(" "));
			jp.add(new JLabel(" "));
			jp.add(new JLabel(" "));
			jp.add(new JLabel(" "));

			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

			jp.setOpaque(true);
			playerColumns.add(jp);
		}

		validate();
	}

	/**
	 * Resets the whole game
	 */
	public void reset() {
		game.resetCards();
		initialize();
		repaint();
	}

	/**
	 * Creates the displayText map Change this if you want to translate the game
	 * into another language
	 */
	private void createTextMap() {
		displayText = new HashMap<String, String>();

		displayText.put("File", "File");
		displayText.put("New", "New");
		// displayText.put("Save", "Save");
		// displayText.put("Load", "Load");
		displayText.put("Exit", "Exit");
	}

	/**
	 * Create the top menu bar
	 */
	private void createTopMenu() {
		menuBar = new JMenuBar();

		JMenu FileMenu = new JMenu("File");
		FileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(FileMenu);

		menuOption[] fileOptions = new menuOption[] { new menuOption(displayText.get("New"), KeyEvent.VK_N),
//				new menuOption(displayText.get("Save"), KeyEvent.VK_S),
//				new menuOption(displayText.get("Load"), KeyEvent.VK_L),
				new menuOption(displayText.get("Exit"), KeyEvent.VK_X) };

		for (menuOption option : fileOptions) {
			JMenuItem opt = new JMenuItem(option.name);
			if (option.shorcut != 0)
				opt.setMnemonic(option.shorcut);

			opt.addActionListener(this);
			FileMenu.add(opt);
		}

		setJMenuBar(menuBar);
	}

	/**
	 * Auxiliary class which stores information about a single menu option
	 *
	 * @member {String} name The name of the
	 * @member {Integer} shortcut The mnemonic for this button
	 */
	class menuOption {
		public String name;
		public Integer shorcut = 0;

		public menuOption(String name, Integer shorcut) {
			this.name = name;
			this.shorcut = shorcut;
		}
	}

	/**
	 * Function to handle most of the events performed on the GUI
	 */
	public void actionPerformed(ActionEvent e) {

		// Handle all menu interactions
		if (e.getSource() instanceof JMenuItem)
			handleMenuInteraction(e);

	}

	/**
	 * Handles the activation of any of the menu bar buttons
	 *
	 * @param {ActionEvent} e
	 */
	private void handleMenuInteraction(ActionEvent e) {
		JMenuItem item = (JMenuItem) e.getSource();

		if (item.getText().equals(displayText.get("Exit"))) {
			this.dispose();
			return;
		}
		if (item.getText().equals(displayText.get("New"))) {
			reset();
			return;
		}
		if (item.getText().equals(displayText.get("Save"))) {
			game.save();
			JOptionPane.showMessageDialog(this, "Game saved!");
			return;
		}
		if (item.getText().equals(displayText.get("Load"))) {
			game.load();
			validate();
			return;
		}
	}

	public static void showOnScreen(int screen, JFrame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();
		if (screen > -1 && screen < gd.length) {
			frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
		} else if (gd.length > 0) {
			frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
		} else {
			throw new RuntimeException("No Screens Found");
		}
	}

	public void popup(String imgName) {
		ImageIcon icon = new ImageIcon(imagePath + "/" + imgName);

			popupDialog = new JDialog();
			popupDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			popupDialog.setTitle("");
			popupDialog.setUndecorated(true);

			popupDialog.add(new JLabel(icon));

			popupDialog.pack();
			popupDialog.setLocationByPlatform(true);
		
		popupDialog.setLocation(this.getLocation());

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = this.getLocation().x + (screenSize.width - popupDialog.getWidth()) / 2;
		int y = (screenSize.height - popupDialog.getHeight()) / 2;
		popupDialog.setLocation(x, y);

		popupDialog.setVisible(true);

		Timer timer;
		timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e1) {
				popupDialog.setVisible(false);
				popupDialog.dispose();
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (tempPile != null) {

			Point pos = getLocationOnScreen();
			pos.x = e.getLocationOnScreen().x - pos.x - mouseOffset.x;
			pos.y = e.getLocationOnScreen().y - pos.y - mouseOffset.y;

			tempPile.setLocation(pos);
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getComponent() instanceof Card) {
			Card c = (Card) e.getComponent();
			Pile p = (Pile) c.getParent();

			switch (p.type) {
			case Draw:
				// game.cleanPiles();
				game.drawCard();				
				break;
			case PLAYER:
				game.clickPile(p);
	//		case Normal:
	//			game.clickPile(p);
	//			break;
			case FIELD:
				game.clickPile(p);
				break;
			case Get:
//				game.turnGetPile();
				break;
			}

			if (game.drawPile.cards.size() == 0) {
				if (!game.searchForFour()) {
					popup("gameover.jpg");
				}	
			}

			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getComponent() instanceof Card) {
			Card c = (Card) e.getComponent();

			// Do nothing if card is reversed
			if (c.isReversed)
				return;

			Pile p = (Pile) c.getParent();

			if (p.cards.isEmpty()) // || p.type == PileType.Final)
				return;

			tempPile = p.split(c);

			lp.add(tempPile, JLayeredPane.DRAG_LAYER);

			Point pos = getLocationOnScreen();
			mouseOffset = e.getPoint();
			pos.x = e.getLocationOnScreen().x - pos.x - mouseOffset.x;
			pos.y = e.getLocationOnScreen().y - pos.y - mouseOffset.y;

			tempPile.setLocation(pos);

			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (tempPile != null) {

			Point mousePos = e.getLocationOnScreen();
			boolean match = false;

			// Check if pile can merge with the pile it is dropped on
			ArrayList<Pile> droppable = new ArrayList<Pile>(game.piles);
			droppable.addAll(game.finalPiles);

			for (Pile p : droppable) {
				Point pilePos = p.getLocationOnScreen();
				Rectangle r = p.getBounds();
				r.x = pilePos.x;
				r.y = pilePos.y;

				if (r.contains(mousePos) && p.acceptsPile(tempPile)) {
					if (p.cards.size() >= 3) {
						p.merge(tempPile);
						for (int i = 0; i < 4; i++) {
							p.removeCard(p.cards.get(0));
						}
						p.cards.clear();
						Player player = (Player) p;
						player.score();
						popup("four.jpg");
						game.cleanPiles();
					} else {
						p.merge(tempPile);
					}
					match = true;
					break;
				}
			}

			// Snap back if no merge is found
			if (!match)
				tempPile.parent.merge(tempPile);

			lp.remove(tempPile);
			tempPile = null;
			repaint();

			if (game.checkWin()) {
				JOptionPane.showMessageDialog(this, "You won! Congrats!");
				reset();
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public class JPanelWithBackground extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Image backgroundImage;

		// Some code to initialize the background image.
		// Here, we use the constructor to load the image. This
		// can vary depending on the use case of the panel.
		public JPanelWithBackground(String fileName) throws IOException {
			File file = new File(fileName);
			backgroundImage = ImageIO.read(file);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Draw the background image.
			g.drawImage(backgroundImage, 0, 0, this);
		}
	}

	private void buildPanel() {
		topPanel = new JPanel();
		buttonPanel = new JPanel();
		nameLabel = new JLabel("Naam");
		nameInput = new JTextField(10);
		theHandler handler = new theHandler();
		addButton = new JButton("Speler toevoegen");
		addButton.addActionListener((ActionListener) handler);

		topPanel.add(nameLabel);
		topPanel.add(nameInput);
		topPanel.add(addButton);

		players.add(topPanel);
		players.add(buttonPanel);

		for (String name : game.getPlayerslist()) {
			addPlayerButton(name);
		}
	}

	private void addPlayerButton(String name) {
		JButton button = new JButton(name);
		theHandler handler = new theHandler();
		button.addActionListener((ActionListener) handler);
		buttonPanel.add(button);
	}

	private class theHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String name = nameInput.getText();

			if (event.getSource() == addButton) {
				if (name.length() < 1) {
					return;
				}
				nameInput.setText("");
				addPlayerButton(name);
				game.addPlayer(name);
				reset();
			} else {
				if (buttonPanel.getComponentCount() > 1) {					
					JButton jb = (JButton) event.getSource();
					name = jb.getText();
					buttonPanel.remove(jb);					
					game.removePlayer(name);					
					reset();
				}
			}
			buttonPanel.revalidate();
			buttonPanel.repaint();
		}

	}
}
