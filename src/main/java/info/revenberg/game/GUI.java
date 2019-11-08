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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import info.revenberg.game.Pile;

public class GUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	public static String imagePath;
	public static String templateName = "Demo";

	private JPanelWithBackground mainPanel;
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

	// Select CardTemplate
	private static JPanel templatePanel;
	private JComboBox<String> cardTemplate;
	private JButton selectTemplate;

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
			mainPanel = new JPanelWithBackground(imagePath + "/" + templateName + "/background.png");
			setContentPane(mainPanel);
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
		topColumns.add(templatePanel);

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

		File file = new File(imagePath);

		cardTemplate.removeAllItems();
		for (final File fileEntry : file.listFiles()) {
			if (fileEntry.isDirectory()) {
				cardTemplate.addItem(fileEntry.getName());
				templateName = fileEntry.getName();
			}
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

		displayText.put("Acties", "Acties");
		displayText.put("Nieuw", "Nieuw");
		displayText.put("Import", "Import");
		displayText.put("Spelregels", "Spelregels");
		displayText.put("Exit", "Exit");
	}

	/**
	 * Create the top menu bar
	 */
	private void createTopMenu() {
		menuBar = new JMenuBar();

		JMenu FileMenu = new JMenu("Acties");
		FileMenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(FileMenu);

		menuOption[] fileOptions = new menuOption[] { new menuOption(displayText.get("Nieuw"), KeyEvent.VK_N),
				new menuOption(displayText.get("Import"), KeyEvent.VK_I),
				new menuOption(displayText.get("Spelregels"), KeyEvent.VK_S),
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
		if (item.getText().equals(displayText.get("Nieuw"))) {
			reset();
			return;
		}
		if (item.getText().equals(displayText.get("Import"))) {
			FileChooser();
			return;
		}
		if (item.getText().equals(displayText.get("Spelregels"))) {
			String Spelregels = "Zoek vier kaartjes die bij elkaar horen. "
					+ "Dat zijn bijvoorbeeld meervouden zoals vliegtuigen, sommen, meesters en skippyballen; in de demo 1, 2, 3 en 4. Of vind vier die bij "
					+ "elkaar horen zoals vliegtuid, vliegtuigje, vliegtuigen en de afbeelding; in de demo A of B of C of D. Zie je een combinatie van kaartjes "
					+ "roep dan Four! (vier). Wie de meeste kaartjes verzameld wint dit spel. Heel veel plezier.";
			JTextArea msg = new JTextArea(Spelregels, 5, 50);
			msg.setLineWrap(true);
			msg.setWrapStyleWord(true);

			JOptionPane.showMessageDialog(this, msg);
			return;
		}
	}

	private void FileChooser() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));
		jfc.setDialogTitle("Choose Value File");
		jfc.setFileFilter(new CSVFilter());
		int returnValue = jfc.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();

			ImportImages test = new ImportImages();
			File file = new File("src/main/resources/images");
			try {
				test.readFile(file.getAbsolutePath(), selectedFile.getAbsolutePath());
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}

			JOptionPane.showMessageDialog(this, "Imported!");
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
		popupDialog.getRootPane().setOpaque(true);
		popupDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		popupDialog.setTitle("");
		popupDialog.setUndecorated(true);

		JLabel label = new JLabel(icon);
		label.setOpaque(true);
		popupDialog.add(label);

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
				game.drawCard();
				break;
			case PLAYER:
				game.clickPile(p);
				break;
			case FIELD:
				game.clickPile(p);
				break;
			case Get:
				// game.turnGetPile();
				break;
			}
			gameOver();

			repaint();
		}
	}

	private void gameOver() {
		if (game.drawPile.cards.size() == 0) {
			if (!game.searchForFour()) {

				Timer timer;
				timer = new Timer(5000, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e1) {
						popup("gameover.jpg");
					}
				});
				timer.setRepeats(false);
				timer.start();
			}
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
						popup("four.png");
						game.cleanPiles();
						gameOver();
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
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public class JPanelWithBackground extends JPanel {
		private static final long serialVersionUID = 1L;
		private Image backgroundImage;

		public JPanelWithBackground(String fileName) throws IOException {
			File file = new File(fileName);
			backgroundImage = ImageIO.read(file);
		}

		public void updateBackground(String fileName) throws IOException {
			File file = new File(fileName);
			backgroundImage = ImageIO.read(file);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
		}
	}

	private void buildPanel() {
		topPanel = new JPanel();
		buttonPanel = new JPanel();
		templatePanel = new JPanel();
		nameLabel = new JLabel("Naam");
		nameInput = new JTextField(10);

		TemplateHandler templateHandler = new TemplateHandler();
		cardTemplate = new JComboBox<String>();
		selectTemplate = new JButton("Wijzig template");
		selectTemplate.addActionListener((ActionListener) templateHandler);

		TheHandler handler = new TheHandler();
		addButton = new JButton("Speler toevoegen");
		addButton.addActionListener((ActionListener) handler);

		topPanel.add(nameLabel);
		topPanel.add(nameInput);
		topPanel.add(addButton);

		templatePanel.add(cardTemplate);
		templatePanel.add(selectTemplate);

		players.add(topPanel);
		players.add(buttonPanel);

		for (String name : game.getPlayerslist()) {
			addPlayerButton(name);
		}
	}

	private void addPlayerButton(String name) {
		JButton button = new JButton(name);
		TheHandler handler = new TheHandler();
		button.addActionListener((ActionListener) handler);
		buttonPanel.add(button);
	}

	private class TemplateHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			GUI.templateName = cardTemplate.getSelectedItem().toString();
			try {
				mainPanel.updateBackground(imagePath + "/" + GUI.templateName + "/background.png");
			} catch (IOException e1) {
				System.out.println(imagePath + "/" + GUI.templateName + "/background.png");
				e1.printStackTrace();
			}
			Card.templateName = GUI.templateName;
			reset();
		}
	}

	private class TheHandler implements ActionListener {
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

	class CSVFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			return (f.isFile() && f.getName().toLowerCase().endsWith(".csv")) || f.isDirectory();
		}

		public String getDescription() {
			return "*.csv";
		}
	}
}
