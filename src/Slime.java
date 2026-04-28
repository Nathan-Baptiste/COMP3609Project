import java.awt.*;
import javax.swing.JPanel;

public class Slime {

    private static final int SPEED = 4;
    private static final int TILE_SIZE = 64;
    private static final double SCALE = 2;

    private JPanel panel;
    private TileMap tileMap;

    private int x, y;

    private boolean movingRight = true;

    // Gravity fields (mirroring Player logic)
    private boolean inAir = false;
    private boolean jumping = false;
    private int timeElapsed = 0;
    private int startY = 0;
    private boolean goingUp = false;
    private boolean goingDown = false;
    private int initialVelocity = 0;

    private int hp = 30;
    private int scoreValue = 10;

    private boolean gettingHit = false;

    private StripAnimation moveAnim;
    private StripAnimation hitAnim;

    public Slime(JPanel panel, TileMap tileMap, int x, int y) {
        this.panel = panel;
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;

        Image moveStrip = ImageManager.loadImage("src/images/Enemies/Slime/SlimeMove.png");
        moveAnim = new StripAnimation(moveStrip, 4, 0, 0, panel, true);

        Image hitStrip = ImageManager.loadImage("src/images/Enemies/Slime/SlimeHit.png");
        hitAnim = new StripAnimation(hitStrip, 1, 0, 0, panel, false);

        moveAnim.start();
        hitAnim.start();
    }

    public void update() {
        moveAnim.update();

        if (gettingHit) {
            hitAnim.update();
            if (hitAnim.isFinished()) {
                gettingHit = false;
            }
        }

        updateGravity();

        // Only walk horizontally when grounded
        if (!inAir && !jumping) {
            walk();
        }

        // Check if slime has walked off a ledge
        if (!inAir && !jumping && isInAir()) {
            fall();
        }
    }

    private void walk() {
        int newX = x;
        int width = getWidth();
        int height = getHeight();

        if (movingRight) {
            newX += SPEED;
            // Check wall collision on the right edge
            Point tile = collidesWithTileVertical(newX + width, y, height);
            if (tile != null) {
                // Flush against left face of tile, then reverse
                x = ((int) tile.getX()) * TILE_SIZE - width;
                movingRight = false;
            } else {
                x = newX;
            }
        } else {
            newX -= SPEED;
            // Check wall collision on the left edge
            Point tile = collidesWithTileVertical(newX, y, height);
            if (tile != null) {
                // Flush against right face of tile, then reverse
                x = ((int) tile.getX() + 1) * TILE_SIZE;
                movingRight = true;
            } else {
                x = newX;
            }
        }
    }

    private void updateGravity() {
        if (!jumping && !inAir) return;

        timeElapsed++;

        int distance = (int)(initialVelocity * timeElapsed - 3.0 * timeElapsed * timeElapsed);
        int newY = startY - distance;

        if (newY > y && goingUp) {
            goingUp = false;
            goingDown = true;
        }

        if (goingUp) {
            Point tile = collidesWithTileUp(x, newY);
            if (tile != null) {
                int offsetY = tileMap.getOffsetY();
                int bottomTileY = ((int) tile.getY()) * TILE_SIZE + offsetY + TILE_SIZE;
                y = bottomTileY;
                fall();
            } else {
                y = newY;
            }
        } else if (goingDown) {
            Point tile = collidesWithTileDown(x, newY);
            if (tile != null) {
                int offsetY = tileMap.getOffsetY();
                int topTileY = ((int) tile.getY()) * TILE_SIZE + offsetY;
                y = topTileY - getHeight();
                jumping = false;
                inAir = false;
                goingDown = false;
            } else {
                y = newY;
            }
        }
    }

    private void fall() {
        jumping = false;
        inAir = true;
        timeElapsed = 0;
        goingUp = false;
        goingDown = true;
        startY = y;
        initialVelocity = 0;
    }

    private boolean isInAir() {
        int width  = getWidth();
        int height = getHeight();

        Point left  = collidesWithTileAtPoint(x,             y + height + 1);
        Point right = collidesWithTileAtPoint(x + width - 1, y + height + 1);

        return (left == null && right == null);
    }

    private Point collidesWithTileAtPoint(int px, int py) {
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(px);
        int yTile = tileMap.pixelsToTiles(py - offsetY);
        if (tileMap.getTile(xTile, yTile) != null)
            return new Point(xTile, yTile);
        return null;
    }

    private Point collidesWithTileVertical(int px, int topY, int height) {
        int offsetY = tileMap.getOffsetY();
        int xTile    = tileMap.pixelsToTiles(px);
        int yTileTop = tileMap.pixelsToTiles(topY - offsetY);
        int yTileBot = tileMap.pixelsToTiles(topY - offsetY + height - 1);

        for (int yTile = yTileTop; yTile <= yTileBot; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null)
                return new Point(xTile, yTile);
        }
        return null;
    }

    private Point collidesWithTileDown(int px, int newY) {
        int width    = getWidth();
        int height   = getHeight();
        int offsetY  = tileMap.getOffsetY();
        int xTile    = tileMap.pixelsToTiles(px);
        int yTileFrom = tileMap.pixelsToTiles(y  - offsetY);
        int yTileTo   = tileMap.pixelsToTiles(newY - offsetY + height);

        for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null)
                return new Point(xTile, yTile);

            if (tileMap.getTile(xTile + 1, yTile) != null) {
                int leftSide = (xTile + 1) * TILE_SIZE;
                if (px + width > leftSide)
                    return new Point(xTile + 1, yTile);
            }
        }
        return null;
    }

    private Point collidesWithTileUp(int px, int newY) {
        int width     = getWidth();
        int offsetY   = tileMap.getOffsetY();
        int xTile     = tileMap.pixelsToTiles(px);
        int yTileFrom = tileMap.pixelsToTiles(y   - offsetY);
        int yTileTo   = tileMap.pixelsToTiles(newY - offsetY);

        for (int yTile = yTileFrom; yTile >= yTileTo; yTile--) {
            if (tileMap.getTile(xTile, yTile) != null)
                return new Point(xTile, yTile);

            if (tileMap.getTile(xTile + 1, yTile) != null) {
                int leftSide = (xTile + 1) * TILE_SIZE;
                if (px + width > leftSide)
                    return new Point(xTile + 1, yTile);
            }
        }
        return null;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        gettingHit = true;
        hitAnim.start();
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public Image getImage() {
        if (gettingHit) return hitAnim.getImage();
        return moveAnim.getImage();
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    public int getWidth()  { return (int)(moveAnim.getImage().getWidth(null)  * SCALE) - 25; }
    public int getHeight() { return (int)(moveAnim.getImage().getHeight(null) * SCALE) + 10; }

    public Rectangle getHitBox() { return new Rectangle(x, y, getWidth(), getHeight()); }

    public int getX() { return x; }
    public int getY() { return y; }
}