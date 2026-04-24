import java.awt.*;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;

/**
    The TileMap class contains the data for a tile-based
    map, including Sprites. Each tile is a reference to an
    Image. Images are used multiple times in the tile map.
    map.
*/

public class TileMap {

    private static final int TILE_SIZE = 64;
    private static final int TILE_SIZE_BITS = 6;
    protected static final double SCALE = 2;

    private int p2OffScreenTime = 0;
    private static final int RESPAWN_TIME = 100; // ~5 seconds (since 50ms per frame)

    private Image[][] tiles;
    private int screenWidth, screenHeight;
    private int mapWidth, mapHeight;
    private int offsetY;

    private LinkedList sprites;
    private Player1 player1;
    private Player2 player2;

    BackgroundManager bgManager;

    private JPanel panel;
    private Dimension dimension;

    /**
        Creates a new TileMap with the specified width and
        height (in number of tiles) of the map.
    */
    public TileMap(JPanel panel, int width, int height) {

	this.panel = panel;
	dimension = panel.getSize();

	screenWidth = dimension.width;
	screenHeight = dimension.height;

	System.out.println ("Width: " + screenWidth);
	System.out.println ("Height: " + screenHeight);


	mapWidth = width;
	mapHeight = height;

        // get the y offset to draw all sprites and tiles

       	offsetY = screenHeight - tilesToPixels(mapHeight);
	System.out.println("offsetY: " + offsetY);

	bgManager = new BackgroundManager (panel, 12);

        tiles = new Image[mapWidth][mapHeight];
        player1 = new Player1(panel, this, bgManager);
        player2 = new Player2(panel, this, bgManager);
        sprites = new LinkedList();

	Image playerImage = player1.getImage();
	int playerHeight = playerImage.getHeight(null);

	int x, y;
	x = (dimension.width / 2) + TILE_SIZE;		// position player in middle of screen

	//x = 192;					// position player in 'random' location
    y = (int) (dimension.height - (TILE_SIZE + playerImage.getHeight(null) * SCALE));

        player1.setX(x);
        player1.setY(y);

        player2.setX(x + 20); // offset Player2
        player2.setY(y);

	System.out.println("Player coordinates: " + x + "," + y);

    }


    /**
        Gets the width of this TileMap (number of pixels across).
    */
    public int getWidthPixels() {
	return tilesToPixels(mapWidth);
    }


    /**
        Gets the width of this TileMap (number of tiles across).
    */
    public int getWidth() {
        return mapWidth;
    }


    /**
        Gets the height of this TileMap (number of tiles down).
    */
    public int getHeight() {
        return mapHeight;
    }


    public int getOffsetY() {
	return offsetY;
    }

    /**
        Gets the tile at the specified location. Returns null if
        no tile is at the location or if the location is out of
        bounds.
    */
    public Image getTile(int x, int y) {
        if (x < 0 || x >= mapWidth ||
            y < 0 || y >= mapHeight)
        {
            return null;
        }
        else {
            return tiles[x][y];
        }
    }


