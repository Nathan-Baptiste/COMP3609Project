import java.awt.*;
import javax.swing.JPanel;
import java.util.Random;

public class Bear {

    private static final int SPEED = 4;
    private static final int CHASE_SPEED = 10;
    private static final int TILE_SIZE = 64;
    private static final double SCALE = 2;

    private static final int DETECTION_RANGE = 350;
    private static final int ATTACK_RANGE = 65;

    private JPanel panel;
    private TileMap tileMap;

    private int x, y;
    private int hp = 150;

    private boolean movingRight = true;
    protected boolean chasing = false;
    protected boolean attacking = false;
    protected boolean gettingHit = false;
    protected boolean idling = false;
    private int idleTimer = 0;

    private boolean chaseCooldown = false;
    private int chaseCooldownTimer = 0;

    private int attackTimer = 0;
    private int hitTimer = 0;
    private int hitCooldown = 0;

    private Image idleImage;
    private StripAnimation moveAnim;
    private StripAnimation chaseAnim;
    private StripAnimation attackAnim;

    private Image hitImage;

    private Random rand = new Random();
    private int moveTimer = 0;


    // Gravity
    private boolean inAir = false;
    private boolean moving = false;
    private boolean jumping = false;
    private int timeElapsed = 0;
    private int startY = 0;
    private int initialVelocity = 0;
    private boolean goingDown = false;
    private boolean facingRight = true; // add this field

    public Bear(JPanel panel, TileMap tileMap, int x, int y) {
        this.panel = panel;
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;

        idleImage = ImageManager.loadImage("src/images/Enemies/Bear/BearIdle.png");

        Image moveStrip = ImageManager.loadImage("src/images/Enemies/Bear/BearMove.png");
        moveAnim = new StripAnimation(moveStrip, 8, 0, 0, panel, true);

        Image chaseStrip = ImageManager.loadImage("src/images/Enemies/Bear/BearChase.png");
        chaseAnim = new StripAnimation(chaseStrip, 4, 0, 0, panel, true);

        Image attackStrip = ImageManager.loadImage("src/images/Enemies/Bear/BearAttack.png");
        attackAnim = new StripAnimation(attackStrip, 10, 0, 0, panel, false);

        hitImage = ImageManager.loadImage("src/images/Enemies/Bear/BearHit.png");

        moveAnim.start();
        chaseAnim.start();
        attackAnim.start();
    }

    public void update() {

        moving = false;

        if (y > tileMap.getOffsetY() + tileMap.tilesToPixels(tileMap.getHeight()) + 100) {
            hp = 0;
            return;
        }

        moveAnim.update();
        chaseAnim.update();
        attackAnim.update();

        if (chaseCooldown) {
            chaseCooldownTimer++;

            if (chaseCooldownTimer > 20) {
                chaseCooldown = false;
                chaseCooldownTimer = 0;
            }
        }

        if (gettingHit) {
            moving = false;
        }

        if (hitCooldown > 0) hitCooldown--;
        if (hitTimer > 0 && --hitTimer == 0) gettingHit = false;

        Player target = getClosestPlayer();

        if (target != null) {
            int dist = Math.abs(target.getX() - x);

            chasing = dist < DETECTION_RANGE
                    && !chaseCooldown
                    && isPlayerOnSameLevel(target);

            if (dist < ATTACK_RANGE && !attacking  && isPlayerOnSameLevel(target)) {
                startAttack();
            }
        } else {
            chasing = false;
        }

        if (!attacking && !gettingHit) {
            if (chasing) {
                chase(target);
            } else {
                wander();
            }
        }

        if (attacking) handleAttack();

        updateGravity();
    }

    private void wander() {

        if (idling) {
            moving = false;
            idleTimer++;

            // stay idle for a bit
            if (idleTimer > 40) { // adjust for longer/shorter pause
                idling = false;
                idleTimer = 0;

                // NOW choose new direction AFTER idling
                movingRight = rand.nextBoolean();
            }

            return; // don't move while idling
        }

        moveTimer++;

        // walk for a while, then stop
        if (moveTimer > 60) {
            moveTimer = 0;
            idling = true; // enter idle state
            System.out.println("True");
            return;
        }

        move(movingRight ? SPEED : -SPEED);
    }

    private void chase(Player target) {
        if (target == null) return;

        movingRight = target.getX() > x;
        boolean moved = move(movingRight ? CHASE_SPEED : -CHASE_SPEED);
        moving = moved;

        if (!moved) {
            chaseCooldown = true;
            chaseCooldownTimer = 0;
            movingRight = !movingRight;
            idling = false;
            moveTimer = 0;
        }
    }

    private boolean move(int dx) {
        int newX = x + dx;
        int width = getWidth();
        int height = getHeight();

        if (dx > 0) {
            Point tile = collidesWithTileVertical(newX + width, y + 5, height - 10);
            if (tile != null) {
                movingRight = false;
                return false;
            } else {
                x = newX;
                facingRight = true;  // ← add
                return true;
            }
        } else {
            Point tile = collidesWithTileVertical(newX, y + 5, height - 10);
            if (tile != null) {
                movingRight = true;
                return false;
            } else {
                x = newX;
                facingRight = false;  // ← add
                return true;
            }
        }
    }

