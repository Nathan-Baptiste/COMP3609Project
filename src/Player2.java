import java.awt.*;
import javax.swing.JPanel;

public class Player2 extends Player {

    public Player2(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);

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

        idleAnim.start();
        runAnim.start();
        jumpAnim.start();
        chargeAnim.start();
        shootAnim.start();
        jumpChargeAnim.start();
        jumpShootAnim.start();
    }

    @Override
    public synchronized void move(int direction) {

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
            newX -= 15;
            if (newX < 0) {
                setX(0);
                return;
            }
            tilePos = collidesWithTile(newX, getY());
        }

        else if (direction == 2) { // right
            moving = true;
            facingRight = true;
            newX += 15;
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



    public void startCharge() {
        if (!charging) {
            charging = true;
            chargeTime = 0;
            chargeAnim.start();
        }
    }

    public void releaseShoot() {
        if (!charging) return;

        charging = false;

        shooting = true;
        shootTimer = 8;

        if (jumpShootAnim != null && (isInAir() || jumping)) {
            jumpShootAnim.start();
        }

        int arrowX;
        int arrowY;

        if (facingRight) {
            arrowX = getX() - 100;
            arrowY = getY() - 30;
        } else {
            arrowX = getX() - 140;
            arrowY = getY() - 30;
        }

        tileMap.spawnArrow(arrowX, arrowY, facingRight);
    }
}