import java.awt.*;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;
import java.util.ArrayList;

public class TileMap {

    private static final int TILE_SIZE = 64;
    private static final int TILE_SIZE_BITS = 6;
    protected static final double SCALE = 2;
    private static final int MAX_ARROWS = 5;
    private static final int RESPAWN_TIME = 100;

    private SoundManager soundManager;
    private JPanel panel;
    private Dimension dimension;

    private Image[][] tiles;
    private int screenWidth, screenHeight;
    private int mapWidth, mapHeight;
    private int offsetY;

    private int p2OffScreenTime = 0;
    private int p1Score = 0;
    private int p2Score = 0;
    private int p1Coins = 0;
    private int p2Coins = 0;

    private boolean levelComplete = false;

    private LinkedList sprites;
    private Player1 player1;
    private Player2 player2;

    BackgroundManager bgManager;

    private ArrayList<Arrow> arrows = new ArrayList<>();
    private ArrayList<Slime> slimes = new ArrayList<>();
    private ArrayList<Skeleton> skeletons = new ArrayList<>();
    private ArrayList<EnemyArrow> enemyArrows = new ArrayList<>();
    private ArrayList<Bear> bears = new ArrayList<>();
    private ArrayList<Minitroll> minitrols = new ArrayList<>();
    private ArrayList<EndFlag> endFlags = new ArrayList<>();
    private ArrayList<Collectible> collectibles = new ArrayList<>();

    public TileMap(JPanel panel, int width, int height, int level) {
        this.panel = panel;
        dimension = panel.getSize();
        soundManager = SoundManager.getInstance();

        screenWidth = dimension.width;
        screenHeight = dimension.height;
        mapWidth = width;
        mapHeight = height;

        offsetY = screenHeight - tilesToPixels(mapHeight);
        bgManager = new BackgroundManager(panel, 12, level);
        tiles = new Image[mapWidth][mapHeight];

        player1 = new Player1(panel, this, bgManager);
        player2 = new Player2(panel, this, bgManager);
        sprites = new LinkedList();

        Image playerImage = player1.getImage();
        int x = (dimension.width / 2) + TILE_SIZE;
        int y = (int) (dimension.height - (TILE_SIZE + playerImage.getHeight(null) * SCALE));

        player1.setX(x);
        player1.setY(y);
        player1.fall();

        player2.setX(x + 20);
        player2.setY(y);
        player2.fall();
    }

    public int getWidthPixels() {
        return tilesToPixels(mapWidth);
    }

    public int getWidth() {
        return mapWidth;
    }

