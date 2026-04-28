import java.awt.*;
import javax.swing.JPanel;

public class Slime {

    private static final int SPEED = 4;
    private static final int TILE_SIZE = 64;

    private JPanel panel;
    private TileMap tileMap;

    private int x, y;

    private boolean movingRight = true;

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
        move();

        moveAnim.update();

        if (gettingHit) {
            hitAnim.update();

            if (hitAnim.isFinished()) {
                gettingHit = false;
            }
        }
    }

    private void move() {
        int newX = x;

        if (movingRight) {
            newX += SPEED;

            int width = getWidth();
            Point tile = collidesWithTile(newX + width, y);

            if (tile != null) {
                movingRight = false;
            } else {
                x = newX;
            }
        } else {
            newX -= SPEED;

            Point tile = collidesWithTile(newX, y);

            if (tile != null) {
                movingRight = true;
            } else {
                x = newX;
            }
        }
    }

    private Point collidesWithTile(int px, int py) {
        int offsetY = tileMap.getOffsetY();

        int xTile = tileMap.pixelsToTiles(px);
        int yTile = tileMap.pixelsToTiles(py - offsetY);

        if (tileMap.getTile(xTile, yTile) != null) {
            return new Point(xTile, yTile);
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
        if (gettingHit) {
            return hitAnim.getImage();
        }
        return moveAnim.getImage();
    }

    public int getWidth() {
        return moveAnim.getImage().getWidth(null);
    }

    public int getHeight() {
        return moveAnim.getImage().getHeight(null);
    }

    public Rectangle getHitBox() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public int getX() { return x; }
    public int getY() { return y; }
}