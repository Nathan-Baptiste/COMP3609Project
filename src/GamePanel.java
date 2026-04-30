import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
   A component that displays all the game entities
*/

public class GamePanel extends JPanel
		       implements Runnable {

	private GameWindow window;

	private SoundManager soundManager;

	private boolean isRunning;
	private boolean isPaused;

	private Thread gameThread;

	private BufferedImage image;
 	private Image backgroundImage;

	private long startTime;
	private int currentLevel = 1;

	// scores
	private int p1Score = 0;
	private int p2Score = 0;

	// coins
	private int p1Coins = 0;
	private int p2Coins = 0;

	private int p2RespawnTimer = -1;
	private static final int RESPAWN_TIME = 200; // ~10 seconds

	private boolean gameOver = false;
	private int fadeAlpha = 0;

	private BirdAnimation animation;
	private volatile boolean isAnimShown;
	private volatile boolean isAnimPaused;

	TileMapManager tileManager;
	TileMap	tileMap;

	public GamePanel (GameWindow window) {
		this.window = window;

		isRunning = false;
		isPaused = false;
		isAnimShown = false;
		isAnimPaused = false;

		soundManager = SoundManager.getInstance();

		image = new BufferedImage (1280, 720, BufferedImage.TYPE_INT_RGB);
	}


	public void createGameEntities() {
		animation = new BirdAnimation();
	}


	public void run () {
		try {
			isRunning = true;
			while (isRunning) {
				if (!isPaused)
					gameUpdate();
				gameRender();
				Thread.sleep (50);
			}
		}
		catch(InterruptedException e) {}
	}


	public void gameUpdate() {

		tileMap.update();
		window.movement();

		if (!isPaused && isAnimShown)
			animation.update();

		// Handle P2 respawn
		if (tileMap.getPlayer2().isDead()) {

			if (p2RespawnTimer == -1) {
				p2RespawnTimer = RESPAWN_TIME;
			}

			p2RespawnTimer--;

			if (p2RespawnTimer <= 0) {
				tileMap.respawnPlayer2();
				p2RespawnTimer = -1;
			}

		} else {
			p2RespawnTimer = -1; // reset if alive
		}

		// Game over check
		if (tileMap.getPlayer1().isDead()) {
			gameOver = true;
		}

		// Handle fade
		if (gameOver && fadeAlpha < 255) {
			fadeAlpha += 3; // speed of fade
		}

		if (fadeAlpha >= 255) {
			endGame();
		}

		if (tileMap.isLevelComplete()) {
			if (currentLevel == 1) {
				startLevel2();
			} else {
				endGame();
			}
			return;
		}
	}


	public void gameRender() {

		// draw the game objects on the image

		Graphics2D imageContext = (Graphics2D) image.getGraphics();

		Font hudFont = new Font("Monospaced", Font.BOLD, 20);
		imageContext.setFont(hudFont);

		tileMap.draw (imageContext);

		if (isAnimShown)
			animation.draw(imageContext);		// draw the animation

		drawHUD(imageContext);

		if (gameOver) {
			Graphics2D g = (Graphics2D) image.getGraphics();

			g.setColor(new Color(0, 0, 0, fadeAlpha));
			g.fillRect(0, 0, 1280, 720);

			if (fadeAlpha >= 200) {
				g.setFont(new Font("Monospaced", Font.BOLD, 60));
				drawOutlinedString(g, "GAME OVER", 450, 360);
			}

			g.dispose();
		}

		Graphics2D g2 = (Graphics2D) getGraphics();	// get the graphics context for the panel
		g2.drawImage(image, 0, 0, 1280, 720, null);	// draw the image on the graphics context

		imageContext.dispose();
	}


	public void startGame() {				// initialise and start the game thread

		currentLevel = 1;

		startTime = System.currentTimeMillis();

		if (gameThread == null) {
			//soundManager.playSound ("background", true);

			tileManager = new TileMapManager (this);

			try {
				tileMap = tileManager.loadMap("src/maps/map1.txt", 1);
				int w, h;
				w = tileMap.getWidth();
				h = tileMap.getHeight();
			}
			catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}

			createGameEntities();

			gameThread = new Thread(this);
			gameThread.start();			

		}
	}


	public void startLevel2() {				// initialise and start a new game thread
		currentLevel = 2;

		tileManager = new TileMapManager(this);
		try {
			tileMap = tileManager.loadMap("src/maps/map2.txt", 2);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}

		gameOver = false;
		fadeAlpha = 0;
		p2RespawnTimer = -1;
	}


	public void pauseGame() {				// pause the game (don't update game entities)
		if (isRunning) {
			if (isPaused)
				isPaused = false;
			else
				isPaused = true;

			if (isAnimShown) {
				if (isPaused)
					animation.stopSound();
				else
					animation.playSound();
			}
		}
	}


	public void endGame() {					// end the game thread
		isRunning = false;
		//soundManager.stopClip ("background");
	}


	// Player 1
	public void moveLeftP1() { tileMap.moveLeftP1(); }
	public void moveRightP1() { tileMap.moveRightP1(); }
	public void jumpP1() { tileMap.jumpP1(); }
	public void attackP1() { tileMap.player1Attack(); }
	public void player1Block() { tileMap.getPlayer1().startBlock();	}
	public void stopPlayer1Block() { tileMap.getPlayer1().stopBlock(); }

	// Player 2
	public void moveLeftP2() { tileMap.moveLeftP2(); }
	public void moveRightP2() { tileMap.moveRightP2(); }
	public void jumpP2() { tileMap.jumpP2(); }
	public void startChargeP2() { tileMap.player2StartCharge(); }
	public void releaseShootP2() { tileMap.player2Shoot(); }
	public void player2Block() { tileMap.getPlayer2().startBlock(); }
	public void stopPlayer2Block() { tileMap.getPlayer2().stopBlock(); }

	private void drawHUD(Graphics2D g) {

		g.setFont(new Font("Monospaced", Font.BOLD, 20));

		// Timer
		long elapsed = System.currentTimeMillis() - startTime;

		int hours = (int)(elapsed / 3600000);
		int minutes = (int)((elapsed / 60000) % 60);
		int seconds = (int)((elapsed / 1000) % 60);

		String timer = String.format("%02d:%02d:%02d", hours, minutes, seconds);

		// center top
		drawOutlinedString(g, timer, 640 - 40, 30);

		// Player 1
		drawOutlinedString(g, "P1: " + String.format("%08d", tileMap.getP1Score()), 20, 30);
		drawOutlinedString(g, "Coins: " + tileMap.getP1Coins(), 20, 60);

		drawHealth(g, 20, 90, tileMap.getPlayer1Health());

		// Player 2
		drawOutlinedString(g, "P2: " + String.format("%08d", tileMap.getP2Score()), 1050, 30);
		drawOutlinedString(g, "Coins: " + tileMap.getP2Coins(), 1050, 60);

		if (tileMap.getPlayer2().isDead()) {
			int secs = p2RespawnTimer / 20 + 1;
			drawOutlinedString(g, "Respawn: " + secs, 1050, 90);
		} else {
			drawHealth(g, 1050, 90, tileMap.getPlayer2Health());

		}
	}

	private void drawHealth(Graphics2D g, int x, int y, int health) {

		// label
		drawOutlinedString(g, "Health:", x, y);

		int offsetX = x + 90;

		for (int i = 0; i < 5; i++) {

			int sx = offsetX + (i * 18);
			int sy = y - 12;
			int size = 12;

			if (i < health) {
				// green health square
				g.setColor(Color.GREEN);
				g.fillRect(sx, sy, size, size);
			} else {
				// empty square
				g.setColor(Color.DARK_GRAY);
				g.fillRect(sx, sy, size, size);
			}

			// BLACK outline for ALL squares
			g.setColor(Color.BLACK);
			g.drawRect(sx, sy, size, size);
		}
	}

	private void drawOutlinedString(Graphics2D g, String text, int x, int y) {

		g.setColor(Color.BLACK);
		g.drawString(text, x - 1, y);
		g.drawString(text, x + 1, y);
		g.drawString(text, x, y - 1);
		g.drawString(text, x, y + 1);

		g.setColor(Color.WHITE);
		g.drawString(text, x, y);
	}

	
	public void showAnimation() {
		isAnimShown = true;
		animation.start();
		
	}

}