    public int getHeight() {
        return mapHeight;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public Image getTile(int x, int y) {
        if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
            return null;
        } else {
            return tiles[x][y];
        }
    }

    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }

    public Iterator getSprites() {
        return sprites.iterator();
    }

    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }

    public static int pixelsToTiles(int pixels) {
        return (int) Math.floor((float) pixels / TILE_SIZE);
    }

    public static int tilesToPixels(int numTiles) {
        return numTiles * TILE_SIZE;
    }

    public void draw(Graphics2D g2) {
        int mapWidthPixels = tilesToPixels(mapWidth);

        int offsetX = screenWidth / 2 - Math.round(player1.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

        bgManager.draw(g2);

        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;

        for (int y = 0; y < mapHeight; y++) {
            for (int x = firstTileX; x <= lastTileX; x++) {
                Image image = getTile(x, y);
                if (image != null) {
                    g2.drawImage(image, tilesToPixels(x) + offsetX, tilesToPixels(y) + offsetY, null);
                }
            }
        }

        for (EndFlag f : endFlags) {
            f.draw(g2, offsetX, offsetY);
        }

        for (Collectible c : collectibles) {
            c.draw(g2, offsetX);
        }

        drawSlimes(g2, offsetX);
        drawSkeletons(g2, offsetX);
        drawBears(g2, offsetX);
        drawMinitrols(g2, offsetX);

        for (EnemyArrow a : enemyArrows) {
            a.draw(g2, offsetX);
        }

        drawPlayer2(g2, offsetX);
        drawPlayer1(g2, offsetX);
        drawPlayer2Countdown(g2, offsetX);

        for (Arrow a : new ArrayList<>(arrows)) {
            a.draw(g2, offsetX);
        }
    }

    private void drawSlimes(Graphics2D g2, int offsetX) {
        for (Slime s : slimes) {
            Image img = s.getImage();

            int w = (int) (img.getWidth(null) * SCALE);
            int h = (int) (img.getHeight(null) * SCALE);

            int tileOffset = 385;
            int xOffset = -20;
            int yOffset = -326;

            int drawX = s.getX() + offsetX + xOffset;
            int drawY = s.getY() + offsetY + yOffset;

            boolean hit = s.isGettingHit();

            if (hit) {
                if (s.isMovingRight()) {
                    g2.drawImage(img, drawX + 28 + w, drawY + 6 + tileOffset, -w, h, null);
                } else {
                    g2.drawImage(img, drawX + 28, drawY + 6 + tileOffset, w, h, null);
                }
            } else {
                if (s.isMovingRight()) {
                    g2.drawImage(img, drawX + w, drawY + tileOffset, -w, h, null);
                } else {
                    g2.drawImage(img, drawX + 18, drawY + tileOffset, w, h, null);
                }
            }
        }
    }

    private void drawSkeletons(Graphics2D g2, int offsetX) {
        for (Skeleton s : skeletons) {
            Image img = s.getImage();

            int w = (int) (img.getWidth(null) * SCALE);
            int h = (int) (img.getHeight(null) * SCALE);

            int tileOffset = 385;
            int xOffset = -20;
            int yOffset = -335;

            int drawX;

            if (s.charging && s.isFacingRight()) {
                drawX = s.getX() + offsetX + xOffset - 32;
            } else if (s.gettingHit && s.isFacingRight()) {
                drawX = s.getX() + offsetX + xOffset - 0;
            } else if (s.gettingHit && !s.isFacingRight()) {
                drawX = s.getX() + offsetX + xOffset + 10;
            } else if (s.charging && !s.isFacingRight()) {
                drawX = s.getX() + offsetX + xOffset - 18;
            } else if (s.shooting && s.isFacingRight()) {
                drawX = s.getX() + offsetX + xOffset + 25;
            } else if (s.shooting && !s.isFacingRight()) {
                drawX = s.getX() + offsetX + xOffset - 12;
            } else {
                drawX = s.getX() + offsetX + xOffset;
            }

            int drawY = s.getY() + offsetY + yOffset;

            if (s.getImage() != null) {
                if (s.isFacingRight()) {
                    g2.drawImage(img, drawX + w, drawY + tileOffset, -w, h, null);
                } else {
                    g2.drawImage(img, drawX, drawY + tileOffset, w, h, null);
                }
            }
        }
    }

    private void drawBears(Graphics2D g2, int offsetX) {
        for (Bear b : bears) {
            Image img = b.getImage();

            int tileOffset = 385;
            int w = (int) (img.getWidth(null) * SCALE);
            int h = (int) (img.getHeight(null) * SCALE);

            int drawX;

            if (b.idling) {
                drawX = b.getX() + offsetX;
            } else if (b.attacking && b.isFacingRight()) {
                drawX = b.getX() + offsetX - 20;
            } else if (b.attacking && !b.isFacingRight()) {
                drawX = b.getX() + offsetX - 40;
            } else if (b.chasing) {
                drawX = b.getX() + offsetX - 20;
            } else if (b.isMoving() && b.isFacingRight()) {
                drawX = b.getX() + offsetX;
            } else if (b.isMoving() && !b.isFacingRight()) {
                drawX = b.getX() + offsetX - 30;
            } else {
                drawX = b.getX() + offsetX - 25;
            }

            int drawY;

            if (b.idling && !b.chasing) {
                drawY = b.getY() + offsetY - 335;
            } else if (b.attacking && !b.gettingHit) {
                drawY = b.getY() + offsetY - 335;
            } else if (b.chasing && !b.gettingHit && !b.attacking) {
                drawY = b.getY() + offsetY - 352;
            } else if (b.gettingHit) {
                drawY = b.getY() + offsetY - 335;
            } else if (b.isMoving()) {
                drawY = b.getY() + offsetY - 335;
            } else {
                drawY = b.getY() + offsetY - 348;
            }

            if (b.isFacingRight()) {
                g2.drawImage(img, drawX + w, drawY + tileOffset, -w, h, null);
            } else {
                g2.drawImage(img, drawX, drawY + tileOffset, w, h, null);
            }
        }
    }

    private void drawMinitrols(Graphics2D g2, int offsetX) {
        for (Minitroll m : minitrols) {
            Image img = m.getImage();
            if (img == null) continue;

            int w = (int) (img.getWidth(null) * SCALE);
            int h = (int) (img.getHeight(null) * SCALE);

            int drawX;

            if (m.isFacingRight() && !m.exploding) {
                drawX = m.getX() + offsetX - 10;
            } else if (m.exploding) {
                if (m.isFacingRight()) {
                    drawX = m.getX() + offsetX - 40;
                } else {
                    drawX = m.getX() + offsetX - 15;
                }
            } else {
                drawX = m.getX() + offsetX;
            }

            int drawY;

            if (m.chasing) {
                drawY = m.getY() + offsetY - 336;
            } else if (m.exploding) {
                drawY = m.getY() + offsetY - 350;
            } else {
                drawY = m.getY() + offsetY - 335;
            }

            if (m.isFacingRight()) {
                g2.drawImage(img, drawX + w, drawY + 385, -w, h, null);
            } else {
                g2.drawImage(img, drawX, drawY + 385, w, h, null);
            }
        }
    }

    private void drawPlayer2(Graphics2D g2, int offsetX) {
        if (!player2.isVisible()) return;

        Image img2 = player2.getImage();
        int w2 = (int) (img2.getWidth(null) * SCALE);
        int h2 = (int) (img2.getHeight(null) * SCALE);

        int p2Offset = -10;

        if (player2.dead && !player2.gettingHit) {
            p2Offset += 20;
        } else if (player2.isBlocking() && player2.facingRight) {
            p2Offset = 4;
        } else if (player2.isBlocking() && !player2.facingRight) {
            p2Offset = 5;
        } else if (player2.gettingHit && player2.facingRight) {
            p2Offset += 20;
        } else if (player2.gettingHit && !player2.facingRight) {
            p2Offset += 20;
        } else if ((player2.jumping || player2.inAir) && player2.charging && player2.facingRight) {
            p2Offset += -5;
        } else if ((player2.jumping || player2.inAir) && player2.charging && !player2.facingRight) {
            p2Offset += -16;
        } else if ((player2.jumping || player2.inAir) && player2.shooting && player2.facingRight) {
            p2Offset += -8;
        } else if ((player2.jumping || player2.inAir) && player2.shooting && !player2.facingRight) {
            p2Offset += -10;
        } else if (player2.charging && player2.facingRight) {
            p2Offset += -4;
        } else if (player2.charging && !player2.facingRight) {
            p2Offset += -16;
        } else if (player2.shooting && player2.facingRight) {
            p2Offset += -6;
        } else if (player2.shooting && !player2.facingRight) {
            p2Offset += -14;
        } else if ((player2.jumping || player2.inAir) && player2.moving && player2.facingRight) {
            p2Offset += 10;
        } else if ((player2.jumping || player2.inAir) && player2.moving && !player2.facingRight) {
            p2Offset += 12;
        } else if ((player2.jumping || player2.inAir) && player2.facingRight) {
            p2Offset += 10;
        } else if ((player2.jumping || player2.inAir) && !player2.facingRight) {
            p2Offset += 15;
        } else if (!player2.facingRight && player2.moving) {
            p2Offset += -15;
        } else if (!player2.facingRight) {
            p2Offset += -8;
        } else if (player2.moving) {
            p2Offset += -35;
        }

        int p2X = Math.round(player2.getX()) + offsetX + p2Offset - 25;
        int p2Y = Math.round(player2.getY());

        int drawY2;

        if (player2.moving) {
            drawY2 = p2Y + 48 + (img2.getHeight(null) - h2);
        } else if (player2.gettingHit) {
            drawY2 = p2Y + 50 + (img2.getHeight(null) - h2);
        } else if (player2.dead && !player2.gettingHit) {
            drawY2 = p2Y + 62 + (img2.getHeight(null) - h2);
        } else if (player2.isBlocking()) {
            drawY2 = p2Y + 49 + (img2.getHeight(null) - h2);
        } else {
            drawY2 = p2Y + 45 + (img2.getHeight(null) - h2);
        }

        if (player2.facingRight) {
            g2.drawImage(img2, p2X, drawY2, w2, h2, null);
        } else {
            g2.drawImage(img2, p2X + w2, drawY2, -w2, h2, null);
        }

        Rectangle p2Box = player2.getHitBox();
        g2.setColor(Color.BLUE);
    }

    private void drawPlayer1(Graphics2D g2, int offsetX) {
        if (!player1.isVisible()) return;

        Image img1 = player1.getImage();
        int w1 = (int) (img1.getWidth(null) * SCALE);
        int h1 = (int) (img1.getHeight(null) * SCALE);

        int p1Offset = -15;

        if (player1.gettingHit) {
            p1Offset = -15;
        } else if (player1.isBlocking() && player1.facingRight) {
            p1Offset = 15;
        } else if (player1.isBlocking() && !player1.facingRight) {
            p1Offset = 10;
        } else if (player1.dead && !player1.gettingHit) {
            p1Offset += 30;
        } else if (player1.attacking && player1.facingRight) {
            p1Offset += -30;
        } else if (player1.attacking && !player1.facingRight) {
            p1Offset += -100;
        } else if (player1.moveAttacking && player1.facingRight) {
            p1Offset += -25;
        } else if (player1.moveAttacking && !player1.facingRight) {
            p1Offset += -100;
        } else if (player1.jumpAttacking && player1.facingRight) {
            p1Offset += -28;
        } else if (player1.jumpAttacking && !player1.facingRight) {
            p1Offset += -70;
        } else if ((player1.jumping || player1.inAir) && player1.moving && player1.facingRight) {
            p1Offset += 10;
        } else if ((player1.jumping || player1.inAir) && player1.facingRight) {
            p1Offset += 10;
        } else if ((player1.jumping || player1.inAir) && !player1.facingRight) {
            p1Offset += 5;
        } else if (!player1.facingRight && player1.moving) {
            p1Offset += -80;
        } else if (!player1.facingRight) {
            p1Offset += -28;
        } else if (player1.moving) {
            p1Offset += -10;
        }

        int p1X = Math.round(player1.getX()) + offsetX + p1Offset - 25;
        int p1Y = Math.round(player1.getY());

        int drawY;

        if (player1.dead && !player1.gettingHit) {
            drawY = p1Y + 60 + (img1.getHeight(null) - h1);
        } else {
            drawY = p1Y + 45 + (img1.getHeight(null) - h1);
        }

        if (player1.facingRight) {
            g2.drawImage(img1, p1X, drawY, w1, h1, null);
        } else {
            g2.drawImage(img1, p1X + w1, drawY, -w1, h1, null);
        }

        Rectangle p1Box = player1.getHitBox();
        g2.setColor(Color.RED);
    }

    private void drawPlayer2Countdown(Graphics2D g2, int offsetX) {
        if (player2.isDead() || isPlayer2OnScreen(offsetX)) return;

        int seconds = (RESPAWN_TIME - p2OffScreenTime) / 20 + 1;

        int p2ScreenX = Math.round(player2.getX()) + offsetX;
        int p2ScreenY = Math.round(player2.getY());

        if (p2ScreenY < 20) {
            p2ScreenY = 20;
        }
        if (p2ScreenY > screenHeight - 20) {
            p2ScreenY = screenHeight - 20;
        }

        int drawX;

        if (p2ScreenX < 0) {
            drawX = 10;
        } else {
            drawX = screenWidth - 30;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String text = "" + seconds;

        g2.setColor(Color.WHITE);
        g2.drawString(text, drawX - 1, p2ScreenY);
        g2.drawString(text, drawX + 1, p2ScreenY);
        g2.drawString(text, drawX, p2ScreenY - 1);
        g2.drawString(text, drawX, p2ScreenY + 1);
        g2.drawString(text, drawX - 1, p2ScreenY - 1);
        g2.drawString(text, drawX + 1, p2ScreenY - 1);
        g2.drawString(text, drawX - 1, p2ScreenY + 1);
        g2.drawString(text, drawX + 1, p2ScreenY + 1);

        g2.setColor(Color.RED);
        g2.drawString(text, drawX, p2ScreenY);
    }

    public boolean isPlayer2OnScreen(int offsetX) {
        int screenX = player2.getX() + offsetX;
        return (screenX >= 0 && screenX <= screenWidth);
    }

    public void moveLeftP1() {
        player1.move(1);
    }

    public void moveRightP1() {
        player1.move(2);
    }

    public void moveLeftP2() {
        player2.move(1);
    }

    public void moveRightP2() {
        player2.move(2);
    }

    public void jumpP1() {
        player1.move(3);
    }

    public void jumpP2() {
        player2.move(3);
    }

    public void player1Attack() {
        player1.attack();
    }

    public void player2StartCharge() {
        player2.startCharge();
    }

    public void player2Shoot() {
        player2.releaseShoot();
    }

    public void spawnArrow(int x, int y, boolean facingRight) {
        if (arrows.size() >= MAX_ARROWS) {
            arrows.remove(0);
        }

        Image arrowImg = ImageManager.loadImage("src/images/ItemsandObjects/Arrow.png");
        arrows.add(new Arrow(x, y, facingRight, arrowImg));
    }

    public void spawnEnemyArrow(int x, int y, boolean facingRight) {
        Image img = ImageManager.loadImage("src/images/ItemsandObjects/Arrow.png");
        enemyArrows.add(new EnemyArrow(x, y - 12, facingRight, img));
    }

    private void updateArrows() {
        int mapWidthPixels = tilesToPixels(mapWidth);

        for (int i = arrows.size() - 1; i >= 0; i--) {
            Arrow a = arrows.get(i);
            a.update();

            if (a.getX() < 0 || a.getX() > mapWidthPixels) {
                arrows.remove(i);
            }
        }
    }

    public void update() {
        player1.update();
        player2.update();
        updateArrows();
        updateCollectibles();
        updateSlimes();
        updateSkeletons();
        updateBears();
        updateMinitrols();
        updateEndFlags();
        updatePlayer1Attack();
        updateArrowHits();
        updateEnemyArrows();
        updateDeadEnemies();
        updatePlayer2Respawn();
    }

    private void updateSlimes() {
        for (Slime s : slimes) {
            s.update();

            if (!player1.isDead() && s.getHitBox().intersects(player1.getHitBox())) {
                if (s.attackSoundCooldown == 0) {
                    soundManager.playSound("slimeAttack", false);
                    s.attackSoundCooldown = 20;
                }

                boolean hitFromRight = s.getX() > player1.getX();

                if (player1.isBlocking()) {
                    player1.blockPush(hitFromRight);
                } else {
                    player1.takeDamage(s.getDamage(), hitFromRight);
                }
            }

            if (!player2.isDead() && s.getHitBox().intersects(player2.getHitBox())) {
                if (s.attackSoundCooldown == 0) {
                    soundManager.playSound("slimeAttack", false);
                    s.attackSoundCooldown = 20;
                }

                boolean hitFromRight = s.getX() > player2.getX();

                if (player2.isBlocking()) {
                    player2.blockPush(hitFromRight);
                } else {
                    player2.takeDamage(s.getDamage(), hitFromRight);
                }
            }
        }
    }

    private void updateSkeletons() {
        for (Skeleton s : skeletons) {
            s.update();
        }
    }

    private void updateBears() {
        for (Bear b : bears) {
            b.update();
        }
    }

    private void updateMinitrols() {
        for (Minitroll m : minitrols) {
            m.update();
        }
    }

    private void updateEndFlags() {
        for (EndFlag f : endFlags) {
            f.update();

            if (!player1.isDead() && f.getHitBox().intersects(player1.getHitBox())) {
                levelComplete = true;
                soundManager.playSound("goal", false);
            }

            if (!player2.isDead() && f.getHitBox().intersects(player2.getHitBox())) {
                levelComplete = true;
                soundManager.playSound("goal", false);
            }
        }
    }

    private void updatePlayer1Attack() {
        if (player1.dead) return;
        if (!player1.attacking && !player1.moveAttacking && !player1.jumpAttacking) return;
        if (!player1.isAttackActiveFrame()) return;

        int hitWidth = 100;
        int hitHeight = 80;

        int hitX = player1.getX();
        int hitY = player1.getY();

        if (player1.jumpAttacking) {
            hitY = player1.getY() - 10;
        }

        if (player1.facingRight) {
            hitX = player1.getX() + player1.getDisplayWidth();
        } else {
            hitX = player1.getX() - hitWidth;
        }

        Rectangle attackBox = new Rectangle(hitX, hitY, hitWidth, hitHeight);

        for (Slime s : slimes) {
            if (attackBox.intersects(s.getHitBox())) {
                boolean hitFromRight = player1.getX() > s.getX();
                s.takeDamage(player1.getAttackDamage(), hitFromRight, false);
            }
        }

        for (Skeleton s : skeletons) {
            if (attackBox.intersects(s.getHitBox())) {
                boolean hitFromRight = player1.getX() > s.getX();
                s.takeDamage(player1.getAttackDamage(), hitFromRight, false);
            }
        }

        for (Bear b : bears) {
            if (attackBox.intersects(b.getHitBox())) {
                boolean hitFromRight = player1.getX() > b.getX();
                b.takeDamage(player1.getAttackDamage(), hitFromRight, false);
            }
        }

        for (Minitroll m : minitrols) {
            if (attackBox.intersects(m.getHitBox())) {
                m.takeDamage(player1.getAttackDamage(), player1.getX() > m.getX(), false);
            }
        }
    }

    private void updateArrowHits() {
        for (Arrow a : arrows) {
            if (!a.isActive()) continue;

            for (Slime s : slimes) {
                if (a.collides(s.getHitBox())) {
                    boolean hitFromRight = a.getX() > s.getX();
                    s.takeDamage(a.getDamage(), hitFromRight, true);
                    a.deactivate();
                    break;
                }
            }

            for (Skeleton s : skeletons) {
                if (a.collides(s.getHitBox())) {
                    boolean hitFromRight = a.getX() > s.getX();
                    s.takeDamage(a.getDamage(), hitFromRight, true);
                    a.deactivate();
                    break;
                }
            }

            for (Bear b : bears) {
                if (a.collides(b.getHitBox())) {
                    boolean hitFromRight = a.getX() > b.getX();
                    b.takeDamage(a.getDamage(), hitFromRight, true);
                    a.deactivate();
                    break;
                }
            }

            for (Minitroll m : minitrols) {
                if (a.collides(m.getHitBox())) {
                    m.takeDamage(a.getDamage(), a.getX() > m.getX(), true);
                    a.deactivate();
                    break;
                }
            }
        }
    }

    private void updateEnemyArrows() {
        for (int i = enemyArrows.size() - 1; i >= 0; i--) {
            EnemyArrow a = enemyArrows.get(i);
            a.update();

            if (a.getX() < 0 || a.getX() > tilesToPixels(mapWidth)) {
                enemyArrows.remove(i);
                continue;
            }

            if (!player1.dead && a.collides(player1.getHitBox())) {
                player1.takeDamage(a.getDamage(), a.getX() > player1.getX());
                enemyArrows.remove(i);
                continue;
            }

            if (!player2.dead && a.collides(player2.getHitBox())) {
                player2.takeDamage(a.getDamage(), a.getX() > player2.getX());
                enemyArrows.remove(i);
            }
        }
    }

    private void updateDeadEnemies() {
        for (int i = slimes.size() - 1; i >= 0; i--) {
            if (slimes.get(i).isDead()) {
                boolean p1Killed = !slimes.get(i).wasKilledByArrow();
                addScore(p1Killed, slimes.get(i).getScoreValue());
                slimes.remove(i);
            }
        }

        for (int i = skeletons.size() - 1; i >= 0; i--) {
            if (skeletons.get(i).isDead()) {
                boolean p1Killed = !skeletons.get(i).wasKilledByArrow();
                addScore(p1Killed, skeletons.get(i).getScoreValue());
                skeletons.remove(i);
            }
        }

        for (int i = bears.size() - 1; i >= 0; i--) {
            if (bears.get(i).isDead()) {
                boolean p1Killed = !bears.get(i).wasKilledByArrow();
                addScore(p1Killed, bears.get(i).getScoreValue());
                bears.remove(i);
            }
        }

        for (int i = minitrols.size() - 1; i >= 0; i--) {
            if (minitrols.get(i).isDead()) {
                boolean p1Killed = !minitrols.get(i).wasKilledByArrow();
                addScore(p1Killed, minitrols.get(i).getScoreValue());
                minitrols.remove(i);
            }
        }
    }

    private void updatePlayer2Respawn() {
        int mapWidthPixels = tilesToPixels(mapWidth);

        int offsetX = screenWidth / 2 - Math.round(player1.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

        if (!player2.isDead() && !isPlayer2OnScreen(offsetX)) {
            p2OffScreenTime++;

            if (p2OffScreenTime >= RESPAWN_TIME) {
                int newX = player1.getX() + 20;
                int newY = player1.getY();
                player2.respawn(newX, newY, false);
                p2OffScreenTime = 0;
            }
        } else {
            p2OffScreenTime = 0;
        }
    }

    private void updateCollectibles() {
        for (int i = collectibles.size() - 1; i >= 0; i--) {
            Collectible c = collectibles.get(i);
            c.update();

            if (c.collidesWith(player1)) {
                if (c.isCoin()) {
                    p1Coins++;
                    p1Score += c.getPoints();
                } else if (c.isFood()) {
                    player1.heal(c.getHealAmount());
                }

                c.collect();
                collectibles.remove(i);
                continue;
            }

            if (c.collidesWith(player2)) {
                if (c.isCoin()) {
                    p2Coins++;
                    p2Score += c.getPoints();
                } else if (c.isFood()) {
                    player2.heal(c.getHealAmount());
                }

                c.collect();
                collectibles.remove(i);
            }
        }
    }

    protected void respawnPlayer2() {
        int newX = player1.getX() + 20;
        int newY = player1.getY();
        player2.respawn(newX, newY, true);
    }

    public void addScore(boolean player1Killed, int amount) {
        if (player1Killed) {
            p1Score += amount;
        } else {
            p2Score += amount;
        }
    }

    public void addSlime(Slime s) { slimes.add(s); }
    public void addSkeleton(Skeleton s) { skeletons.add(s); }
    public void addBear(Bear b) { bears.add(b); }
    public void addMinitroll(Minitroll m) { minitrols.add(m); }
    public void addEndFlag(EndFlag e) { endFlags.add(e); }
    public void addCollectible(Collectible c) { collectibles.add(c); }

    public Player1 getPlayer1() { return player1; }
    public Player2 getPlayer2() { return player2; }

    public int getOffsetX() {
        int mapWidthPixels = tilesToPixels(mapWidth);
        int offsetX = screenWidth / 2 - Math.round(player1.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);
        return offsetX;
    }

    public boolean isLevelComplete() { return levelComplete; }
    public int getPlayer1Health() { return player1.getHealth(); }
    public int getPlayer2Health() { return player2.getHealth(); }
    public int getP1Score() { return p1Score; }
    public int getP2Score() { return p2Score; }
    public int getP1Coins() { return p1Coins; }
    public int getP2Coins() { return p2Coins; }
}