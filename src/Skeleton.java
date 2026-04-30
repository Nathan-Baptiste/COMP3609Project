import java.awt.*;
import javax.swing.JPanel;

public class Skeleton {

    private static final int SHOOT_INTERVAL = 20; // ~3 seconds at 60fps
    private static final double SCALE = 2;
    private static final int TILE_SIZE = 64;

    private int x, y;
    private int hp = 100;

    private boolean facingRight = true;

    private int shootTimer = 0;
    private int hitCooldown = 0;
    private int hitTimer = 0;

    protected boolean gettingHit = false;
    protected boolean charging = false;
    protected boolean shooting = false;

    private JPanel panel;
    private TileMap tileMap;


    private StripAnimation idleAnim;
    private StripAnimation shootAnim;
    private StripAnimation chargeAnim;

    private boolean inAir = false;
    private boolean jumping = false;

    private int timeElapsed = 0;
    private int startY = 0;
    private int initialVelocity = 0;

    private boolean goingUp = false;
    private boolean goingDown = false;

    private Image hitImage;

    public Skeleton(JPanel panel, TileMap tileMap, int x, int y) {
        this.panel = panel;
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;

        Image idleStrip = ImageManager.loadImage("src/images/Enemies/Skeleton/SkeletonIdle.png");
        idleAnim = new StripAnimation(idleStrip, 4, 0, 0, panel, true);

        Image shootStrip = ImageManager.loadImage("src/images/Enemies/Skeleton/SkeletonShoot.png");
        shootAnim = new StripAnimation(shootStrip, 6, 0, 0, panel, false);

        Image chargeStrip = ImageManager.loadImage("src/images/Enemies/Skeleton/SkeletonChargeBow.png");
        chargeAnim = new StripAnimation(chargeStrip, 7, 0, 0, panel, false);

        hitImage = ImageManager.loadImage("src/images/Enemies/Skeleton/SkeletonHit.png");

        idleAnim.start();
        shootAnim.start();
        chargeAnim.start();
    }

    public void update() {

        idleAnim.update();
        shootAnim.update();
        chargeAnim.update();
        updateFacing();

        if (y > tileMap.getOffsetY() + tileMap.tilesToPixels(tileMap.getHeight()) + 100) {
            hp = 0;
            return;
        }

        Player target = getClosestPlayer();
        if (target == null) {
            shootTimer = 0;
            charging = false;
            shooting = false;
            return;
        }

        if (hitCooldown > 0) hitCooldown--;
        if (hitTimer > 0 && --hitTimer == 0) gettingHit = false;

        // ONLY shoot if on screen
        if (!isOnScreen()) {
            shootTimer = 0;
            charging = false;
            shooting = false;
            return;
        }

        shootTimer++;

        if (shootTimer == SHOOT_INTERVAL && !gettingHit && getClosestPlayer() != null) {

            if (isPlayerOnSameLevel()) {
                charging = true;
                chargeAnim.start();
            } else {
                // skip shot if not aligned
                shootTimer = 0;
            }
        }

        if (charging && shootTimer >= SHOOT_INTERVAL + 20 && !gettingHit) {
            charging = false;
            shootTimer = 0;
            shoot();
        }

        if (shooting && shootTimer > 10) {
            shooting = false;
        }

        updateGravity();
    }

    private void updateFacing() {
        Player target = getClosestPlayer();
        if (target == null) return;
        facingRight = target.getX() > x;
    }

    private Player getClosestPlayer() {

        Player p1 = tileMap.getPlayer1();
        Player p2 = tileMap.getPlayer2();

        boolean p1Alive = !p1.isDead();
        boolean p2Alive = !p2.isDead();

        // If both dead → return null (no targeting)
        if (!p1Alive && !p2Alive) return null;

        // If only one is alive → always target them
        if (p1Alive && !p2Alive) return p1;
        if (!p1Alive && p2Alive) return p2;

        // Both alive → choose closest
        int p1Dist = Math.abs(p1.getX() - x);
        int p2Dist = Math.abs(p2.getX() - x);

        return (p1Dist < p2Dist) ? p1 : p2;
    }


    private void shoot() {
        charging = false;
        shooting = true;

        tileMap.spawnEnemyArrow(x, y + 20, facingRight);
        shootAnim.start();

        // stop charge immediately after shot
        chargeAnim.start(); // optional reset
    }