    /**
        Sets the tile at the specified location.
    */
    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }


    /**
        Gets an Iterator of all the Sprites in this map,
        excluding the player Sprite.
    */

    public Iterator getSprites() {
        return sprites.iterator();
    }

    /**
        Class method to convert a pixel position to a tile position.
    */

    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }


    /**
        Class method to convert a pixel position to a tile position.
    */

    public static int pixelsToTiles(int pixels) {
        return (int)Math.floor((float)pixels / TILE_SIZE);
    }


    /**
        Class method to convert a tile position to a pixel position.
    */

    public static int tilesToPixels(int numTiles) {
        return numTiles * TILE_SIZE;
    }

    /**
        Draws the specified TileMap.
    */
    public void draw(Graphics2D g2)
    {
        int mapWidthPixels = tilesToPixels(mapWidth);

        // get the scrolling position of the map
        // based on player's position

        int offsetX = screenWidth / 2 -
                Math.round(player1.getX()) - TILE_SIZE;

        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

/*
        // draw black background, if needed
        if (background == null ||
            screenHeight > background.getHeight(null))
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, screenWidth, screenHeight);
        }
*/
	// draw the background first

	bgManager.draw (g2);

        // draw the visible tiles

        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
        for (int y=0; y<mapHeight; y++) {
            for (int x=firstTileX; x <= lastTileX; x++) {
                Image image = getTile(x, y);
                if (image != null) {
                    g2.drawImage(image,
                        tilesToPixels(x) + offsetX,
                        tilesToPixels(y) + offsetY,
                        null);
                }
            }
        }


        // draw player

        //Player 2
        Image img2 = player2.getImage();
        int w2 = (int)(img2.getWidth(null) * SCALE);
        int h2 = (int)(img2.getHeight(null) * SCALE);

        int p2X = Math.round(player2.getX()) + offsetX;
        int p2Y = Math.round(player2.getY());

        int drawY2 = p2Y + (img2.getHeight(null) - h2);

        if (player2.facingRight) {
            g2.drawImage(img2, p2X, drawY2, w2, h2, null);
        } else {
            g2.drawImage(img2, p2X + w2, drawY2, -w2, h2, null);
        }

        //Player 1
        Image img1 = player1.getImage();

        int w1 = (int)(img1.getWidth(null) * SCALE);
        int h1 = (int)(img1.getHeight(null) * SCALE);

        int p1X = Math.round(player1.getX()) + offsetX;
        int p1Y = Math.round(player1.getY());

        int drawY = p1Y + (img1.getHeight(null) - h1);

        if (player1.facingRight) {
            g2.drawImage(img1, p1X, drawY, w1, h1, null);
        } else {
            g2.drawImage(img1, p1X + w1, drawY, -w1, h1, null);
        }




        //Visible Countdown
        if (!isPlayer2OnScreen(offsetX)) {

            int drawX;

            int seconds = (RESPAWN_TIME - p2OffScreenTime) / 20 + 1;

            int p2ScreenX = Math.round(player2.getX()) + offsetX;
            int p2ScreenY = Math.round(player2.getY());

            if (p2ScreenY < 20) p2ScreenY = 20;
            if (p2ScreenY > screenHeight - 20) p2ScreenY = screenHeight - 20;

            if (p2ScreenX < 0) {
                drawX = 10; // left edge
            } else {
                drawX = screenWidth - 30; // right edge
            }

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("" + seconds, drawX, p2ScreenY);
        }
/*
        // draw sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            int x = Math.round(sprite.getX()) + offsetX;
            int y = Math.round(sprite.getY()) + offsetY;
            g.drawImage(sprite.getImage(), x, y, null);

            // wake up the creature when it's on screen
            if (sprite instanceof Creature &&
                x >= 0 && x < screenWidth)
            {
                ((Creature)sprite).wakeUp();
            }
        }
*/

    }

    public boolean isPlayer2OnScreen(int offsetX) {

        int screenX = player2.getX() + offsetX;

        return (screenX >= 0 && screenX <= screenWidth);
    }


    //Player 1
    public void moveLeftP1() {
        int x = player1.getX();
        int y = player1.getY();

        System.out.println("P1 Left: x=" + x + " y=" + y);

        player1.move(1);
    }


    public void moveRightP1() {
        int x = player1.getX();
        int y = player1.getY();

        System.out.println("P1 Right: x=" + x + " y=" + y);

        player1.move(2);
    }

    //Player 2
    public void moveLeftP2() {
        int x = player2.getX();
        int y = player2.getY();

        System.out.println("P2 Left: x=" + x + " y=" + y);

        player2.move(1);
    }

    public void moveRightP2() {
        int x = player2.getX();
        int y = player2.getY();

        System.out.println("P2 Right: x=" + x + " y=" + y);

        player2.move(2);
    }


    // Player 1
    public void jumpP1() {
        int x = player1.getX();
        int y = player1.getY();

        System.out.println("P1 Jump: x=" + x + " y=" + y);

        player1.move(3);
    }

    // Player 2
    public void jumpP2() {
        int x = player2.getX();
        int y = player2.getY();

        System.out.println("P2 Jump: x=" + x + " y=" + y);

        player2.move(3);
    }


    public void update() {
        player1.update();
        player2.update();

        int mapWidthPixels = tilesToPixels(mapWidth);

        int offsetX = screenWidth / 2 -
                Math.round(player1.getX()) - TILE_SIZE;

        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

        // check if player2 is visible
        if (!isPlayer2OnScreen(offsetX)) {
            p2OffScreenTime++;

            if (p2OffScreenTime >= RESPAWN_TIME) {
                respawnPlayer2();
                p2OffScreenTime = 0;
            }
        } else {
            p2OffScreenTime = 0;
        }
    }

    private void respawnPlayer2() {

        int offset = 20; // distance from Player1

        int newX = player1.getX() + offset;
        int newY = player1.getY();

        player2.respawn(newX, newY);

        System.out.println("Player2 respawned next to Player1");
    }

}
