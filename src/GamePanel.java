import javax.swing.JPanel;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

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
	}


	public void gameRender() {

		// draw the game objects on the image

		Graphics2D imageContext = (Graphics2D) image.getGraphics();

		tileMap.draw (imageContext);

		if (isAnimShown)
			animation.draw(imageContext);		// draw the animation

		Graphics2D g2 = (Graphics2D) getGraphics();	// get the graphics context for the panel
		g2.drawImage(image, 0, 0, 1280, 720, null);	// draw the image on the graphics context

		imageContext.dispose();
	}


	public void startGame() {				// initialise and start the game thread 

		if (gameThread == null) {
			//soundManager.playSound ("background", true);

			tileManager = new TileMapManager (this);

			try {
				tileMap = tileManager.loadMap("src/maps/map1.txt");
				int w, h;
				w = tileMap.getWidth();
				h = tileMap.getHeight();
				System.out.println ("Width of tilemap " + w);
				System.out.println ("Height of tilemap " + h);
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
		if (gameThread != null || !isRunning) {
			//soundManager.playSound ("background", true);

			tileManager = new TileMapManager (this);

			try {
				tileMap = tileManager.loadMap("src/maps/map2.txt");
				int w, h;
				w = tileMap.getWidth();
				h = tileMap.getHeight();
				System.out.println ("Width of tilemap " + w);
				System.out.println ("Height of tilemap " + h);
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

	// Player 2
	public void moveLeftP2() { tileMap.moveLeftP2(); }
	public void moveRightP2() { tileMap.moveRightP2(); }
	public void jumpP2() { tileMap.jumpP2(); }

	
	public void showAnimation() {
		isAnimShown = true;
		animation.start();
		
	}

}