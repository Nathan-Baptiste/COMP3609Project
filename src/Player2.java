import java.awt.*;
import javax.swing.JPanel;

public class Player2 extends Player {

    public Player2(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);

        Image idleStrip = ImageManager.loadImage("src/images/Player2/Player2Idle.png");
        idleAnim = new StripAnimation(idleStrip, 4, 0, 0, panel);

        Image runStrip = ImageManager.loadImage("src/images/Player2/Player2Run.png");
        runAnim = new StripAnimation(runStrip, 6, 0, 0, panel);

        idleAnim.start();
        runAnim.start();
    }

    @Override
    public synchronized void move(int direction) {

        int newX = getX();
        Point tilePos = null;

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
}