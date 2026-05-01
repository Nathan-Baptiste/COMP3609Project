import java.awt.*;
import javax.swing.JPanel;
import java.util.Random;

public class Minitroll {

    private static final int TILE_SIZE = 64;
    private static final double SCALE = 2;

    private static final int DETECTION_RANGE = 500;   // pixels to start chasing
    private static final int CLOSE_RANGE     = 70;    // pixels to start countdown
    private static final int EXPLOSION_RANGE = 120;   // explosion radius (unblockable)

    private static final int COUNTDOWN_MIN = 6;
    private static final int COUNTDOWN_MAX = 8;

    private static final int CHASE_SPEED = 14;

    private JPanel panel;
    private TileMap tileMap;

    private int x, y;
    private int hp = 20;
    private int scoreValue = 5000;

    private boolean facingRight = true;

    private SoundManager soundManager;

    // State flags
    private boolean idle      = true;
    protected boolean chasing   = false;
    private boolean closeState = false;   // "MinitrollClose" – countdown ticking
    protected boolean exploding = false;
    private boolean dead      = false;

    private int countdownTimer = 0;
    private int countdownTarget = 0;

    private int hitCooldown = 0;
    private int hitTimer    = 0;
    private boolean gettingHit = false;
    private boolean killedByArrow = false;

    // Gravity
    private boolean inAir      = false;
    private boolean goingDown  = false;
    private int     timeElapsed = 0;
    private int     startY      = 0;
    private int     initialVelocity = 0;

    // Animations / images
    private StripAnimation idleAnim;
    private StripAnimation chaseAnim;
    private StripAnimation closeAnim;
    private StripAnimation explodeAnim;
    private Image          hitImage;

    private boolean explodeAnimDone = false;
    private boolean explodeDamageDealt = false;

    private Random rand = new Random();

    public Minitroll(JPanel panel, TileMap tileMap, int x, int y) {
        this.panel   = panel;
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;

        soundManager = SoundManager.getInstance();

        Image idleStrip    = ImageManager.loadImage("src/images/Enemies/Minitroll/MinitrollIdle.png");
        Image chaseStrip   = ImageManager.loadImage("src/images/Enemies/Minitroll/MinitrollChase.png");
        Image closeStrip   = ImageManager.loadImage("src/images/Enemies/Minitroll/MinitrollClose.png");
        Image explodeStrip = ImageManager.loadImage("src/images/Enemies/Minitroll/MinitrollExplode.png");

        idleAnim    = new StripAnimation(idleStrip,    4,  0, 0, panel, true);
        chaseAnim   = new StripAnimation(chaseStrip,   6,  0, 0, panel, true);
        closeAnim   = new StripAnimation(closeStrip,   4,  0, 0, panel, true);
        explodeAnim = new StripAnimation(explodeStrip, 9,  0, 0, panel, false);

        hitImage = ImageManager.loadImage("src/images/Enemies/Minitroll/MinitrollHit.png");

        idleAnim.start();
        chaseAnim.start();
        closeAnim.start();
        explodeAnim.start();
    }

    public void update() {

        if (dead) return;

        if (y > tileMap.getOffsetY() + tileMap.tilesToPixels(tileMap.getHeight()) + 100) {
            dead = true;
            return;
        }

        idleAnim.update();
        chaseAnim.update();
        closeAnim.update();
        explodeAnim.update();

        if (hitCooldown > 0) hitCooldown--;
        if (hitTimer > 0 && --hitTimer == 0) gettingHit = false;

        // Handle exploding state
        if (exploding) {
            if (!explodeDamageDealt) {
                dealExplosionDamage();
                explodeDamageDealt = true;
            }
            if (explodeAnim.isFinished()) {
                dead = true;
            }
            updateGravity();
            return;
        }

        Player target = getClosestPlayer();

        if (target == null || target.isDead()) {
            enterIdle();
            updateGravity();
            return;
        }

        int dist = Math.abs(target.getX() - x);
        boolean sameLevel = isPlayerOnSameLevel(target);

        if (closeState) {
            if (dist > DETECTION_RANGE || !sameLevel) {
                enterIdle();
            } else {
                countdownTimer++;
                facingRight = target.getX() > x;

                if (countdownTimer >= countdownTarget) {
                    enterExplode();
                }
            }
        } else if (chasing) {
            if (dist > DETECTION_RANGE || !sameLevel) {
                enterIdle();
            } else if (dist < CLOSE_RANGE) {
                enterClose();
            } else {
                // Keep chasing
                facingRight = target.getX() > x;
                moveToward(target);
            }
        } else {
            // idle
            if (dist < DETECTION_RANGE && sameLevel) {
                enterChase();
            }
        }

        updateGravity();
    }

