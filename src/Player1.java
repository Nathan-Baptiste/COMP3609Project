import javax.swing.JPanel;
import java.awt.*;

public class Player1 extends Player {


    public Player1(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);

        Image idleStrip = ImageManager.loadImage("src/images/Player1/Player1Idle.png");
        idleAnim = new StripAnimation(idleStrip, 4, 0, 0, panel, true);

        Image runStrip = ImageManager.loadImage("src/images/Player1/Player1Run.png");
        runAnim = new StripAnimation(runStrip, 8, 0, 0, panel, true);

        Image jumpStrip = ImageManager.loadImage("src/images/Player1/Player1Jump.png");
        jumpAnim = new StripAnimation(jumpStrip, 5, 0, 0, panel, false);

        Image attackStrip = ImageManager.loadImage("src/images/Player1/Player1Attack.png");
        attackAnim = new StripAnimation(attackStrip, 6, 0, 0, panel, false);

        Image moveAttackStrip = ImageManager.loadImage("src/images/Player1/Player1RunAttack.png");
        moveAttackAnim = new StripAnimation(moveAttackStrip, 8, 0, 0, panel, false);

        Image jumpAttackStrip = ImageManager.loadImage("src/images/Player1/Player1JumpAttack.png");
        jumpAttackAnim = new StripAnimation(jumpAttackStrip, 6, 0, 0, panel, false);

        idleAnim.start();
        runAnim.start();
        jumpAnim.start();
        attackAnim.start();
        moveAttackAnim.start();
        jumpAttackAnim.start();
    }

    public void attack() {
        if (attacking || moveAttacking || jumpAttacking) return;

        if (inAir || jumping) {
            jumpAttacking = true;
            jumpAttackAnim.start();
        }
        else if (moving) {
            moveAttacking = true;
            moveAttackAnim.start();
        }
        else {
            attacking = true;
            attackAnim.start();
        }
    }
}