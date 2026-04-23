import java.awt.Point;
import javax.swing.JPanel;

public class Player2 extends Player {

    public Player2(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);
    }

    @Override
    public synchronized void move(int direction) {

        int newX = getX();
        Point tilePos = null;

        if (direction == 1) { // left
            playerImage = playerLeftImage;
            newX -= 15;
            if (newX < 0) {
                setX(0);
                return;
            }
            tilePos = collidesWithTile(newX, getY());
        }

        else if (direction == 2) { // right
            playerImage = playerRightImage;
            newX += 15;
            int width = getImage().getWidth(null);
            tilePos = collidesWithTile(newX + width, getY());
        }

        else if (direction == 3) { // jump
            jump();
            return;
        }

        if (tilePos != null) {
            if (direction == 1)
                setX(((int) tilePos.getX() + 1) * 64);
            else if (direction == 2)
                setX(((int) tilePos.getX()) * 64 - getImage().getWidth(null));
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