    private void enterIdle() {
        idle       = true;
        chasing    = false;
        closeState = false;
        exploding  = false;
        countdownTimer = 0;
    }

    private void enterChase() {
        idle       = false;
        chasing    = true;
        closeState = false;
        exploding  = false;
        soundManager.playSound("minitrolChase", false);
        soundManager.playSound("minitrolLaughing", false);
    }

    private void enterClose() {
        idle          = false;
        chasing       = false;
        closeState    = true;
        exploding     = false;
        countdownTimer  = 0;
        countdownTarget = COUNTDOWN_MIN + rand.nextInt(COUNTDOWN_MAX - COUNTDOWN_MIN + 1);
        closeAnim.start();
        soundManager.playSound("minitrolClose", false);
    }

    private void enterExplode() {
        idle              = false;
        chasing           = false;
        closeState        = false;
        exploding         = true;
        explodeDamageDealt = false;
        explodeAnim.start();
        soundManager.playSound("minitrolExplode", false);
        soundManager.playSound("minitrolHit", false);
    }

    private void moveToward(Player target) {
        int dx;

        if (target.getX() > x) {
            dx = CHASE_SPEED;
        } else {
            dx = -CHASE_SPEED;
        }


        int newX = x + dx;
        int width  = getWidth();
        int height = getHeight();


        if (dx > 0) {
            Point tile = collidesWithTileVertical(newX + width, y + 5, height - 10);
            if (tile == null) x = newX;
        } else {
            Point tile = collidesWithTileVertical(newX, y + 5, height - 10);
            if (tile == null) x = newX;
        }
    }

    private void dealExplosionDamage() {
        Rectangle blast = new Rectangle(
                x - EXPLOSION_RANGE / 2,
                y - EXPLOSION_RANGE / 2,
                getWidth()  + EXPLOSION_RANGE,
                getHeight() + EXPLOSION_RANGE
        );

        Player p1 = tileMap.getPlayer1();
        Player p2 = tileMap.getPlayer2();

        if (!p1.isDead() && blast.intersects(p1.getHitBox())) {
            // Force-apply damage regardless of block
            p1.takeDamage(2, x > p1.getX());
        }
        if (!p2.isDead() && blast.intersects(p2.getHitBox())) {
            p2.takeDamage(2, x > p2.getX());
        }
    }

    public void takeDamage(int dmg, boolean hitFromRight, boolean fromArrow) {
        if (hitCooldown > 0 || exploding || dead) return;
        if (fromArrow) killedByArrow = true;

        soundManager.playSound("minitrolHit", false);

        hp -= dmg;
        gettingHit  = true;
        hitTimer    = 10;
        hitCooldown = 6;

        int knockback = 20;
        int dx;

        if (hitFromRight) {
            dx = -knockback;
        } else {
            dx = knockback;
        }

        int newX = x + dx;
        int width  = getWidth();
        int height = getHeight();

        if (dx > 0) {
            Point tile = collidesWithTileVertical(newX + width, y + 5, height - 10);
            if (tile != null) newX = tile.x * TILE_SIZE - width;
        } else {
            Point tile = collidesWithTileVertical(newX, y + 5, height - 10);
            if (tile != null) newX = (tile.x + 1) * TILE_SIZE;
        }

        x = newX;

        // Being killed before countdown → don't explode
        if (hp <= 0) {
            dead = true;
        }
    }

