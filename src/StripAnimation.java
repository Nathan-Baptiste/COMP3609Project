import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 The StripAnimation class creates an animation from a strip file.
 */
public class StripAnimation {

    Animation animation;

    private int x;        // x position of animation
    private int y;        // y position of animation

    private int width;
    private int height;

    private int dx;       // increment to move along x-axis
    private int dy;       // increment to move along y-axis

    public StripAnimation(Image stripImage, int totalFrames, int xPos, int yPos, JPanel panel, boolean loop) {
        animation = new Animation(loop);

        dx = 0;     // increment to move along x-axis
        dy = 0;     // increment to move along y-axis

        x = xPos;
        y = yPos;

        // Calculate the width and height of a single frame from the strip
        width = (int) Math.round((double) stripImage.getWidth(null) / totalFrames);
        height = stripImage.getHeight(null);

        // Extract each frame using Graphics2D.drawImage() with src/dst rects
        for (int i = 0; i < totalFrames; i++) {

            BufferedImage frameImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) frameImage.getGraphics();

            g.drawImage(stripImage,
                    0,0, width, height,
                    i * width, 0, (i + 1) * width, height,
                    null);

            g.dispose();
            animation.addFrame(frameImage, 100);
        }
    }

    public void start() {
        animation.start();
    }

    public void update() {
        animation.update();
    }

    public boolean isFinished() {
        return !animation.isStillActive();
    }

    public Image getImage() {
        return animation.getImage();
    }

    public int getCurrentFrame() { return animation.getCurrentFrameIndex(); }

    public void draw(Graphics2D g2, int screenX, int screenY, int displayWidth, int displayHeight) {
        g2.drawImage(animation.getImage(), screenX, screenY, displayWidth, displayHeight, null);
    }
}