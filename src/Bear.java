import java.awt.*;
import javax.swing.JPanel;
import java.util.Random;

public class Bear {

    private static final int SPEED = 4;
    private static final int CHASE_SPEED = 10;
    private static final int TILE_SIZE = 64;
    private static final double SCALE = 2;

    private static final int DETECTION_RANGE = 350;
    private static final int ATTACK_RANGE = 80;

    private JPanel panel;
    private TileMap tileMap;

    private int x, y;
    private int hp = 200;

    private boolean movingRight = true;
    protected boolean chasing = false;
    private boolean attacking = false;
    private boolean gettingHit = false;
    private boolean idling = false;
    private int idleTimer = 0;

    private boolean chaseCooldown = false;
    private int chaseCooldownTimer = 0;

    private int attackTimer = 0;
    private int hitTimer = 0;
    private int hitCooldown = 0;

    private StripAnimation idleAnim;
    private StripAnimation moveAnim;
    private StripAnimation chaseAnim;
    private StripAnimation attackAnim;

    private Image hitImage;

    private Random rand = new Random();
    private int moveTimer = 0;

    // Gravity
    private boolean inAir = false;
    private boolean jumping = false;
    private int timeElapsed = 0;
    private int startY = 0;
    private int initialVelocity = 0;
    private boolean goingDown = false;

    public Bear(JPanel panel, TileMap tileMap, int x, int y) {
        this.panel = panel;
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;

        idleAnim = new StripAnimation(
                ImageManager.loadImage("src/images/Enemies/Bear/BearIdle.png"),
                1, 0, 0, panel, true);

        moveAnim = new StripAnimation(
                ImageManager.loadImage("src/images/Enemies/Bear/BearMove.png"),
                8, 0, 0, panel, true);

        chaseAnim = new StripAnimation(
                ImageManager.loadImage("src/images/Enemies/Bear/BearChase.png"),
                4, 0, 0, panel, true);

        attackAnim = new StripAnimation(
                ImageManager.loadImage("src/images/Enemies/Bear/BearAttack.png"),
                10, 0, 0, panel, false);

        hitImage = ImageManager.loadImage("src/images/Enemies/Bear/BearHit.png");

        idleAnim.start();
        moveAnim.start();
        chaseAnim.start();
        attackAnim.start();
    }

    public void update() {

        idleAnim.update();
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

        if (hitCooldown > 0) hitCooldown--;
        if (hitTimer > 0 && --hitTimer == 0) gettingHit = false;

        Player target = getClosestPlayer();

        if (target != null) {
            int dist = Math.abs(target.getX() - x);

            chasing = dist < DETECTION_RANGE
                    && !chaseCooldown
                    && isPlayerOnSameLevel(target);

            if (dist < ATTACK_RANGE && !attacking) {
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
            return;
        }

        move(movingRight ? SPEED : -SPEED);
    }

    private void chase(Player target) {
        if (target == null) return;

        movingRight = target.getX() > x;

        boolean moved = move(movingRight ? CHASE_SPEED : -CHASE_SPEED);

        // If hit wall while chasing → trigger cooldown
        if (!moved) {
            chaseCooldown = true;
            chaseCooldownTimer = 0;

            chasing = false;

            // turn around and patrol opposite direction
            movingRight = !movingRight;

            // reset wander so it starts moving immediately
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
                return false; // HIT WALL
            } else {
                x = newX;
                return true;
            }
        } else {
            Point tile = collidesWithTileVertical(newX, y + 5, height - 10);
            if (tile != null) {
                movingRight = true;
                return false; // HIT WALL
            } else {
                x = newX;
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
        attackTimer++;

        // ACTIVE FRAMES 6–9
        if (attackTimer >= 6 && attackTimer <= 9) {
            Rectangle attackBox = getAttackBox();

            Player p1 = tileMap.getPlayer1();
            Player p2 = tileMap.getPlayer2();

            if (!p1.isDead() && attackBox.intersects(p1.getHitBox())) {
                p1.takeDamage(2, x < p1.getX());
            }

            if (!p2.isDead() && attackBox.intersects(p2.getHitBox())) {
                p2.takeDamage(2, x < p2.getX());
            }
        }

        if (attackTimer > 10) {
            attacking = false;
        }
    }

    private Rectangle getAttackBox() {
        int width = 80;
        int height = 80;

        if (movingRight) {
            return new Rectangle(x + getWidth(), y, width, height);
        } else {
            return new Rectangle(x - width, y, width, height);
        }
    }

    public void takeDamage(int dmg, boolean hitFromRight) {
        if (hitCooldown > 0) return;

        hp -= dmg;

        gettingHit = true;
        hitTimer = 10;
        hitCooldown = 6;

        int knockback = 30;
        if (hitFromRight) x -= knockback;
        else x += knockback;
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
        int width = getWidth();
        int height = getHeight();
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(px);
        int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
        int yTileTo = tileMap.pixelsToTiles(newY - offsetY + height);

        for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null)
                return new Point(xTile, yTile);
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
        if (!idling) return moveAnim.getImage();

        return idleAnim.getImage();
    }

    public Rectangle getHitBox() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public int getWidth() {
        return (int)(idleAnim.getImage().getWidth(null) * SCALE) - 30;
    }

    public int getHeight() {
        return (int)(idleAnim.getImage().getHeight(null) * SCALE);
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isMovingRight() { return movingRight; }
}