    private void startAttack() {
        attacking = true;
        attackTimer = 0;
        attackAnim.start();
    }

    private void handleAttack() {
        moving = false;
        attackTimer++;

        if (attackTimer >= 9 && attackTimer <= 10) {
            Rectangle attackBox = getAttackBox();
            Player p1 = tileMap.getPlayer1();
            Player p2 = tileMap.getPlayer2();

            // PLAYER 1
            if (!p1.isDead() && attackBox.intersects(p1.getHitBox())) {
                boolean hitFromRight = x > p1.getX();

                if (p1.isBlocking()) {
                    p1.blockPush(hitFromRight);
                } else {
                    p1.takeDamage(2, hitFromRight);
                }
            }

            // PLAYER 2
            if (!p2.isDead() && attackBox.intersects(p2.getHitBox())) {
                boolean hitFromRight = x > p2.getX();

                if (p2.isBlocking()) {
                    p2.blockPush(hitFromRight);
                } else {
                    p2.takeDamage(2, hitFromRight);
                }
            }
        }

        if (attackAnim.isFinished()) {
            attacking = false;
        }
    }

    protected Rectangle getAttackBox() {
        int width = 90;
        int height = 80;

        if (movingRight) {
            return new Rectangle(x + getWidth() - 60, y, width, height);
        } else {
            return new Rectangle(x - width + 60, y, width, height);
        }
    }

    public void takeDamage(int dmg, boolean hitFromRight) {
        if (hitCooldown > 0) return;

        hp -= dmg;
        gettingHit = true;
        hitTimer = 10;
        hitCooldown = 6;

        attacking = false;
        attackTimer = 0;

        int knockback = 30;
        int dx = hitFromRight ? -knockback : knockback;
        int newX = x + dx;
        int height = getHeight();
        int width = getWidth();

        if (dx > 0) {
            Point tile = collidesWithTileVertical(newX + width, y + 5, height - 10);
            if (tile != null) newX = tile.x * TILE_SIZE - width;
        } else {
            Point tile = collidesWithTileVertical(newX, y + 5, height - 10);
            if (tile != null) newX = (tile.x + 1) * TILE_SIZE;
        }

        x = newX;

        // reset gravity so it re-checks ground from new position
        inAir = false;
        goingDown = false;
        timeElapsed = 0;
    }

    // ---------- GRAVITY ----------
    private void updateGravity() {
        if (!jumping && !inAir) {
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
                int topTileY = tile.y * TILE_SIZE + offsetY;
                y = topTileY - getHeight();

                inAir = false;
                goingDown = false;
            } else {
                y = newY;
            }
        }
    }

    private void fall() {
        inAir = true;
        timeElapsed = 0;
        startY = y;
        initialVelocity = 0;
        goingDown = true;
    }

    private boolean isPlayerOnSameLevel(Player target) {
        if (target == null) return false;

        int verticalDistance = Math.abs(target.getY() - y);

        int tolerance = 120; // tweak if needed

        return verticalDistance < tolerance;
    }

    private boolean isInAir() {
        int width = getWidth();
        int height = getHeight();

        Point left = collidesWithTileAtPoint(x, y + height + 1);
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
        int xTile = tileMap.pixelsToTiles(px);
        int yTileTop = tileMap.pixelsToTiles(topY - offsetY);
        int yTileBot = tileMap.pixelsToTiles(topY - offsetY + height);

        for (int yTile = yTileTop; yTile <= yTileBot; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null)
                return new Point(xTile, yTile);
        }
        return null;
    }

    private Point collidesWithTileDown(int px, int newY) {
        int width  = getWidth();
        int height = getHeight();
        int offsetY = tileMap.getOffsetY();

        int yTileFrom = tileMap.pixelsToTiles(y    - offsetY);
        int yTileTo   = tileMap.pixelsToTiles(newY - offsetY + height);

        int xTileLeft  = tileMap.pixelsToTiles(px);
        int xTileRight = tileMap.pixelsToTiles(px + width - 1);

        for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
            if (tileMap.getTile(xTileLeft, yTile) != null)
                return new Point(xTileLeft, yTile);
            if (xTileRight != xTileLeft && tileMap.getTile(xTileRight, yTile) != null)
                return new Point(xTileRight, yTile);
        }
        return null;
    }

    private Player getClosestPlayer() {
        Player p1 = tileMap.getPlayer1();
        Player p2 = tileMap.getPlayer2();

        if (p1.isDead() && p2.isDead()) return null;
        if (!p1.isDead() && p2.isDead()) return p1;
        if (p1.isDead()) return p2;

        return (Math.abs(p1.getX() - x) < Math.abs(p2.getX() - x)) ? p1 : p2;
    }

    public Image getImage() {
        if (gettingHit) return hitImage;
        if (attacking) return attackAnim.getImage();
        if (chasing) return chaseAnim.getImage();
        if (idling) return idleImage;

        return moveAnim.getImage();
    }

    public Rectangle getHitBox() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public int getWidth() {
        return (int)(idleImage.getWidth(null) * SCALE) - 10;
    }

    public int getHeight() {
        return (int)(idleImage.getHeight(null) * SCALE);
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isMovingRight() { return movingRight; }

    public boolean isMoving() {
        return moving;
    }

    public boolean isFacingRight() { return facingRight; }
}