    private void updateGravity() {
        if (!inAir) {
            if (isInAir()) fall();
            return;
        }

        timeElapsed++;
        int distance = (int)(initialVelocity * timeElapsed - 3.0 * timeElapsed * timeElapsed);
        int newY = startY - distance;

        if (goingDown) {
            Point tile = collidesWithTileDown(x, newY);
            if (tile != null) {
                int offsetY = tileMap.getOffsetY();
                y = tile.y * TILE_SIZE + offsetY - getHeight();
                inAir     = false;
                goingDown = false;
            } else {
                y = newY;
            }
        }
    }

    private void fall() {
        inAir       = true;
        goingDown   = true;
        timeElapsed = 0;
        startY      = y;
        initialVelocity = 0;
    }

    private boolean isInAir() {
        int w = getWidth(), h = getHeight();
        return collidesWithTileAtPoint(x, y + h + 1) == null
                && collidesWithTileAtPoint(x + w - 1, y + h + 1) == null;
    }

    private Point collidesWithTileAtPoint(int px, int py) {
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(px);
        int yTile = tileMap.pixelsToTiles(py - offsetY);
        if (tileMap.getTile(xTile, yTile) != null) {
            return new Point(xTile, yTile);
        } else {
            return null;
        }
    }

    private Point collidesWithTileVertical(int px, int topY, int height) {
        int offsetY  = tileMap.getOffsetY();
        int xTile    = tileMap.pixelsToTiles(px);
        int yTileTop = tileMap.pixelsToTiles(topY - offsetY);
        int yTileBot = tileMap.pixelsToTiles(topY - offsetY + height);
        for (int yt = yTileTop; yt <= yTileBot; yt++) {
            if (tileMap.getTile(xTile, yt) != null)
                return new Point(xTile, yt);
        }
        return null;
    }

    private Point collidesWithTileDown(int px, int newY) {
        int width   = getWidth();
        int height  = getHeight();
        int offsetY = tileMap.getOffsetY();
        int yFrom = tileMap.pixelsToTiles((y + height) - offsetY);
        int yTo   = tileMap.pixelsToTiles(newY - offsetY + height);
        int xLeft   = tileMap.pixelsToTiles(px);
        int xRight  = tileMap.pixelsToTiles(px + width - 1);
        for (int yt = yFrom; yt <= yTo; yt++) {
            if (tileMap.getTile(xLeft,  yt) != null) return new Point(xLeft,  yt);
            if (xRight != xLeft && tileMap.getTile(xRight, yt) != null) return new Point(xRight, yt);
        }
        return null;
    }

    private Player getClosestPlayer() {
        Player p1 = tileMap.getPlayer1();
        Player p2 = tileMap.getPlayer2();
        if (p1.isDead() && p2.isDead()) return null;
        if (!p1.isDead() && p2.isDead()) return p1;
        if (p1.isDead()) return p2;
        if (Math.abs(p1.getX() - x) < Math.abs(p2.getX() - x)) {
            return p1;
        } else {
            return p2;
        }
    }

    private boolean isPlayerOnSameLevel(Player target) {
        return Math.abs(target.getY() - y) < 120;
    }

    public Image getImage() {
        if (gettingHit)  return hitImage;
        if (exploding)   return explodeAnim.getImage();
        if (closeState)  return closeAnim.getImage();
        if (chasing)     return chaseAnim.getImage();
        return idleAnim.getImage();
    }

    public Rectangle getHitBox() { return new Rectangle(x, y, getWidth(), getHeight()); }

    public int getWidth()  {
        Image img = idleAnim.getImage();
        if (img == null) {
            return 40;
        } else {
            return (int)(img.getWidth(null) * SCALE) - 10;
        }

    }

    public int getHeight() {
        Image img = idleAnim.getImage();
        if (img == null) {
            return 60;
        } else {
            return (int)(img.getHeight(null) * SCALE);
        }
    }

    public int  getX()       { return x; }
    public int  getY()       { return y; }
    public boolean isDead()  { return dead; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isExploding()   { return exploding; }
    public int getScoreValue() { return scoreValue; }
    public boolean wasKilledByArrow() { return killedByArrow; }
}