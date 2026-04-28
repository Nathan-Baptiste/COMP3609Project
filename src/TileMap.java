import java.awt.*;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;
import java.util.ArrayList;

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
    private static final int MAX_ARROWS = 5;

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
    private ArrayList<Arrow> arrows = new ArrayList<>();

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
        player1.fall();

        player2.setX(x + 20); // offset Player2
        player2.setY(y);
        player2.fall();

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

        int p2Offset = -10;
        if ((player2.jumping || player2.inAir) && player2.charging && player2.facingRight) { //align jump charging right
            p2Offset += -5;
        } else if ((player2.jumping || player2.inAir) && player2.charging && !player2.facingRight) { //align jump charging left
            p2Offset += -16;
        } else if ((player2.jumping || player2.inAir) && player2.shooting && player2.facingRight) { //align jump shooting right
            p2Offset += -8;
        } else if ((player2.jumping || player2.inAir) && player2.shooting && !player2.facingRight) { //align jump shooting left
            p2Offset += -10;
        } else if (player2.charging && player2.facingRight) { // align charging right sprites
            p2Offset += -4;
        } else if (player2.charging && !player2.facingRight) { //align charging left sprites
            p2Offset += -16;
        } else if (player2.shooting && player2.facingRight) { // align shooting right sprites
            p2Offset += -6;
        } else if (player2.shooting && !player2.facingRight) { //align shooting left sprites
            p2Offset += -14;
        } else if ((player2.jumping || player2.inAir) && player2.moving && player2.facingRight) { //align jumping and moving right sprites
            p2Offset += 10;
        } else if ((player2.jumping || player2.inAir) && player2.moving && !player2.facingRight) { //align jumping and moving left sprites
            p2Offset += 12;
        } else if ((player2.jumping || player2.inAir) && player2.facingRight) { //align jumping right sprites
            p2Offset += 10;
        } else if ((player2.jumping || player2.inAir) && !player2.facingRight) { //align jumping left sprites
            p2Offset += 15;
        }else if (!player2.facingRight && player2.moving) //align moving left sprites
            p2Offset += -15;
        else if (!player2.facingRight) //align idle left sprites
            p2Offset += -8;
        else if (player2.moving) //align moving right sprites.
            p2Offset += -35;
        int p2X = Math.round(player2.getX()) + offsetX + p2Offset - 25;
        int p2Y = Math.round(player2.getY());

        int drawY2;
        if (player2.moving)
            drawY2 = p2Y + 47 + (img2.getHeight(null) - h2);
        else
            drawY2 = p2Y + 45 + (img2.getHeight(null) - h2);

        if (player2.facingRight) {
            g2.drawImage(img2, p2X, drawY2, w2, h2, null);
        } else {
            g2.drawImage(img2, p2X + w2, drawY2, -w2, h2, null);
        }

        // HITBOX
        Rectangle p2Box = player2.getHitBox();

        g2.setColor(Color.BLUE);
        g2.drawRect(
                p2Box.x + offsetX,
                p2Box.y,
                p2Box.width,
                p2Box.height
        );


        //Player 1
        Image img1 = player1.getImage();

        int w1 = (int)(img1.getWidth(null) * SCALE);
        int h1 = (int)(img1.getHeight(null) * SCALE);

        int p1Offset = -15;
        if (player1.attacking && player1.facingRight) { // allign attacking right sprites
            p1Offset += -30;
        } else if (player1.attacking && !player1.facingRight) { //align attacking left sprites
            p1Offset += -100;
        } else if (player1.moveAttacking && player1.facingRight) { //align right move attack sprites
            p1Offset += -25;
        } else if (player1.moveAttacking && !player1.facingRight) { //align left move attack sprites
            p1Offset += -100;
        }else if (player1.jumpAttacking && player1.facingRight) { //align right jump attack sprites
            p1Offset += -28;
        } else if (player1.jumpAttacking && !player1.facingRight) { //align left jump attack sprites
            p1Offset += -70;
        } else if ((player1.jumping || player1.inAir) && player1.moving && player1.facingRight) { //align jumping and moving right sprites
            p1Offset += 10;
        } else if ((player1.jumping || player1.inAir) && player1.facingRight) { //align jumping right sprites
            p1Offset += 10;
        } else if ((player1.jumping || player1.inAir) && !player1.facingRight) { //align jumping left sprites
            p1Offset += 5;
        } else if (!player1.facingRight && player1.moving) { //align moving left sprites
            p1Offset += -80;
        } else if (!player1.facingRight) { //align idle left sprites
            p1Offset += -28;
        } else if (player1.moving) { //align moving right sprites
            p1Offset += -10;
        }
        int p1X = Math.round(player1.getX()) + offsetX + p1Offset - 25;

        int p1Y = Math.round(player1.getY());

        int drawY = p1Y + 45 + (img1.getHeight(null) - h1);

        if (player1.facingRight) {
            g2.drawImage(img1, p1X, drawY, w1, h1, null);
        } else {
            g2.drawImage(img1, p1X + w1, drawY, -w1, h1, null);
        }

        // HITBOX
        Rectangle p1Box = player1.getHitBox();

        g2.setColor(Color.RED);
        g2.drawRect(
                p1Box.x + offsetX,
                p1Box.y,
                p1Box.width,
                p1Box.height
        );

        //ATTACK HITBOX
        if ((player1.attacking || player1.moveAttacking || player1.jumpAttacking)
                && player1.isAttackActiveFrame()) {

            int hitWidth = 40;
            int hitHeight = 30;

            int hitX = player1.getX();
            int hitY = player1.getY();

            // adjust vertical hitbox for air attack
            if (player1.jumpAttacking) {
                hitHeight = 35;
                hitY = player1.getY() - 10; // slightly above player
            }

            // move attack = slightly longer range
            if (player1.moveAttacking) {
                hitWidth = 55;
                hitHeight = 30;
            }

            // direction handling (IMPORTANT)
            if (player1.facingRight) {
                hitX = player1.getX() + player1.getDisplayWidth();
            } else {
                hitX = player1.getX() - hitWidth;
            }

            Rectangle attackBox = new Rectangle(hitX, hitY, hitWidth, hitHeight);

            // color per type (helps debugging)
            if (player1.jumpAttacking)
                g2.setColor(Color.CYAN);
            else if (player1.moveAttacking)
                g2.setColor(Color.ORANGE);
            else
                g2.setColor(Color.YELLOW);

            g2.drawRect(
                    attackBox.x + offsetX,
                    attackBox.y,
                    attackBox.width,
                    attackBox.height
            );
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

        for (Arrow a : new ArrayList<>(arrows)) {
            a.draw(g2);
        }
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

    //Player 1
    public void player1Attack() {
        player1.attack();
    }

    //Player 2
    public void player2StartCharge() {
        player2.startCharge();
    }

    public void player2Shoot() {
        player2.releaseShoot();
    }

    public void spawnArrow(int x, int y, boolean facingRight) {
        if (arrows.size() >= MAX_ARROWS) {
            arrows.remove(0); // remove oldest arrow
        }

        Image arrowImg = ImageManager.loadImage("src/images/ItemsandObjects/Arrow.png");
        arrows.add(new Arrow(x, y, facingRight, arrowImg));
    }

    private void updateArrows() {
        for (int i = arrows.size() - 1; i >= 0; i--) {
            Arrow a = arrows.get(i);
            a.update();

            if (a.getX() < 0 || a.getX() > screenWidth) {
                arrows.remove(i);
            }
        }
    }

    public void update() {
        player1.update();
        player2.update();
        updateArrows();

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
