import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Collectible {

    public static final int COIN = 0;
    public static final int CAKE = 1;
    public static final int BROCCOLI = 2;
    public static final int CHICKEN = 3;

    private static final int SCALE = 2;

    private int x;
    private int y;
    private int type;

    private boolean collected = false;

    private Animation animation;
    private Image currentImage;

    public Collectible(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;

        BufferedImage spriteSheet = null;

        if (type == COIN) {
            spriteSheet = ImageManager.loadBufferedImage("src/images/itemsandObjects/Coin.png");
        }
        else if (type == CAKE) {
            spriteSheet = ImageManager.loadBufferedImage("src/images/itemsandObjects/Cake.png");
        }
        else if (type == BROCCOLI) {
            spriteSheet = ImageManager.loadBufferedImage("src/images/itemsandObjects/Broccoli.png");
        }
        else if (type == CHICKEN) {
            spriteSheet = ImageManager.loadBufferedImage("src/images/itemsandObjects/Chicken.png");
        }

        if (spriteSheet == null) {
            System.out.println("Collectible sprite sheet failed to load.");
            return;
        }

        animation = new Animation(true);
        loadFramesFromSpriteSheet(spriteSheet);

        if (animation.getNumFrames() > 0) {
            animation.start();
            currentImage = animation.getImage();
        }

        System.out.println("Collectible loaded. Frames: " + animation.getNumFrames());
    }

    private void loadFramesFromSpriteSheet(BufferedImage sheet) {
        ArrayList<int[]> frameRanges = new ArrayList<int[]>();

        boolean insideFrame = false;
        int startX = 0;

        for (int x = 0; x < sheet.getWidth(); x++) {

            boolean columnHasPixel = columnHasVisiblePixel(sheet, x);

            if (columnHasPixel && !insideFrame) {
                startX = x;
                insideFrame = true;
            }

            if (!columnHasPixel && insideFrame) {
                int endX = x - 1;
                frameRanges.add(new int[] { startX, endX });
                insideFrame = false;
            }
        }

        if (insideFrame) {
            frameRanges.add(new int[] { startX, sheet.getWidth() - 1 });
        }

        for (int i = 0; i < frameRanges.size(); i++) {
            int start = frameRanges.get(i)[0];
            int end = frameRanges.get(i)[1];

            int frameWidth = end - start + 1;
            int frameHeight = sheet.getHeight();

            BufferedImage frame = sheet.getSubimage(start, 0, frameWidth, frameHeight);
            animation.addFrame(frame, 120);
        }
    }

    private boolean columnHasVisiblePixel(BufferedImage sheet, int x) {
        for (int y = 0; y < sheet.getHeight(); y++) {
            int pixel = sheet.getRGB(x, y);
            int alpha = (pixel >> 24) & 0xff;

            if (alpha > 10) {
                return true;
            }
        }

        return false;
    }

    public void update() {
        if (collected) return;

        if (animation != null) {
            animation.update();
            currentImage = animation.getImage();
        }
    }

    public void draw(Graphics2D g2, int offsetX) {
        if (collected || currentImage == null) return;

        int w = currentImage.getWidth(null) * SCALE;
        int h = currentImage.getHeight(null) * SCALE;

        g2.drawImage(currentImage, x + offsetX, y, w, h, null);
    }

    public Rectangle getHitBox() {
        if (currentImage == null) {
            return new Rectangle(x, y, 32, 32);
        }

        int w = currentImage.getWidth(null) * SCALE;
        int h = currentImage.getHeight(null) * SCALE;

        return new Rectangle(x, y, w, h);
    }

    public boolean collidesWith(Player player) {
        if (collected || player == null || player.isDead()) {
            return false;
        }

        return getHitBox().intersects(player.getHitBox());
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean isCoin() {
        return type == COIN;
    }

    public boolean isFood() {
        return type == CAKE || type == BROCCOLI || type == CHICKEN;
    }

    public int getPoints() {
        if (isCoin()) {
            return 5;
        }

        return 0;
    }

    public int getHealAmount() {
        if (isFood()) {
            return 1;
        }

        return 0;
    }

    public static int foodTypeFromTilePosition(int tileX, int tileY) {
        int choice = Math.abs(tileX + tileY) % 3;

        if (choice == 0) {
            return CAKE;
        }
        else if (choice == 1) {
            return BROCCOLI;
        }

        return CHICKEN;
    }
}
