import java.awt.*;
import java.util.Random;
import javax.swing.JPanel;

public class Slime {

    private static final int SPEED = 4;
    private static final int TILE_SIZE = 64;
    private static final double SCALE = 2;
    private static final int HIT_COOLDOWN_TIME = 6; // ~1 second (adjust if needed)
    private static final int HIT_TIME = 10; // frames to show hit sprite

    protected int attackSoundCooldown = 0;

    private JPanel panel;
    private TileMap tileMap;

    private int x, y;

    private boolean movingRight = true;

    private Random rand = new Random();
    private int moveSoundCooldown = 0;

    private boolean inAir = false;
    private boolean jumping = false;
    private int timeElapsed = 0;
    private int startY = 0;
    private boolean goingUp = false;
    private boolean goingDown = false;
    private int initialVelocity = 0;
    private int hitCooldown = 0;
    private int hitTimer = 0;

    private int hp = 80;
    private int damage = 1; // how much damage slime does
    private int scoreValue = 1000;

    private boolean gettingHit = false;
    private boolean killedByArrow = false;

    private SoundManager soundManager;

    private StripAnimation moveAnim;
    private Image hitImage;

    public Slime(JPanel panel, TileMap tileMap, int x, int y) {
        this.panel = panel;
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;

        soundManager = SoundManager.getInstance();

        Image moveStrip = ImageManager.loadImage("src/images/Enemies/Slime/SlimeMove.png");
        moveAnim = new StripAnimation(moveStrip, 4, 0, 0, panel, true);

        hitImage = ImageManager.loadImage("src/images/Enemies/Slime/SlimeHit.png");

        moveAnim.start();
    }

    public void update() {
        moveAnim.update();

        if (attackSoundCooldown > 0)
            attackSoundCooldown--;

        if (moveSoundCooldown > 0) {
            moveSoundCooldown--;
        }

        if (y > tileMap.getOffsetY() + tileMap.tilesToPixels(tileMap.getHeight()) + 100) {
            hp = 0;
            return;
        }

        if (hitCooldown > 0) {
            hitCooldown--;
        }

        if (hitTimer > 0) {
            hitTimer--;
            if (hitTimer == 0) {
                gettingHit = false;
            }
        }

        updateGravity();

        if (!inAir && !jumping && !gettingHit) {
            walk();
        }

        if (!inAir && !jumping && isInAir()) {
            fall();
        }
    }

    private void walk() {
        int newX = x;
        int width = getWidth();
        int height = getHeight();

        if (moveSoundCooldown == 0) {

            int chance = rand.nextInt(100); // 0 - 99

            if (chance < 3) {
                soundManager.playSound("slimeMove", false);
                moveSoundCooldown = 60;
            }
        }

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
        int yTileFrom = tileMap.pixelsToTiles((y + height) - offsetY);
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

    public boolean isDead() {
        return hp <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public Image getImage() {
        if (gettingHit) return hitImage;
        return moveAnim.getImage();
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    public void takeDamage(int damage, boolean hitFromRight, boolean fromArrow) {
        if (hitCooldown > 0) return;
        if (fromArrow) killedByArrow = true;

        soundManager.playSound("slimeHit", false);

        hp -= damage;
        movingRight = hitFromRight;
        gettingHit = true;
        hitTimer = HIT_TIME;
        hitCooldown = HIT_COOLDOWN_TIME;

        int knockback = 20;
        int dx = hitFromRight ? -knockback : knockback;
        int newX = x + dx;
        int width = getWidth();
        int height = getHeight();

        if (dx > 0) {
            Point tile = collidesWithTileVertical(newX + width, newX, height);
            if (tile != null) newX = (int)(tile.getX() + 1) * TILE_SIZE;
        } else {
            Point tile = collidesWithTileVertical(newX, newX, height);
            if (tile != null) newX = (int)tile.getX() * TILE_SIZE - width;
        }

        x = newX;
    }

    public int getWidth()  { return (int)(moveAnim.getImage().getWidth(null)  * SCALE) - 25; }
    public int getHeight() { return (int)(moveAnim.getImage().getHeight(null) * SCALE) + 10; }

    public Rectangle getHitBox() { return new Rectangle(x, y, getWidth(), getHeight()); }

    public int getX() { return x; }
    public int getY() { return y; }

    public int getDamage() {
        return damage;
    }

    public boolean isGettingHit() { return gettingHit; }
    public boolean wasKilledByArrow() { return killedByArrow; }
}