    public void takeDamage(int dmg, boolean hitFromRight) {
        if (hitCooldown > 0) return;

        hp -= dmg;
        gettingHit = true;
        hitTimer = 10;
        hitCooldown = 6;
        charging = false;
        shooting = false;
        shootTimer = 0;

        int knockback = 25;
        int dx = hitFromRight ? -knockback : knockback;
        int newX = x + dx;
        int width = getWidth();
        int height = getHeight();

        if (dx > 0) {
            Point tile = collidesWithTileVertical(newX + width, y + 5, height - 10);
            if (tile != null) newX = (int)(tile.getX() + 1) * TILE_SIZE;
        } else {
            Point tile = collidesWithTileVertical(newX, y + 5, height - 10);
            if (tile != null) newX = (int)tile.getX() * TILE_SIZE - width;
        }

        x = newX;
    }

    private void updateGravity() {

        if (!jumping && !inAir) {
            if (isInAir()) {
                fall();
            }
            return;
        }

        timeElapsed++;

        int distance = (int)(initialVelocity * timeElapsed - 3.0 * timeElapsed * timeElapsed);
        int newY = startY - distance;

        if (goingDown) {

            Point tile = collidesWithTileDown(x, newY);

            if (tile != null) {
                int offsetY = tileMap.getOffsetY();
                int topTileY = tile.y * 64 + offsetY;

                y = topTileY - getHeight();

                inAir = false;
                jumping = false;
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
        startY = y;
        initialVelocity = 0;
        goingDown = true;
    }

    private boolean isInAir() {
        int width  = getWidth();
        int height = getHeight();

        Point left  = collidesWithTileAtPoint(x,             y + height + 1);
        Point right = collidesWithTileAtPoint(x + width - 1, y + height + 1);

        return (left == null && right == null);
    }

    private Point collidesWithTileVertical(int px, int topY, int height) {
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(px);
        int yTileTop = tileMap.pixelsToTiles(topY - offsetY);
        int yTileBot = tileMap.pixelsToTiles(topY - offsetY + height);

        for (int yTile = yTileTop; yTile <= yTileBot; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null)
                return new Point(xTile, yTile);
        }
        return null;
    }

    private Point collidesWithTileAtPoint(int px, int py) {
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(px);
        int yTile = tileMap.pixelsToTiles(py - offsetY);

        if (tileMap.getTile(xTile, yTile) != null)
            return new Point(xTile, yTile);

        return null;
    }

    private Point collidesWithTileDown(int px, int newY) {
        int width   = getWidth();
        int height  = getHeight();
        int offsetY = tileMap.getOffsetY();

        int feetY     = y + height;
        int yTileFrom = tileMap.pixelsToTiles(feetY - offsetY) + 1;
        int yTileTo   = tileMap.pixelsToTiles(newY + height - offsetY);

        int xLeft  = tileMap.pixelsToTiles(px);
        int xRight = tileMap.pixelsToTiles(px + width - 1);

        for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
            if (tileMap.getTile(xLeft,  yTile) != null) return new Point(xLeft,  yTile);
            if (xRight != xLeft && tileMap.getTile(xRight, yTile) != null) return new Point(xRight, yTile);
        }
        return null;
    }

    public Image getImage() {

        if (gettingHit)
            return hitImage;
        else if (shooting)
            return shootAnim.getImage();
        else if (charging)
            return chargeAnim.getImage();
        else
            return idleAnim.getImage();
    }

    public Rectangle getHitBox() {
        int w = getWidth();
        int h = getHeight();
        return new Rectangle(x, y, w, h);
    }

    public int getWidth() {
        return (int)(idleAnim.getImage().getWidth(null) * SCALE) - 40;
    }

    public int getHeight() {
        return (int)(idleAnim.getImage().getHeight(null) * SCALE);
    }

    public boolean isDead() {
        return hp <= 0;
    }

    private boolean isPlayerOnSameLevel() {

        Player target = getClosestPlayer();

        int verticalDistance = Math.abs(target.getY() - y);

        int tolerance = 120;

        return verticalDistance < tolerance;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    private boolean isOnScreen() {
        int screenWidth = panel.getWidth();

        int offsetX = screenWidth / 2 -
                Math.round(tileMap.getPlayer1().getX()) - TILE_SIZE;

        int mapWidthPixels = tileMap.getWidthPixels();

        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

        int screenX = x + offsetX;

        return (screenX >= -100 && screenX <= screenWidth + 100);
    }

    public int getX() { return x; }
    public int getY() { return y; }
}