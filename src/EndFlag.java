import java.awt.*;

public class EndFlag {

    private int x, y;
    private int offsetX, offsetY;
    private int scale;

    private Image image;
    private Rectangle hitBox;

    private int width;
    private int height;

    public EndFlag(int x, int y, Image img) {
        this(x, y, img, 3, 0, 0); // default: x2 scale
    }

    public EndFlag(int x, int y, Image img, int scale, int offsetX, int offsetY) {
        this.x = x;
        this.y = y;
        this.image = img;

        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        this.width = (int)(img.getWidth(null) * scale);
        this.height = (int)(img.getHeight(null) * scale);

        hitBox = new Rectangle(x, y, width, height);
    }

    public void update() {
        hitBox.setBounds(x + offsetX, y + offsetY, width, height);
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        g2.drawImage(
                image,
                x + offsetX + cameraX,
                y + offsetY + cameraY,
                width,
                height,
                null
        );
    }

    public Rectangle getHitBox() {
        return hitBox;
    }
}