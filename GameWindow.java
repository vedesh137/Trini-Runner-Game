import javax.swing.*; // need this for GUI objects
import java.awt.*; // need this for certain AWT classes
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.awt.image.BufferStrategy; // need this to implement page flipping

public class GameWindow extends JFrame implements
		Runnable,
		KeyListener,
		MouseListener,
		MouseMotionListener {
	private static final int NUM_BUFFERS = 2; // used for page flipping

	private int pWidth, pHeight; // width and height of screen

	private Thread gameThread = null; // the thread that controls the game
	private volatile boolean isRunning = false; // used to stop the game thread

	private BirdAnimation animation = null; // animation sprite
	//private ImageEffect imageEffect; // sprite demonstrating an image effect

	private BufferedImage image; // drawing area for each frame

	private Image quit1Image; // first image for quit button
	private Image quit2Image; // second image for quit button

	private boolean finishedOff = false; // used when the game terminates

	private volatile boolean isOverQuitButton = false;
	private Rectangle quitButtonArea; // used by the quit button

	private volatile boolean isOverPauseButton = false;
	private Rectangle pauseButtonArea; // used by the pause 'button'
	private volatile boolean isPaused = false;

	private volatile boolean isOverStopButton = false;
	private Rectangle stopButtonArea; // used by the stop 'button'
	private volatile boolean isStopped = false;

	private volatile boolean isOverShowAnimButton = false;
	private Rectangle showAnimButtonArea; // used by the show animation 'button'
	private volatile boolean isAnimShown = false;

	private volatile boolean isOverPauseAnimButton = false;
	private Rectangle pauseAnimButtonArea; // used by the pause animation 'button'
	private volatile boolean isAnimPaused = false;

	private GraphicsDevice device; // used for full-screen exclusive mode
	private Graphics gScr;
	private BufferStrategy bufferStrategy;

	private SoundManager soundManager;
	TileMapManager tileManager;
	TileMap tileMap;

	private boolean jumpKeypressed = false;
	private boolean leftKeyPressed = false;
	private boolean rightKeyPressed = false;
	private boolean attackKeyPressed = false;
	private boolean dashKeyPressed = false;

	// Level attributes
	private Level lvlData;
	private int  initialLevelNum;

	private ScoreManager scoreManager;

	private boolean isMenuVisible = true; // Show menu initially
    private Rectangle startButtonArea; // Area for the 'Start Game' button
	private Image menuBackground;


	public GameWindow() {

		super("Trini Runner");

		initFullScreen();
		menuBackground = ImageManager.loadImage("images/MenuBG.png");
		quit1Image = ImageManager.loadImage("images/Quit1.png");
		quit2Image = ImageManager.loadImage("images/Quit2.png");
		
		setButtonAreas();

		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		animation = new BirdAnimation();
		soundManager = SoundManager.getInstance();
		image = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_RGB);

		//game Attributes
		lvlData = new Level(this, scoreManager);
		initialLevelNum = 1;




		startGame();
	}

	// implementation of Runnable interface
	private void drawMenu(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		// Draw the background
		g2d.drawImage(menuBackground, 0, 0, pWidth, pHeight, null);
		
		// Title
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
		g2d.drawString("Game Menu", 10, 30);
	
		// Start Game button
		g2d.setColor(Color.RED);
		g2d.fillRect(startButtonArea.x, startButtonArea.y, startButtonArea.width, startButtonArea.height);
		g2d.setColor(Color.BLACK);
		g2d.drawString("Start Game", startButtonArea.x + 45, startButtonArea.y + 33);
	
		// Exit Game instruction
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 24));  // Smaller font for additional instructions
		g2d.drawString("Press ESC on Keyboard to Quit Game", pWidth - 990, pHeight - 30); // Position this text at the bottom right
	}
	
	public void run() {
		try {
			isRunning = true;
			while (isRunning) {
				if (isPaused == false) {
					gameUpdate();
				}
				screenUpdate();
				Thread.sleep(8);
			}
		} catch (InterruptedException e) {
		}

		finishOff();
	}



	private void finishOff() {
		if (!finishedOff) {
			finishedOff = true;
			restoreScreen();
			System.exit(0);
		}
	}

	public void endGame() {
		// Reset game state instead of stopping the game
		isMenuVisible = true; // Show the menu
		
		lvlData.loadLevel( initialLevelNum); // Reload the level or reset the game state
		// No need to stop the game loop here
	}
	

	

	private void restoreScreen() {
		Window w = device.getFullScreenWindow();

		if (w != null)
			w.dispose();

		device.setFullScreenWindow(null);
	}

	public void gameUpdate() {
		if (!isMenuVisible) {
			captureInput();
			lvlData.update();
			if (lvlData.getPlayer().getHealth() <= 0) {
				endGame(); // This will now reset the game and show the menu
			}
		}
	}
	

	private void screenUpdate() {

		try {
			gScr = bufferStrategy.getDrawGraphics();
			gameRender(gScr);
			gScr.dispose();
			if (!bufferStrategy.contentsLost())
				bufferStrategy.show();
			else
				System.out.println("Contents of buffer lost.");

			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)

			Toolkit.getDefaultToolkit().sync();
		} catch (Exception e) {
			e.printStackTrace();
			isRunning = false;
		}
	}

	public void gameRender(Graphics gScr) {
		Graphics2D g2d = (Graphics2D) gScr;
		if (isMenuVisible) {
			drawMenu(g2d); // This should draw the menu background and options
		} else {
			// Game rendering logic here
			Graphics2D imageContext = (Graphics2D) image.getGraphics();
			lvlData.draw(imageContext);
			if (isAnimShown) animation.draw(imageContext);
			drawButtons(imageContext);
			g2d.drawImage(image, 0, 0, pWidth, pHeight, null);
			imageContext.dispose();
		}
		g2d.dispose();
	}
	

	private void initFullScreen() { // standard procedure to get into FSEM

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		device = ge.getDefaultScreenDevice();

		setUndecorated(true); // no menu bar, borders, etc.
		setIgnoreRepaint(true); // turn off all paint events since doing active rendering
		setResizable(false); // screen cannot be resized

		if (!device.isFullScreenSupported()) {
			System.out.println("Full-screen exclusive mode not supported");
			System.exit(0);
		}

		device.setFullScreenWindow(this); // switch on full-screen exclusive mode

		// we can now adjust the display modes, if we wish

		showCurrentMode();

		pWidth = getBounds().width;
		pHeight = getBounds().height;

		System.out.println("Width of window is " + pWidth);
		System.out.println("Height of window is " + pHeight);

		try {
			createBufferStrategy(NUM_BUFFERS);
		} catch (Exception e) {
			System.out.println("Error while creating buffer strategy " + e);
			System.exit(0);
		}

		bufferStrategy = getBufferStrategy();
	}

	// This method provides details about the current display mode.

	private void showCurrentMode() {

		DisplayMode dm[] = device.getDisplayModes();

		for (int i = 0; i < dm.length; i++) {
			System.out.println("Current Display Mode: (" +
					dm[i].getWidth() + "," + dm[i].getHeight() + "," +
					dm[i].getBitDepth() + "," + dm[i].getRefreshRate() + ")  ");
		}

		// DisplayMode d = new DisplayMode (800, 600, 32, 60);
		// device.setDisplayMode(d);

		DisplayMode dm1 = device.getDisplayMode();

		dm1 = device.getDisplayMode();

		System.out.println("Current Display Mode: (" +
				dm1.getWidth() + "," + dm1.getHeight() + "," +
				dm1.getBitDepth() + "," + dm1.getRefreshRate() + ")  ");
	}

	// Specify screen areas for the buttons and create bounding rectangles

	private void setButtonAreas() {
		int leftOffset = (pWidth - (5 * 150) - (4 * 20)) / 2;
		pauseButtonArea = new Rectangle(leftOffset, 60, 150, 40);
	
		leftOffset += 170;
		stopButtonArea = new Rectangle(leftOffset, 60, 150, 40);
	
		leftOffset += 170;
		showAnimButtonArea = new Rectangle(leftOffset, 60, 150, 40);
	
		leftOffset += 170;
		pauseAnimButtonArea = new Rectangle(leftOffset, 60, 150, 40);
	
		leftOffset += 170;
		quitButtonArea = new Rectangle(leftOffset, 55, 180, 50);
	
		// Initialize start button area
		int startButtonWidth = 200;
		int startButtonHeight = 50;
		int startX = (pWidth - startButtonWidth) / 2;
		int startY = pHeight / 2;
		startButtonArea = new Rectangle(startX, startY, startButtonWidth, startButtonHeight);
	}
	

	private void drawButtons(Graphics g) {
		Font oldFont, newFont;

		oldFont = g.getFont(); // save current font to restore when finished

		newFont = new Font("Open Sans", Font.ITALIC + Font.BOLD, 18);
		g.setFont(newFont); // set this as font for text on buttons

		g.setColor(Color.black); // set outline colour of button

	
		//draw other info
		g.setColor(Color.BLACK);
		g.drawRect(25, pauseAnimButtonArea.y  + 100, pauseAnimButtonArea.width, pauseAnimButtonArea.height + 10);

		if (isOverPauseAnimButton && isAnimShown && !isPaused && !isStopped)
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.RED);


			//health and player ammo
			
			g.drawString("Helth: " + lvlData.getPlayer().getHealth(), 30, pauseAnimButtonArea.y + 125);
			g.drawString("Amo: " + lvlData.getPlayer().getAmmo(), 30, pauseAnimButtonArea.y + 145);

		g.setFont(oldFont); // reset font

	}

	private void startGame() {
		lvlData.loadLevel( initialLevelNum);
		if (gameThread == null) {
		
			gameThread = new Thread(this);
			gameThread.start();

		}
	}

	// displays a message to the screen when the user stops the game

	private void gameOverMessage(Graphics g) {

		Font font = new Font("SansSerif", Font.BOLD, 24);
		FontMetrics metrics = this.getFontMetrics(font);

		String msg = "Game Over. Thanks for playing!";

		int x = (pWidth - metrics.stringWidth(msg)) / 2;
		int y = (pHeight - metrics.getHeight()) / 2;

		g.setColor(Color.BLUE);
		g.setFont(font);
		g.drawString(msg, x, y);

	}

	// Respond to player key inputs
	public void captureInput() {
		// if(!playerDied) {
		if (leftKeyPressed) {
			lvlData.getTileMap().moveLeft();
		}
		if (rightKeyPressed) {
			lvlData.getTileMap().moveRight();
		}
		if (jumpKeypressed) {
			lvlData.getTileMap().jump();
		}
		if (attackKeyPressed) {
			lvlData.getTileMap().playerAttack();
		}
		if (dashKeyPressed) {
			lvlData.getTileMap().playerDash();
		}
	}

	// implementation of methods in KeyListener interface

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) || (keyCode == KeyEvent.VK_END)) {
			isRunning = false; // user can quit anytime by pressing
			return; // one of these keys (ESC, Q, END)
		}
		if (keyCode == KeyEvent.VK_LEFT) {
			leftKeyPressed = true;
		}
		if (keyCode == KeyEvent.VK_RIGHT) {
			rightKeyPressed = true;
		}
		if (keyCode == KeyEvent.VK_UP) {
			jumpKeypressed = true;
		}
		if (keyCode == KeyEvent.VK_A) {
			attackKeyPressed = true;
		}
		if (keyCode == KeyEvent.VK_D) {
			dashKeyPressed = true;
		}

	}

	
	public void keyReleased(KeyEvent e) {

		int keyCode = e.getKeyCode();

		// if left released
		if (keyCode == KeyEvent.VK_LEFT) {
			leftKeyPressed = false;
		}
		// if right released
		if (keyCode == KeyEvent.VK_RIGHT) {
			rightKeyPressed = false;
		}
		// if space released
		if (keyCode == KeyEvent.VK_UP) {
			jumpKeypressed = false;
		}
		// if attack released
		if (keyCode == KeyEvent.VK_A) {
			attackKeyPressed = false;
		}
		// if dash released
		if (keyCode == KeyEvent.VK_D) {
			dashKeyPressed = false;
		}

	}

	public void keyTyped(KeyEvent e) {

	}

	// implement methods of MouseListener interface

	public void mouseClicked(MouseEvent e) {
		if (isMenuVisible && startButtonArea.contains(e.getPoint())) {
            isMenuVisible = false; // Hide the menu and start the game
        }
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		testMousePress(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) {
		//request focus
		requestFocus();

		if (lvlData.getTileMap().isAboveCannon()) {
			//System.out.println("You can SHoot!: " + e.getX() + " y: " + e.getY());
			lvlData.getTileMap().setDirectionCannon(e.getX());
			lvlData.getTileMap().shootAllCannonBullets(e.getX(), e.getY());
			//lvlData.getTileMap().playerAttack();
			//shoot player bullet
			
		
	}
	// System.out.println("You CANNOT SHoot!: " + e.getX() + " y: " + e.getY());

		lvlData.getTileMap().playerShoot(e.getX(), e.getY());
	}

	// implement methods of MouseMotionListener interface

	public void mouseDragged(MouseEvent e) {

	}

	public void mouseMoved(MouseEvent e) {
		
		testMouseMove(e.getX(), e.getY());
	}

	/*
	 * This method handles mouse clicks on one of the buttons
	 * (Pause, Stop, Start Anim, Pause Anim, and Quit).
	 */

	private void testMousePress(int x, int y) {

		// if (isStopped && !isOverQuitButton) // don't do anything if game stopped
		// 	return;

		// if (isOverStopButton) { // mouse click on Stop button
		// 	isStopped = true;
		// 	isPaused = false;
		// } else if (isOverPauseButton) { // mouse click on Pause button
		// 	isPaused = !isPaused; // toggle pausing
		// } else if (isOverShowAnimButton && !isPaused) {// mouse click on Start Anim button
		// 	isAnimShown = true;
		// 	isAnimPaused = false;
		// 	animation.start();
		// } else if (isOverPauseAnimButton) { // mouse click on Pause Anim button
		// 	if (isAnimPaused) {
		// 		isAnimPaused = false;
		// 		animation.playSound();
		// 	} else {
		// 		isAnimPaused = true; // toggle pausing
		// 		animation.stopSound();
		// 	}
		// } else if (isOverQuitButton) { // mouse click on Quit button
		// 	isRunning = false; // set running to false to terminate
		// }
	}

	/*
	 * This method checks to see if the mouse is currently moving over one of
	 * the buttons (Pause, Stop, Show Anim, Pause Anim, and Quit). It sets a
	 * boolean value which will cause the button to be displayed accordingly.
	 */

	private void testMouseMove(int x, int y) {
		// if (isRunning) {
		// 	isOverPauseButton = pauseButtonArea.contains(x, y) ? true : false;
		// 	isOverStopButton = stopButtonArea.contains(x, y) ? true : false;
		// 	isOverShowAnimButton = showAnimButtonArea.contains(x, y) ? true : false;
		// 	isOverPauseAnimButton = pauseAnimButtonArea.contains(x, y) ? true : false;
		// 	isOverQuitButton = quitButtonArea.contains(x, y) ? true : false;
		// }
	}

}