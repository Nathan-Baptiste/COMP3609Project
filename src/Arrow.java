import java.awt.*;

public class Arrow {

    private static final int SCALE = 2;

    private int x, y;
    private int speed = 18;
    private boolean facingRight;
    private boolean active = true;

    private int damage = 10;

    private Image image;

    public Arrow(int x, int y, boolean facingRight, Image image) {
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.image = image;
    }

    public void update() {
        if (facingRight)
            x += speed;
        else
            x -= speed;
    }

    public void draw(Graphics2D g2, int offsetX) {
        if (!active) return;

        int w = image.getWidth(null) * SCALE;
        int h = image.getHeight(null) * SCALE;
        int screenX = x + offsetX;

        if (facingRight) {
            g2.drawImage(image, screenX, y, w, h, null);
        } else {
            g2.drawImage(image, screenX + w, y, -w, h, null);
        }
    }

    public Rectangle getHitBox() {
        int w = image.getWidth(null) * SCALE;
        int h = image.getHeight(null) * SCALE;
        return new Rectangle(x, y, w, h);
    }

    public boolean collides(Rectangle r) {
        return getHitBox().intersects(r);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getDamage() { return damage; }
}