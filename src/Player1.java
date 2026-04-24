import javax.swing.JPanel;
import java.awt.*;

public class Player1 extends Player {

    public Player1(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);

        Image strip = ImageManager.loadImage("src/images/Player1/Player1Idle.png");

        idleAnim = new StripAnimation(strip, 4, 0, 0, panel);
        idleAnim.start();
    }
}