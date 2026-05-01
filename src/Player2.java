import java.awt.*;
import javax.swing.JPanel;

public class Player2 extends Player {

    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_MAX = 5; // adjust feel (25 = ~0.5 sec if 50ms loop)
    private SoundManager soundManager;


    public Player2(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);

        soundManager = SoundManager.getInstance();

        Image idleStrip = ImageManager.loadImage("src/images/Player2/Player2Idle.png");
        idleAnim = new StripAnimation(idleStrip, 4, 0, 0, panel, true);

        Image runStrip = ImageManager.loadImage("src/images/Player2/Player2Run.png");
        runAnim = new StripAnimation(runStrip, 6, 0, 0, panel, true);

        Image jumpStrip = ImageManager.loadImage("src/images/Player2/Player2Jump.png");
        jumpAnim = new StripAnimation(jumpStrip, 6, 0, 0, panel, false);

        Image chargeStrip = ImageManager.loadImage("src/images/Player2/Player2ChargeBow.png");
        chargeAnim = new StripAnimation(chargeStrip, 5, 0, 0, panel, false);

        Image shootStrip = ImageManager.loadImage("src/images/Player2/Player2Shoot.png");
        shootAnim = new StripAnimation(shootStrip, 6, 0, 0, panel, false);

        Image jumpChargeStrip = ImageManager.loadImage("src/images/Player2/Player2JumpCharge.png");
        jumpChargeAnim = new StripAnimation(jumpChargeStrip, 5, 0, 0, panel, false);

        Image jumpShootStrip = ImageManager.loadImage("src/images/Player2/Player2JumpShoot.png");
        jumpShootAnim = new StripAnimation(jumpShootStrip, 6, 0, 0, panel, false);

        Image blockStrip = ImageManager.loadImage("src/images/Player2/Player2Block.png");
        blockAnim = new StripAnimation(blockStrip, 4, 0, 0, panel, true);

        hitImage = ImageManager.loadImage("src/images/Player2/Player2Hit.png");
        deathImage = ImageManager.loadImage("src/images/Player1/PlayerDeath.png");

        idleAnim.start();
        runAnim.start();
        jumpAnim.start();
        chargeAnim.start();
        shootAnim.start();
        jumpChargeAnim.start();
        jumpShootAnim.start();
        blockAnim.start();
    }

    public synchronized void move(int direction) {

        if (gettingHit) return;
        if (isDead()) return;
        if (blocking) return;

        int newX = getX();
        Point tilePos = null;

        if (direction == 3) {
            jump();
            return;
        }

        if (charging && !(jumping || inAir)) {
            if (direction == 1) {
                facingRight = false;
            } else if (direction == 2) {
                facingRight = true;
            }
            return;
        }

        if (shooting && (direction == 1 || direction == 2)) {
            shooting = false;
        }

        if (direction == 1) { // left
            moving = true;
            facingRight = false;
            newX -= currentSpeed + 3;
            if (newX < 0) {
                setX(0);
                return;
            }
            tilePos = collidesWithTile(newX, getY());
        }

        else if (direction == 2) { // right
            moving = true;
            facingRight = true;
            newX += currentSpeed + 3;
            tilePos = collidesWithTile(newX + getDisplayWidth(), getY());
        }

        else if (direction == 3) { // jump
            jump();
            return;
        }

        if (tilePos != null) {
            if (direction == 1)
                setX(((int) tilePos.getX() + 1) * 64);
            else if (direction == 2)
                setX(((int) tilePos.getX()) * 64 - getDisplayWidth());
        }
        else {
            // move player ONLY (no camera scroll)
            if (direction == 1 || direction == 2)
                setX(newX);

            if (isInAir())
                fall();
        }
    }

    public void update() {
        super.update();

        if (shootCooldown > 0) {
            shootCooldown--;
        }
    }


    public void startCharge() {
        if (shootCooldown > 0) return;

        soundManager.playSound("chargeBow", false);

        if (!charging) {
            charging = true;
            chargeTime = 0;
            chargeAnim.start();
        }
    }

    public void releaseShoot() {
        if (!charging) return;

        if (shootCooldown > 0) return;

        soundManager.playSound("shootBow", false);

        charging = false;
        shooting = true;
        shootTimer = 8;

        shootCooldown = SHOOT_COOLDOWN_MAX;

        if (jumpShootAnim != null && (isInAir() || jumping)) {
            jumpShootAnim.start();
        }

        int arrowX;
        int arrowY;

        if (facingRight) {
            arrowX = getX();
            arrowY = getY() + 16;
        } else {
            arrowX = getX();
            arrowY = getY() + 16;
        }

        tileMap.spawnArrow(arrowX, arrowY, facingRight);
    }
}